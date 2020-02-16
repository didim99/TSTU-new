package ru.didim99.tstu.core.graphics;

import android.graphics.PointF;

/**
 * Created by didim99 on 16.02.20.
 */
public class TouchEvent {
  public enum Action { SINGLE_TAP, DOUBLE_TAP, MOTION_START, MOVE, MOTION_END }

  private Action action;
  private PointF position;

  public TouchEvent(Action action, PointF position) {
    this.action = action;
    this.position = position;
  }

  Action getAction() { return action; }
  PointF getPosition() { return position; }

  @Override
  public String toString() {
    return action + ": " + position;
  }
}
