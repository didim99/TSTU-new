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
    for (int i = 0; i < data.length; i++)
      data[i] += v.data[i];
    return this;
  }

  PointD sub(PointD v) {
    for (int i = 0; i < data.length; i++)
      data[i] -= v.data[i];
    return this;
  }

  @Override
  public String toString() {
    return "Point: " + Arrays.toString(data);
  }
}
