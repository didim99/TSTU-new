package ru.didim99.tstu.core.math.optimization;

import ru.didim99.tstu.core.math.common.Function;
import ru.didim99.tstu.core.math.common.FunctionRN;
import ru.didim99.tstu.core.math.common.Limit;
import ru.didim99.tstu.core.math.common.PointD;
import ru.didim99.tstu.core.math.common.PointRN;

/**
 * Created by didim99 on 21.10.19.
 */
class Functions {
  static final FunctionRN[] parabola = {
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

  private static final int T  = 0; // t
  private static final int X  = 1; // x(t)
  private static final int DX = 2; // x'(t)
  private static final int Y  = 3; // y(t)
  private static final int DY = 4; // y'(t)

  // Values definition: t, x1(t), x1'(t), x2(t), x2'(t), ...
  static final FunctionRN functional = x -> Math.pow(x.get(DX) + x.get(X), 2);

  // Values definition: t, x1(t), x2(t), ...
  static final PointD functionalStart = new PointD(1.0, 1.0);
  static final PointD functionalEnd = new PointD(2.0, 0.0);

  static final Function functionalRef = x -> Math.sinh(2 - x) / Math.sinh(1);

  static final Function eulerP = x -> 1; // Function p(t) of Euler equation
  static final Function eulerF = x -> 0; // Function f(t) of Euler equation

  private static double paraboloid(PointRN p, double a, double b,
                                   double c, double d, double alpha) {
    double x = p.get(0), y = p.get(1);
    return Math.pow((x - a) * Math.cos(alpha) + (y - b) * Math.sin(alpha), 2) / (c * c)
      + Math.pow((y - b) * Math.cos(alpha) - (x - a) * Math.sin(alpha), 2) / (d * d);
  }
}
