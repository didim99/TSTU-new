package ru.didim99.tstu.core.optimization;

import java.util.Arrays;

/**
 * Created by didim99 on 06.09.19.
 */
public class PointD {
  private double[] data;

  public PointD(double... v) {
    data = new double[v.length];
    System.arraycopy(v, 0, data, 0, v.length);
  }

  public PointD(PointD src) {
    this(src.data);
  }

  public PointD set(PointD src) {
    System.arraycopy(src.data, 0, data, 0, data.length);
    return this;
  }

  public double get(int pos) {
    return data[pos];
  }

  public void set(int pos, double val) {
    data[pos] = val;
  }

  public void add(int pos, double val) {
    data[pos] += val;
  }

  public PointD add(PointD v) {
    PointD newPoint = new PointD(data);
    for (int i = 0; i < data.length; i++)
      newPoint.data[i] += v.data[i];
    return newPoint;
  }

  public PointD sub(PointD v) {
    PointD newPoint = new PointD(data);
    for (int i = 0; i < data.length; i++)
      newPoint.data[i] -= v.data[i];
    return newPoint;
  }

  public PointD mult(double v) {
    PointD newPoint = new PointD(data);
    for (int i = 0; i < data.length; i++)
      newPoint.data[i] *= v;
    return newPoint;
  }

  public PointD div(double v) {
    PointD newPoint = new PointD(data);
    for (int i = 0; i < data.length; i++)
      newPoint.data[i] /= v;
    return newPoint;
  }

  public boolean isZero(double eps) {
    double len = 0;
    for (double v : data)
      len += v * v;
    len = Math.sqrt(len);
    return len < eps;
  }

  public int size() { return data.length; }

  @Override
  public String toString() {
    return "Point: " + Arrays.toString(data);
  }
}
