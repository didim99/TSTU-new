package ru.didim99.tstu.core.graphics.utils;

import java.util.Locale;

/**
 * 4x4 matrix for geometry operations
 * Created by didim99 on 09.03.19.
 */
public class Mat4 {
  private static final int N = 4;

  private double data[];

  public Mat4() {
    data = new double[16];
    loadIdentity();
  }

  public double get(int i, int j) {
    return data[i*4 + j];
  }

  public void set(int i, int j, double v) {
    data[i*4 + j] = v;
  }

  public void multiply(Mat4 m) {
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < N; j++) {
        double tmp = 0;
        for (int k = 0; k < N; k++)
          tmp += get(i, k) * m.get(k, j);
        set(i, j, tmp);
      }
    }
  }

  public void translate(Vec4 translate) {
    Mat4 tmp = new Mat4();
    tmp.loadTranslate(translate);
    multiply(tmp);
  }

  public void scale(Vec4 scale) {
    Mat4 tmp = new Mat4();
    tmp.loadScale(scale);
    multiply(tmp);
  }

  public void rotate(Vec4 rotate) {
    Mat4 tmp = new Mat4();
    tmp.loadRotateX(rotate.x());
    multiply(tmp);
    tmp.loadIdentity();
    tmp.loadRotateY(rotate.y());
    multiply(tmp);
    tmp.loadIdentity();
    tmp.loadRotateZ(rotate.z());
    multiply(tmp);
  }

  private void loadTranslate(Vec4 translate) {
    data[12] = translate.x();
    data[13] = translate.y();
    data[14] = translate.z();
  }

  private void loadScale(Vec4 scale) {
    data[0] = scale.x();
    data[5] = scale.y();
    data[10] = scale.z();
  }

  private void loadRotateX(double angle) {
    double s = Math.sin(Math.toRadians(angle));
    double c = Math.cos(Math.toRadians(angle));
    data[5] = c; data[6] = s;
    data[9] = -s; data[10] = c;
  }

  private void loadRotateY(double angle) {
    double s = Math.sin(Math.toRadians(angle));
    double c = Math.cos(Math.toRadians(angle));
    data[0] = c; data[2] = -s;
    data[8] = s; data[10] = c;
  }

  private void loadRotateZ(double angle) {
    double s = Math.sin(Math.toRadians(angle));
    double c = Math.cos(Math.toRadians(angle));
    data[0] = c; data[1] = s;
    data[4] = -s; data[5] = c;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 4; i++)
      sb.append(String.format(Locale.ROOT,
        "\n%7.3f %7.3f %7.3f %7.3f",
        data[4 * i], data[ 4 * i + 1],
        data[4 * i + 2], data[4 * i + 3]));
    return sb.toString();
  }

  public void loadIdentity() {
    data[0] = 1; data[1] = 0; data[2] = 0; data[3] = 0;
    data[4] = 0; data[5] = 1; data[6] = 0; data[7] = 0;
    data[8] = 0; data[9] = 0; data[10] = 1; data[11] = 0;
    data[12] = 0; data[13] = 0; data[14] = 0; data[15] = 1;
  }
}
