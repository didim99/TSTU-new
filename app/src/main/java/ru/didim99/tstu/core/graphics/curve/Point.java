package ru.didim99.tstu.core.graphics.curve;

import android.graphics.PointF;

/**
 * Created by didim99 on 16.02.20.
 */
public class Point {
  private static final float TAP_DISTANCE = 60;
  private static final float EQ_DISTANCE = 1;

  private PointF position;
  private boolean active;

  public Point(PointF position) {
    this.position = position;
    this.active = false;
  }

  public PointF getPosition() {
    return position;
  }

  public boolean isActive() {
    return active;
  }

  public void setPosition(PointF position) {
    this.position = position;
  }

  void setActive(boolean active) {
    this.active = active;
  }

  double distance(PointF point) {
    double dx = Math.abs(position.x - point.x);
    double dy = Math.abs(position.y - point.y);
    return Math.sqrt(dx * dx + dy * dy);
  }

  boolean nearest(PointF point) {
    return Math.abs(position.x - point.x) < TAP_DISTANCE
      && Math.abs(position.y - point.y) < TAP_DISTANCE;
  }

  boolean xEquals(Point point) {
    return Math.abs(position.x - point.position.x) < EQ_DISTANCE;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Point)) return false;
    return nearest(((Point) obj).position);
  }

  @Override
  public String toString() {
    return position + ", active: " + active;
  }

  static int compareX(Point p1, Point p2) {
    return Float.compare(p1.position.x, p2.position.x);
  }
}
