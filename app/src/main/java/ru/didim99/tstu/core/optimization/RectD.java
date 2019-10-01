package ru.didim99.tstu.core.optimization;

/**
 * Created by didim99 on 29.09.19.
 */
public class RectD {
  public double xMin, xMax;
  public double yMin, yMax;

  RectD(double xMin, double xMax, double yMin, double yMax) {
    this.xMin = xMin;
    this.xMax = xMax;
    this.yMin = yMin;
    this.yMax = yMax;
  }

  @Override
  public String toString() {
    return "Rect: [x: " + xMin + "..." + xMax
      + " y: " + yMin + "..." + yMax + "]";
  }
}
