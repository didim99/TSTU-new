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
    Mat4 res = new Mat4();
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < N; j++) {
        double tmp = 0;
        for (int k = 0; k < N; k++)
          tmp += get(i, k) * m.get(k, j);
        res.set(i, j, tmp);
      }
    }
    load(res);
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
    tmp.loadRotate(rotate);
    multiply(tmp);
  }

  public void loadIdentity() {
    data[0] = 1; data[1] = 0; data[2] = 0; data[3] = 0;
    data[4] = 0; data[5] = 1; data[6] = 0; data[7] = 0;
    data[8] = 0; data[9] = 0; data[10] = 1; data[11] = 0;
    data[12] = 0; data[13] = 0; data[14] = 0; data[15] = 1;
  }

  public void load(Mat4 src) {
    System.arraycopy(src.data, 0, data, 0, data.length);
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

  private void loadRotate(Vec4 rotate) {
    double rx = Math.toRadians(rotate.x());
    double ry = Math.toRadians(rotate.y());
    double rz = Math.toRadians(rotate.z());
    double sx = Math.sin(rx);
    double cx = Math.cos(rx);
    double sy = Math.sin(ry);
    double cy = Math.cos(ry);
    double sz = Math.sin(rz);
    double cz = Math.cos(rz);
    double ss = sx * sy;
    double cs = cx * sy;

    data[0]   =  cy * cz;
    data[1]   = -cy * sz;
    data[2]   = -sy;
    data[4]   = -ss * cz + cx * sz;
    data[5]   =  ss * sz + cx * cz;
    data[6]   = -sx * cy;
    data[8]   =  cs * cz + sx * sz;
    data[9]   = -cs * sz + sx * cz;
    data[10]  =  cx * cy;
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
}
