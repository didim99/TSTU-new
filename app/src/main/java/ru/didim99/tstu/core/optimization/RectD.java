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

  RectD(RectD src) {
    this(src.xMin, src.xMax, src.yMin, src.yMax);
  }

  RectD margin(double factor) {
    RectD r = new RectD(this);
    double dx = Math.abs(xMax - xMin) * factor / 2;
    double dy = Math.abs(yMax - yMin) * factor / 2;
    r.xMin -= dx; r.xMax += dx;
    r.yMin -= dy; r.yMax += dy;
    return r;
  }

  RectD coverRelative(double width, double height) {
    RectD r = new RectD(this);
    double dx = Math.abs(xMax - xMin);
    double dy = Math.abs(yMax - yMin);
    double ratio = width / height, margin;

    if (ratio > dx / dy) {
      margin = (dy * ratio - dx) / 2;
      r.xMin -= margin; r.xMax += margin;
    } else {
      margin = (dx / ratio - dy) / 2;
      r.yMin -= margin; r.yMax += margin;
    }

    return r;
  }

  @Override
  public String toString() {
    return "Rect: [x: " + xMin + "..." + xMax
      + " y: " + yMin + "..." + yMax + "]";
  }
}
