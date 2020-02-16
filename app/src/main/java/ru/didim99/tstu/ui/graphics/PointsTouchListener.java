package ru.didim99.tstu.ui.graphics;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import ru.didim99.tstu.core.graphics.TouchEvent;
import ru.didim99.tstu.core.graphics.TouchEvent.Action;

/**
 * Created by didim99 on 16.02.20.
 */
class PointsTouchListener implements View.OnTouchListener {
  private static final float TAP_THRESHOLD = 60;
  private static final long TAP_DURATION = 400;

  private OnInteractionListener listener;
  private long lastTapDown, lastMoveDown;
  private PointF lastPoint, firstTapPoint;
  private long firstTapTime;
  private boolean tracking;

  PointsTouchListener(OnInteractionListener listener) {
    this.listener = listener;
    this.lastTapDown = 0;
    this.lastMoveDown = 0;
    this.firstTapTime = 0;
    this.tracking = false;
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    int action = event.getAction();
    if (action == MotionEvent.ACTION_DOWN) {
      lastTapDown = lastMoveDown = event.getEventTime();
      lastPoint = position(event);
    } else if (action == MotionEvent.ACTION_MOVE) {
      if (lastTapDown > 0 && isLongDistance(event, lastPoint)) lastTapDown = 0;
      else if (lastMoveDown > 0 && event.getEventTime() - lastMoveDown > TAP_DURATION) {
        if (tracking) listener.onInteraction(
          new TouchEvent(Action.MOVE, position(event)));
        else tracking = listener.onInteraction(
          new TouchEvent(Action.MOTION_START, lastPoint));
        if (!tracking) lastMoveDown = 0;
      }
    } else if (action == MotionEvent.ACTION_UP) {
      if (event.getEventTime() - lastTapDown < TAP_DURATION) {
        if (firstTapTime > 0 && event.getEventTime() - firstTapTime < TAP_DURATION) {
          if (!isLongDistance(event, firstTapPoint))
            listener.onInteraction(new TouchEvent(
              Action.DOUBLE_TAP, lastPoint));
          firstTapTime = 0;
        } else {
          if (!listener.onInteraction(new TouchEvent(
              Action.SINGLE_TAP, lastPoint))) {
            firstTapTime = event.getEventTime();
            firstTapPoint = lastPoint;
          } else v.performClick();
        }
      } else if (tracking)
        listener.onInteraction(new TouchEvent(
          Action.MOTION_END, position(event)));

      lastTapDown = 0;
      lastMoveDown = 0;
      tracking = false;
    }

    return true;
  }

  private boolean isLongDistance(MotionEvent event, PointF point) {
    return Math.abs(event.getX() - point.x) > TAP_THRESHOLD
      || Math.abs(event.getY() - point.y) > TAP_THRESHOLD;
  }

  private PointF position(MotionEvent event) {
    return new PointF(event.getX(), event.getY());
  }

  interface OnInteractionListener {
    boolean onInteraction(TouchEvent event);
  }
}
