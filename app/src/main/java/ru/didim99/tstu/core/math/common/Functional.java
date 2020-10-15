package ru.didim99.tstu.core.math.common;

import java.util.ArrayList;

/**
 * Created by didim99 on 01.12.19.
 */
public class Functional implements FunctionRN {
  private static final int T = 0;

  private FunctionRN j;
  private PointD start, end;
  private int xCount, resolution;
  private double dt;

  public Functional(FunctionRN j) {
    this.j = j;
  }

  public void setBounds(PointD start, PointD end) {
    this.xCount = start.size() - 1;
    this.start = start;
    this.end = end;
  }

  public void setResolution(int res) {
    if (start == null || end == null)
      throw new IllegalStateException("Bounds must be set first");
    if (res <= 0)
      throw new IllegalArgumentException("res must be > 0");
    this.dt = (end.get(T) - start.get(T)) / (res + 1);
    this.resolution = res;
  }

  @Override
  public double f(PointRN p) {
    double[] jValues = new double[resolution + 1];
    PointD[] xValues = new PointD[xCount];

    for (int k = 1, idx = 0; k <= xCount; k++, idx++) {
      xValues[idx] = new PointD(resolution + 2);
      xValues[idx].set(0, start.get(k));
      for (int i = 0; i < resolution; i++)
        xValues[idx].set(i + 1, p.get(offset(k) + i));
      xValues[idx].set(resolution + 1, end.get(k));
    }

    double res = 0, t = start.get(T);
    PointD tmp = new PointD(xCount * 2 + 1), x;
    for (int i = 0; i <= resolution; i++) {
      tmp.set(T, t);
      for (int k = 1; k <= xCount; k++) {
        x = xValues[k - 1];
        tmp.set(k * 2 - 1, x.get(i));
        tmp.set(k * 2, (x.get(i + 1) - x.get(i)) / dt);
      }

      jValues[i] = j.f(tmp);
      if (i > 0) res += (jValues[i - 1] + jValues[i]) / 2 * dt;
      t += dt;
    }

    return res;
  }

  public PointD getStartValues() {
    if (start == null || end == null)
      throw new IllegalStateException("Bounds must be set first");
    if (resolution == 0)
      throw new IllegalStateException("Resolution must be set first");
    PointD p = new PointD(resolution * xCount + 1);
    for (int k = 1; k <= xCount; k++) {
      double x = start.get(k);
      double dx = (end.get(k) - x) / (resolution + 1);
      for (int i = 0; i < resolution; i++)
        p.set(offset(k) + i, x += dx);
    }

    return p;
  }

  public ArrayList<PointD> getSeries(PointD internal) {
    ArrayList<PointD> series = new ArrayList<>();
    series.add(new PointD(start));

    double t = start.get(T);
    for (int i = 0; i < resolution; i++) {
      PointD p = new PointD(xCount + 1);
      p.set(T, t += dt);
      for (int k = 1; k <= xCount; k++)
        p.set(k, internal.get(offset(k) + i));
      series.add(p);
    }

    series.add(new PointD(end));
    return series;
  }

  private int offset(int k) {
    return resolution * (k - 1);
  }
}
