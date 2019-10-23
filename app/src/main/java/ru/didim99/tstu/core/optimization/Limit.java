package ru.didim99.tstu.core.optimization;

/**
 * Created by didim99 on 23.10.19.
 */
public class Limit implements FunctionR2 {

  public enum Mode { LT, LE, GE, GT }

  private FunctionR2 function;
  private Mode mode;

  public Limit(FunctionR2 function, Mode mode) {
    this.function = function;
    this.mode = mode;
  }

  @Override
  public double f(PointD p) {
    return function.f(p);
  }

  public boolean check(PointD p) {
    switch (mode) {
      case LT: return function.f(p) < 0;
      case LE: return !(function.f(p) > 0);
      case GE: return !(function.f(p) < 0);
      case GT: return function.f(p) > 0;
      default: return true;
    }
  }
}
