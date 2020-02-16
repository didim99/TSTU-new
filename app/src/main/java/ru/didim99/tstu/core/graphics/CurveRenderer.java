package ru.didim99.tstu.core.graphics;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.graphics.curve.Point;
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
  private static final int COLOR_CURVE = R.color.graph0;
  private static final int COLOR_POINT_INACTIVE = R.color.graph2;
  private static final int COLOR_POINT_ACTIVE = R.color.graph4;
  private static final float CURVE_WIDTH = 5;
  private static final float POINT_RADIUS_INACTIVE = 15;
  private static final float POINT_RADIUS_ACTIVE = 50;

  private static final class CurveType {
    private static final int LAGRANGE  = 0;
    private static final int BEZIER    = 1;
  }

  private boolean configured;
  private BlockingQueue<TouchEvent> eventQueue;
  private StateChangeListener listener;
  private Point activePoint;
  // Drawing
  private Paint paintCurve;
  private Paint paintInactive;
  private Paint paintActive;

  public CurveRenderer(DrawerView target, Config config, Resources res) {
    super(target, Config.Type.SCENE, config,
      DEFAULT_WIDTH, DEFAULT_HEIGHT, true);
    this.eventQueue = new LinkedBlockingQueue<>();
    this.configured = false;
    this.activePoint = null;

    paintCurve = new Paint();
    paintCurve.setColor(res.getColor(COLOR_CURVE));
    paintCurve.setStrokeCap(Paint.Cap.ROUND);
    paintCurve.setStrokeWidth(CURVE_WIDTH);
    paintInactive = new Paint();
    paintInactive.setColor(res.getColor(COLOR_POINT_INACTIVE));
    paintInactive.setStrokeWidth(POINT_RADIUS_INACTIVE);
    paintInactive.setStrokeCap(Paint.Cap.ROUND);
    paintActive = new Paint();
    paintActive.setColor(res.getColor(COLOR_POINT_ACTIVE));
    paintActive.setStrokeWidth(POINT_RADIUS_ACTIVE);
    paintActive.setStrokeCap(Paint.Cap.ROUND);

    if (config == null) {
      this.config.points = new ArrayList<>();
      this.config.curveType = CurveType.LAGRANGE;
    }

    setRealtimeMode();
    onSceneCreated(this);
  }

  public void setStateChangeListener(StateChangeListener listener) {
    this.listener = listener;
  }

  public void setCurveType(int type) {
    config.curveType = type;
  }

  @Override
  public void clear() {
    if (config.points != null) {
      config.points.clear();
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
        eventPossible = !checkPoint(event.getPosition());
        if (eventPossible) onPointsCountChanged(true);
        break;
      case DOUBLE_TAP:
        eventPossible = checkPoint(event.getPosition());
        if (eventPossible) onPointsCountChanged(false);
        break;
      case MOTION_START:
        eventPossible = checkPoint(event.getPosition());
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

  private void onPointsCountChanged(boolean add) {
    if (listener != null) listener.onPointsCountChanged(
      config.points.size() + (add ? 1 : -1));
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

  private void dispatchTouchEvent(TouchEvent event) {
    switch (event.getAction()) {
      case SINGLE_TAP:
        config.points.add(new Point(event.getPosition()));
        if (config.curveType == CurveType.LAGRANGE)
          Collections.sort(config.points, Point::compareX);
        break;
      case DOUBLE_TAP:
        int removeIndex = getPointIndex(event.getPosition());
        if (removeIndex >= 0) config.points.remove(removeIndex);
        break;
      case MOTION_START:
        int activeIndex = getPointIndex(event.getPosition());
        if (activeIndex >= 0) setActivePoint(config.points.get(activeIndex));
        break;
      case MOVE:
        activePoint.getPosition().set(event.getPosition());
        if (config.curveType == CurveType.LAGRANGE)
          Collections.sort(config.points, Point::compareX);
        break;
      case MOTION_END:
        setActivePoint(null);
        break;
    }
  }

  @Override
  public void draw(Canvas canvas) {
    configure(canvas);
    if (config.points.isEmpty()) return;

    for (Point point : config.points) {
      PointF position = point.getPosition();
      canvas.drawPoint(position.x, position.y,
        point.isActive() ? paintActive : paintInactive);
    }
  }

  private boolean checkPoint(PointF position) {
    for (Point point : config.points)
      if (point.nearest(position)) return true;
    return false;
  }

  private int getPointIndex(PointF position) {
    int index = -1;

    double distance, minDistance = Double.POSITIVE_INFINITY;
    for (int i = 0; i < config.points.size(); i++) {
      distance = config.points.get(i).distance(position);
      if (distance < minDistance) {
        minDistance = distance;
        index = i;
      }
    }

    return index;
  }

  private void setActivePoint(Point point) {
    if (point == null) {
      activePoint.setActive(false);
      activePoint = null;
    } else {
      activePoint = point;
      activePoint.setActive(true);
    }
  }

  public interface StateChangeListener {
    void onPointsCountChanged(int count);
  }
}
