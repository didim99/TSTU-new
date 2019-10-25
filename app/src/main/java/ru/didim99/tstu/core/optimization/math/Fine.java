package ru.didim99.tstu.core.optimization.math;

/**
 * Created by didim99 on 24.10.19.
 */
public class Fine implements FunctionRN {
  private static final double FACTOR_START = 1;
  private static final double FACTOR_STEP = 10;

  public enum Type { INTERNAL, EXTERNAL }

  private final Type type;
  private final Function function;
  private final Limit[] limits;
  private double factor;

  public Fine(Type type, Limit... limits) {
    this.type = type;
    this.limits = limits;
    this.factor = FACTOR_START;

    switch (type) {
      case INTERNAL: this.function = x -> 1 / x; break;
      case EXTERNAL: this.function = x -> Math.pow(Math.min(0, x), 2); break;
      default: throw new IllegalArgumentException("Unknown fine type");
    }
  }

  @Override
  public double f(PointD p) {
    double res = 0;
    for (Limit limit : limits)
      res += function.f(limit.f(p));
    return res * factor;
  }

  public boolean check(PointD p) {
    for (Limit limit : limits)
      if (!limit.check(p)) return false;
    return true;
  }

  public void increaseFactor() {
    switch (type) {
      case INTERNAL: this.factor /= FACTOR_STEP; break;
      case EXTERNAL: this.factor *= FACTOR_STEP; break;
    }
  }
}
