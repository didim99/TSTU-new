package ru.didim99.tstu.core.optimization.math;

import java.util.ArrayList;

/**
 * Created by didim99 on 01.12.19.
 */
public class Functional implements FunctionRN {
  private static final int T = 0;
  private static final int X = 1;

  private FunctionRN j;
  private PointD start, end;
  private int resolution;
  private double dt;

  public Functional(FunctionRN j) {
    this.j = j;
  }

  public void setBounds(PointD start, PointD end) {
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
  public double f(PointD p) {
    PointD tmp = new PointD(resolution + 1);
    PointD p2 = new PointD(resolution + 2);

    p2.set(0, start.get(X));
    for (int i = 1; i <= resolution; i++)
      p2.set(i, p.get(i - 1));
    p2.set(resolution + 1, end.get(X));

    double res = 0;
    for (int i = 0; i <= resolution; i++) {
      tmp.set(i, j.f(new PointD(p2.get(i),
        (p2.get(i + 1) - p2.get(i)) / dt)));
      if (i > 0) res += (tmp.get(i - 1)
        + tmp.get(i)) / 2 * dt;
    }

    return res;
  }

  public PointD getStartValues() {
    if (start == null || end == null)
      throw new IllegalStateException("Bounds must be set first");
    if (resolution == 0)
      throw new IllegalStateException("Resolution must be set first");
    PointD start = new PointD(resolution + 1);
    double dx = (end.get(X) - start.get(X)) / (resolution + 1);
    double x = start.get(X);
    for (int i = 0; i < resolution; i++)
      start.set(i, x += dx);
    return start;
  }

  public ArrayList<PointD> getSeries(PointD internal) {
    ArrayList<PointD> series = new ArrayList<>();
    series.add(new PointD(start));
    double t = start.get(T);
    for (int i = 0; i < resolution; i++)
      series.add(new PointD(t += dt, internal.get(i)));
    series.add(new PointD(end));
    return series;
  }
}
