package ru.didim99.tstu.core.graphics.utils;

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

  private double data[];

  Vec4(Vec4 src) {
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

  void add(Vec4 v) {
    data[X] += v.data[X];
    data[Y] += v.data[Y];
    data[Z] += v.data[Z];
  }

  void div(double f) {
    data[X] /= f;
    data[Y] /= f;
    data[Z] /= f;
  }

  Vec4 multiply(Mat4 matrix) {
    Vec4 r = new Vec4(this);
    for (int i = 0; i < N; i++) {
      double tmp = 0;
      for (int j = 0; j < N; j++)
        tmp += data[j] * matrix.get(j, i);
      r.data[i] = tmp;
    }
    return r;
  }

  @Override
  public String toString() {
    return String.format(Locale.ROOT,
      "%7.3f %7.3f %7.3f %7.3f\n",
      data[0], data[1], data[2], data[3]);
  }
}
