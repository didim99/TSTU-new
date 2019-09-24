package ru.didim99.tstu.core.optimization;

/**
 * Created by didim99 on 06.09.19.
 */
public class PointD {
  private double x, y;

  PointD(double x, double y) {
    this.x = x;
    this.y = y;
  }

  double getX() { return x; }
  double getY() { return y; }

  @Override
  public String toString() {
    return "Point: [" + x + ", " + y + "]";
  }
}
