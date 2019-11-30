package ru.didim99.tstu.core.optimization.math;

import java.util.Arrays;

/**
 * Created by didim99 on 24.10.19.
 */
public class Fine implements FunctionRN {
  private static final double FACTOR_START = 1;
  private static final double FACTOR_STEP = 5;

  public enum Type { INTERNAL, EXTERNAL, COMBINED }

  private final Type type;
  private final Limit[] limits;
  private final Function[] functions;
  private final double[] factors;

  public Fine(Type type, Limit... limits) {
    this.type = type;
    this.limits = limits;
    this.functions = new Function[limits.length];
    this.factors = new double[limits.length];
    Arrays.fill(factors, FACTOR_START);

    for (int i = 0; i < limits.length; i++) {
      switch (type) {
        case INTERNAL:
          this.functions[i] = x -> 1 / x;
          break;
        case EXTERNAL:
          this.functions[i] = x -> Math.pow(Math.min(0, x), 2);
          break;
        case COMBINED:
          this.functions[i] = limits[i].isEquality() ? x -> x * x : Math::log;
          if (!limits[i].isEquality()) this.factors[i] = -FACTOR_START;
          break;
        default: throw new IllegalArgumentException("Unknown fine type");
      }
    }
  }

  @Override
  public double f(PointD p) {
    double res = 0;
    for (int i = 0; i < limits.length; i++)
      res += functions[i].f(limits[i].f(p)) * factors[i];
    return res;
  }

  public boolean checkStartPoint(PointD p) {
    for (Limit limit : limits) {
      if (limit.isEquality()) continue;
      if (!limit.check(p)) return false;
    }

    return true;
  }

  public void increaseFactor() {
    for (int i = 0; i < limits.length; i++) {
      switch (type) {
        case INTERNAL:
          factors[i] /= FACTOR_STEP;
          break;
        case EXTERNAL:
          factors[i] *= FACTOR_STEP;
          break;
        case COMBINED:
          factors[i] *= limits[i].isEquality() ? FACTOR_STEP : 1 / FACTOR_STEP;
          break;
        default: throw new IllegalArgumentException("Unknown fine type");
      }
    }
  }
}
