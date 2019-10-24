package ru.didim99.tstu.core.optimization.math;

/**
 * Created by didim99 on 24.10.19.
 */
public class Fine implements FunctionRN {

  public enum Type { INTERNAL, EXTERNAL }

  private final Type type;
  private final Function function;
  private final Limit[] limits;
  private double factor;

  public Fine(Type type, double factor, Limit... limits) {
    this.type = type;
    this.factor = factor;
    this.limits = limits;

    switch (type) {
      case INTERNAL: this.function = x -> 1 / x; this.factor = 1 / factor; break;
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

  public Type getFineType() {
    return type;
  }
}
