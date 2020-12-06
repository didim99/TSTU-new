package ru.didim99.tstu.core.graphics;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.graphics.curve.Curve;
import ru.didim99.tstu.core.graphics.curve.Point;
import ru.didim99.tstu.core.graphics.curve.builder.BaseBuilder;
import ru.didim99.tstu.ui.UIManager;
import ru.didim99.tstu.ui.view.DrawerView;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 15.02.20.
 */
public class CurveRenderer extends AsyncRenderer implements Scene {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_CR";
  private static final int DEFAULT_WIDTH = 500;
  private static final int DEFAULT_HEIGHT = 500;
  private static final long EVENT_WAIT = 1000;
  // Drawer settings
  private static final PaintConfig PAINT_FRAME =
    new PaintConfig(R.color.curveFrame, 3, Paint.Cap.ROUND);
  private static final PaintConfig PAINT_CURVE =
    new PaintConfig(UIManager.getInstance().resolveAttr(
      R.attr.clr_blue), 5, Paint.Cap.ROUND);
  private static final PaintConfig PAINT_ARM =
    new PaintConfig(UIManager.getInstance().resolveAttr(
      R.attr.clr_valueBarBg), 3, Paint.Cap.ROUND);
  private static final PaintConfig PAINT_POINT_DEFAULT =
    new PaintConfig(UIManager.getInstance().resolveAttr(
      R.attr.clr_yellow), 15, Paint.Cap.ROUND);
  private static final PaintConfig PAINT_POINT_CONTROL =
    new PaintConfig(UIManager.getInstance().resolveAttr(
      R.attr.clr_green), 15, Paint.Cap.ROUND);
  private static final PaintConfig PAINT_POINT_ACTIVE =
    new PaintConfig(UIManager.getInstance().resolveAttr(
      R.attr.clr_red), 50, Paint.Cap.ROUND);


  private boolean configured;
  private BlockingQueue<TouchEvent> eventQueue;
  private StateChangeListener listener;
  // Drawing
  private final Paint paintInactive, paintActive;
  private final Paint paintCurve, paintFrame;
  private final Paint paintControl, paintArm;

  public CurveRenderer(DrawerView target, Config config, Resources res) {
    super(target, Config.Type.SCENE, config,
      DEFAULT_WIDTH, DEFAULT_HEIGHT, true);
    this.eventQueue = new LinkedBlockingQueue<>();
    this.configured = false;

    paintInactive = getPaint(PAINT_POINT_DEFAULT, res);
    paintActive = getPaint(PAINT_POINT_ACTIVE, res);
    paintCurve = getPaint(PAINT_CURVE, res);
    paintFrame = getPaint(PAINT_FRAME, res);
    paintControl = getPaint(PAINT_POINT_CONTROL, res);
    paintArm = getPaint(PAINT_ARM, res);

    if (config == null) {
      this.config.curve = new Curve();
    }

    setRealtimeMode();
    onSceneCreated(this);
  }

  public void setStateChangeListener(StateChangeListener listener) {
    this.listener = listener;
  }

  public void setCurveType(int type) {
    config.curve.setType(type);
    publishProgress();
  }

  public void setDrawPoints(boolean drawPoints) {
    config.curve.setDrawPoints(drawPoints);
    publishProgress();
  }

  public void setDrawFrame(boolean drawFrame) {
    config.curve.setDrawFrame(drawFrame);
    publishProgress();
  }

  public void setSyncControls(boolean syncControls) {
    config.curve.setSyncControls(syncControls);
    publishProgress();
  }

  @Override
  public void clear() {
    if (config.curve != null && config.curve.clear()) {
      onPointsCountChanged(false);
      publishProgress();
    }
  }

  /**
   * @return {@code true} if event was handled, {@code false} otherwise
   **/
  public boolean onTouchEvent(TouchEvent event) {
    TouchEvent.Action action = event.getAction();
    boolean eventPossible = true;
    switch (action) {
      case SINGLE_TAP:
        eventPossible = !config.curve.checkPoint(event.getPosition(), false);
        if (eventPossible) onPointsCountChanged(true);
        break;
      case DOUBLE_TAP:
        eventPossible = config.curve.checkPoint(event.getPosition(), true);
        if (eventPossible) onPointsCountChanged(false);
        break;
      case MOTION_START:
        eventPossible = config.curve.checkPoint(event.getPosition(), false);
        break;
    }

    if (eventPossible) {
      if (!action.equals(TouchEvent.Action.MOVE))
        MyLog.v(LOG_TAG, "Registered touch event: " + event);
      dispatchTouchEvent(event);
      eventQueue.add(event);
    }

    return eventPossible;
  }

  private void dispatchTouchEvent(TouchEvent event) {
    switch (event.getAction()) {
      case SINGLE_TAP:
        config.curve.addPoint(event.getPosition());
        break;
      case DOUBLE_TAP:
        config.curve.removePoint(event.getPosition());
        break;
      case MOTION_START:
        config.curve.setActivePoint(event.getPosition());
        break;
      case MOVE:
        config.curve.moveActivePoint(event.getPosition());
        break;
      case MOTION_END:
        config.curve.setActivePoint(null);
        break;
    }
  }

  private void onPointsCountChanged(boolean add) {
    if (listener != null) listener.onPointsCountChanged(
      config.curve.getBasePoints().size() + (add ? 1 : -1));
  }

  private void configure(Canvas canvas) {
    if (configured) return;
    config.width = canvas.getWidth();
    config.height = canvas.getHeight();
    configured = true;
  }

  @Override
  void update() throws InterruptedException {
    TouchEvent event = eventQueue.poll(
      EVENT_WAIT, TimeUnit.MILLISECONDS);
    if (event != null) publishProgress();
  }

  @Override
  public void draw(Canvas canvas) {
    configure(canvas);
    if (config.curve.isEmpty()) return;
    BaseBuilder builder = config.curve.getBuilder();

    if (builder.rebuild()) {
      if (config.curve.isDrawFrame())
        canvas.drawLines(builder.getFramePuffer(),
          0, builder.getFrameBufferSize(), paintFrame);
      canvas.drawLines(builder.getPointsPuffer(), paintCurve);
    }

    if (config.curve.isDrawPoints()) {
      if (config.curve.getBuilder().hasControlPoints()) {
        canvas.drawLines(builder.getArmBuffer(),
          0, builder.getArmBufferSize(), paintArm);
        for (Point point : config.curve.getControlPoints()) {
          PointF position = point.getPosition();
          canvas.drawPoint(position.x, position.y,
            point.isActive() ? paintActive : paintControl);
        }
      }

      for (Point point : config.curve.getBasePoints()) {
        PointF position = point.getPosition();
        canvas.drawPoint(position.x, position.y,
          point.isActive() ? paintActive : paintInactive);
      }
    }
  }

  private Paint getPaint(PaintConfig config, Resources res) {
    Paint paint = new Paint();
    paint.setColor(res.getColor(config.color));
    paint.setStrokeWidth(config.width);
    paint.setStrokeCap(config.cap);
    return paint;
  }

  private static class PaintConfig {
    private final int color;
    private final float width;
    private final Paint.Cap cap;

    private PaintConfig(int color, float width, Paint.Cap cap) {
      this.color = color;
      this.width = width;
      this.cap = cap;
    }
  }

  public interface StateChangeListener {
    void onPointsCountChanged(int count);
  }
}
