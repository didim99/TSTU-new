package ru.didim99.tstu.core.optimization.math;

/**
 * Created by didim99 on 01.12.19.
 */
public class Functional implements FunctionRN {

  private MultiFunction j;

  public Functional(MultiFunction j) {
    this.j = j;
  }

  @Override
  public double f(PointD p) {
    return j.f(p.get(0), this::x, this::dx);
  }

  private double x(double t) {
    return t; // STUB
  }

  private double dx(double t) {
    return x(t); // STUB
  }
}
