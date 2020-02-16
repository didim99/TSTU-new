package ru.didim99.tstu.core.graphics.model;

import java.util.Arrays;
import java.util.Locale;

/**
 * 4x1 vector foe geometry operations
 * Created by didim99 on 09.03.19.
 */
public class Vec4 {
  private static final int N = 4;
  private static final int X = 0;
  private static final int Y = 1;
  private static final int Z = 2;

  private double[] data;

  public Vec4(Vec4 src) {
    data = Arrays.copyOf(src.data, src.data.length);
  }

  public Vec4(double x, double y, double z) {
    data = new double[] {x, y, z, 1};
  }

  public double x() { return data[X]; }
  public double y() { return data[Y]; }
  public double z() { return data[Z]; }

  public void x(double x) { data[X] = x; }
  public void y(double y) { data[Y] = y; }
  public void z(double z) { data[Z] = z; }

  public void set(double x, double y, double z) {
    data[X] = x;
    data[Y] = y;
    data[Z] = z;
  }

  public Vec4 add(Vec4 v) {
    data[X] += v.data[X];
    data[Y] += v.data[Y];
    data[Z] += v.data[Z];
    return this;
  }

  public Vec4 sub(Vec4 v) {
    data[X] -= v.data[X];
    data[Y] -= v.data[Y];
    data[Z] -= v.data[Z];
    return this;
  }

  public Vec4 multiply(double f) {
    data[X] *= f;
    data[Y] *= f;
    data[Z] *= f;
    return this;
  }

  void div(double f) {
    data[X] /= f;
    data[Y] /= f;
    data[Z] /= f;
  }

  public Vec4 multiply(Mat4 matrix) {
    Vec4 r = new Vec4(this);
    for (int i = 0; i < N; i++) {
      double tmp = 0;
      for (int j = 0; j < N; j++)
        tmp += data[j] * matrix.get(j, i);
      r.data[i] = tmp;
    }
    return r;
  }

  private double length() {
    return Math.sqrt(data[X] * data[X]
      + data[Y] * data[Y]
      + data[Z] * data[Z]);
  }

  private double multiplyScalar(Vec4 v) {
    return data[X] * v.data[X]
      + data[Y] * v.data[Y]
      + data[Z] * v.data[Z];
  }

  double calcAngle(Vec4 v) {
    v = new Vec4(v);
    v.sub(this);
    return multiplyScalar(v)
      / (length() * v.length());
  }

  @Override
  public String toString() {
    return String.format(Locale.ROOT,
      "%7.3f %7.3f %7.3f %7.3f\n",
      data[0], data[1], data[2], data[3]);
  }
}
