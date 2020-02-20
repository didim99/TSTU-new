package ru.didim99.tstu.core.graphics.curve;

import android.graphics.PointF;
import java.util.ArrayList;

/**
 * Created by didim99 on 16.02.20.
 */
public class Point {
  private static final float EQ_DISTANCE = 1;
  static final float TAP_DISTANCE = 60;

  public enum Type { BASE, CONTROL }

  private Type type;
  private PointF position;
  private boolean active, reversed;
  private ArrayList<Point> controls;
  private Point controlNext, controlPrev;
  private Point parent;

  public Point(PointF position) {
    this(Type.BASE, position);
  }

  public Point(PointF position, Point parent) {
    this(Type.CONTROL, position);
    this.parent = parent;
  }

  private Point(Type type, PointF position) {
    this.type = type;
    this.position = position;
    this.active = false;
    this.reversed = false;
  }

  public Type getType() {
    return type;
  }

  public PointF getPosition() {
    return position;
  }

  public boolean isActive() {
    return active;
  }

  ArrayList<Point> getControls() {
    return controls;
  }

  public Point getControlNext() {
    return controlNext;
  }

  public Point getControlPrev() {
    return controlPrev;
  }

  public Point getParent() {
    return parent;
  }

  public float getVisibleX() {
    return position.x;
  }

  public float getVisibleY() {
    return position.y;
  }

  public float getReverseX() {
    return parent.position.x * 2 - position.x;
  }

  public float getReverseY() {
    return parent.position.y * 2 - position.y;
  }

  public float getRealX() {
    return reversed ? getReverseX() : position.x;
  }

  public float getRealY() {
    return reversed ? getReverseY() : position.y;
  }

  void setActive(boolean active) {
    this.active = active;
  }

  public void clearControls() {
    if (controls != null) controls.clear();
  }

  public void setControlNext(Point controlNext) {
    this.controlNext = controlNext;
    if (controlNext != null)
      addControl(controlNext);
  }

  public void setControlPrev(Point controlPrev) {
    this.controlPrev = controlPrev;
    if (controlPrev != null) {
      this.controlPrev.reversed = true;
      addControl(controlPrev);
    }
  }

  private void addControl(Point control) {
    if (controls == null) controls = new ArrayList<>();
    controls.add(control);
  }

  void moveTo(PointF pos) {
    if (controls != null && !controls.isEmpty()) {
      for (Point point : controls) point.position.offset(
        pos.x - position.x, pos.y - position.y);
    }

    position.set(pos);
  }

  double distance(PointF point) {
    double dx = Math.abs(position.x - point.x);
    double dy = Math.abs(position.y - point.y);
    return Math.sqrt(dx * dx + dy * dy);
  }

  boolean near(PointF point) {
    return Math.abs(position.x - point.x) < TAP_DISTANCE
      && Math.abs(position.y - point.y) < TAP_DISTANCE;
  }

  public boolean xEquals(Point point) {
    return Math.abs(position.x - point.position.x) < EQ_DISTANCE;
  }

  @Override
  public String toString() {
    return position + ": " + type + ", active: " + active;
  }

  static int compareX(Point p1, Point p2) {
    return Float.compare(p1.position.x, p2.position.x);
  }
}
