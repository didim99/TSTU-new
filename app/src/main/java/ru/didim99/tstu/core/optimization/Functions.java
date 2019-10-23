package ru.didim99.tstu.core.optimization;

/**
 * Created by didim99 on 21.10.19.
 */
class Functions {
  private static final double PA = -4;
  private static final double PB = -5;
  private static final double PC = -3;
  private static final double PD = -5;
  private static final double PALPHA = Math.toRadians(115);

  private static final double PA2 = 4;
  private static final double PB2 = 4;
  private static final double PC2 = 2;
  private static final double PD2 = 3;
  private static final double PALPHA2 = Math.toRadians(55);

  static FunctionR2 parabola = (p) -> paraboloid(p, PA, PB, PC, PD, PALPHA);
  static FunctionR2 parabola2 = (p) -> paraboloid(p, PA2, PB2, PC2, PD2, PALPHA2);

  static FunctionR2 resenbrok = (p) -> {
    double x = p.get(0), y = p.get(1);
    return 100 * Math.pow(y - x * x, 2) + Math.pow(1 - x, 2);
  };

  static Limit[] limits = {
    new Limit(p -> p.get(0) - 4 / p.get(1), Limit.Mode.LE),
    new Limit(p -> p.get(0), Limit.Mode.GE)
  };

  private static double paraboloid(PointD p, double a, double b,
                                   double c, double d, double alpha) {
    double x = p.get(0), y = p.get(1);
    return Math.pow((x - a) * Math.cos(alpha) + (y - b) * Math.sin(alpha), 2) / (c * c)
      + Math.pow((y - b) * Math.cos(alpha) - (x - a) * Math.sin(alpha), 2) / (d * d);
  }
}
