package ru.didim99.tstu.core.optimization;

import ru.didim99.tstu.core.optimization.math.Function;
import ru.didim99.tstu.core.optimization.math.FunctionRN;
import ru.didim99.tstu.core.optimization.math.Limit;
import ru.didim99.tstu.core.optimization.math.PointD;

/**
 * Created by didim99 on 21.10.19.
 */
class Functions {
  static final FunctionRN[] parabola = new FunctionRN[] {
    p -> paraboloid(p, -4, -5, 3, 5, Math.toRadians(115)),
    p -> paraboloid(p, 4, 4, 2, 3, Math.toRadians(55)),
    p -> paraboloid(p, -2, -1, 2, 3, Math.toRadians(125))
  };

  static final FunctionRN resenbrok = p -> {
    double x = p.get(0), y = p.get(1);
    return 100 * Math.pow(y - x * x, 2) + Math.pow(1 - x, 2);
  };

  static final Limit[] limitsInEq = {
    new Limit(p -> p.get(0) - 4 / p.get(1), Limit.Mode.LE),
    new Limit(p -> p.get(0), Limit.Mode.GE)
  };

  static final Limit[] limitsEq = {
    new Limit(p -> p.get(1) * p.get(1) - p.get(0), Limit.Mode.EQ),
    new Limit(p -> Math.pow(p.get(1), 5) + 3 * p.get(0) * p.get(0)
      - 2 * p.get(0) - 5, Limit.Mode.LE)
  };

  static final PointD eulerStart = new PointD(1.0, 1.0);
  static final PointD eulerEnd = new PointD(2.0, 0.0);
  static Function eulerP = x -> 1;
  static Function eulerF = x -> 0;

  static final Function checkEuler = x -> Math.sinh(2 - x) / Math.sinh(1);

  private static double paraboloid(PointD p, double a, double b,
                                   double c, double d, double alpha) {
    double x = p.get(0), y = p.get(1);
    return Math.pow((x - a) * Math.cos(alpha) + (y - b) * Math.sin(alpha), 2) / (c * c)
      + Math.pow((y - b) * Math.cos(alpha) - (x - a) * Math.sin(alpha), 2) / (d * d);
  }
}
