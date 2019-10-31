package ru.didim99.tstu.core.optimization.math;

/**
 * Created by didim99 on 23.10.19.
 */
public class Limit implements FunctionRN {
  private static final double EPSILON = 10E-8;

  public enum Mode { LT, LE, EQ, GE, GT }

  private FunctionRN function;
  private Mode mode;

  public Limit(FunctionRN function, Mode mode) {
    this.function = function;
    this.mode = mode;
  }

  @Override
  public double f(PointD p) {
    switch (mode) {
      case LE:
      case LT: return -function.f(p);
      case GE:
      case GT: return function.f(p);
      default: return 0;
    }
  }

  public boolean check(PointD p) {
    switch (mode) {
      case LT: return function.f(p) < 0;
      case LE: return !(function.f(p) > 0);
      case EQ: return Math.abs(function.f(p)) < EPSILON;
      case GE: return !(function.f(p) < 0);
      case GT: return function.f(p) > 0;
      default: return true;
    }
  }
}
