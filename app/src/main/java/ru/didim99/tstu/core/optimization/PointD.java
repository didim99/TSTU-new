package ru.didim99.tstu.core.optimization;

import java.util.Arrays;

/**
 * Created by didim99 on 06.09.19.
 */
public class PointD {
  private double[] data;

  PointD(double... v) {
    data = new double[v.length];
    System.arraycopy(v, 0, data, 0, v.length);
  }

  PointD(PointD src) {
    this(src.data);
  }

  void set(PointD src) {
    System.arraycopy(src.data, 0, data, 0, data.length);
  }

  void set(int pos, double val) {
    data[pos] = val;
  }

  double get(int pos) {
    return data[pos];
  }

  void add(int pos, double val) {
    data[pos] += val;
  }

  PointD add(PointD v) {
    PointD newPoint = new PointD(data);
    for (int i = 0; i < data.length; i++)
      newPoint.data[i] += v.data[i];
    return newPoint;
  }

  PointD sub(PointD v) {
    PointD newPoint = new PointD(data);
    for (int i = 0; i < data.length; i++)
      newPoint.data[i] -= v.data[i];
    return newPoint;
  }

  PointD mult(double v) {
    PointD newPoint = new PointD(data);
    for (int i = 0; i < data.length; i++)
      newPoint.data[i] *= v;
    return newPoint;
  }

  PointD div(double v) {
    PointD newPoint = new PointD(data);
    for (int i = 0; i < data.length; i++)
      newPoint.data[i] /= v;
    return newPoint;
  }

  boolean isZero(double eps) {
    double len = 0;
    for (double v : data)
      len += v * v;
    len = Math.sqrt(len);
    return len < eps;
  }

  int size() { return data.length; }

  @Override
  public String toString() {
    return "Point: " + Arrays.toString(data);
  }
}
