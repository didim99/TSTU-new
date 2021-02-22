package ru.didim99.tstu.core.math.common;

import java.util.Arrays;

/**
 * Created by didim99 on 06.09.19.
 */
public class PointD implements PointRN {
  private final double[] data;

  public PointD(int size) {
    this(size, 0);
  }

  public PointD(int size, double v) {
    data = new double[size];
    Arrays.fill(data, v);
  }

  public PointD(double... v) {
    data = new double[v.length];
    System.arraycopy(v, 0, data, 0, v.length);
  }

  public PointD(PointD src) {
    this(src.data);
  }

  public PointD copy() {
    return new PointD(this);
  }

  public PointD set(PointD src) {
    return set(src, Math.min(data.length, src.data.length));
  }

  public PointD set(PointD src, int r) {
    System.arraycopy(src.data, 0, data, 0, r);
    return this;
  }

  @Override
  public double get(int pos) {
    return data[pos];
  }

  public double get(Enum<?> pos) {
    return data[pos.ordinal()];
  }

  public double getLast() {
    return data[data.length - 1];
  }

  public void set(double val) {
    Arrays.fill(data, val);
  }

  @Override
  public void set(int pos, double val) {
    data[pos] = val;
  }

  public void set(Enum<?> pos, double val) {
    data[pos.ordinal()] = val;
  }

  public void add(int pos, double val) {
    data[pos] += val;
  }

  public void mul(int pos, double val) {
    data[pos] *= val;
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

  public PointD mul(double v) {
    PointD newPoint = new PointD(data);
    for (int i = 0; i < data.length; i++)
      newPoint.data[i] *= v;
    return newPoint;
  }

  public PointD mul(PointD v) {
    PointD newPoint = new PointD(data);
    for (int i = 0; i < data.length; i++)
      newPoint.data[i] *= v.data[i];
    return newPoint;
  }

  public PointD div(double v) {
    PointD newPoint = new PointD(data);
    for (int i = 0; i < data.length; i++)
      newPoint.data[i] /= v;
    return newPoint;
  }

  public PointD negative() {
    PointD newPoint = new PointD(size());
    for (int i = 0; i < data.length; i++)
      newPoint.data[i] = -data[i];
    return newPoint;
  }

  public PointD abs() {
    PointD newPoint = new PointD(size());
    for (int i = 0; i < data.length; i++)
      newPoint.data[i] = Math.abs(data[i]);
    return newPoint;
  }

  public PointD norm(int r) {
    return div(length(r));
  }

  public double length(int r) {
    return Math.sqrt(length2(r));
  }

  public double length2(int r) {
    double len = 0;
    if (r > size()) r = size();
    for (int i = 0; i < r; i++)
      len += data[i] * data[i];
    return len;
  }

  public int size() {
    return data.length;
  }

  @Override
  public String toString() {
    return "Point: " + Arrays.toString(data);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof PointD)) return false;
    return Arrays.equals(data, ((PointD) obj).data);
  }
}
