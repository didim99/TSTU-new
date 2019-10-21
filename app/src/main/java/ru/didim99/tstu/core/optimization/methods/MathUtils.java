package ru.didim99.tstu.core.optimization.methods;

import ru.didim99.tstu.core.optimization.FunctionR2;
import ru.didim99.tstu.core.optimization.PointD;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 20.10.19.
 */
class MathUtils {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MUtils";
  
  private static final double EPSILON = 0.001;
  private static final double STEP = 1.0;

  static PointD gradient(FunctionR2 fun, PointD p, double gStep) {
    PointD g = new PointD(3);
    PointD p2 = new PointD(p);
    calcF(fun, p);

    for (int i = 0; i < p.size() - 1; i++) {
      p2.set(p).add(i, gStep);
      g.set(i, (fun.f(p2) - p.get(2)) / gStep);
    }

    return g;
  }

  static PointD minimize(FunctionR2 function, PointD start, PointD dir) {
    calcF(function, start);
    double factor = STEP, delta = EPSILON, df;
    PointD prev = new PointD(start);
    if (dir.length(2) > 1.0)
      dir = dir.norm(2);

    int steps = 0, totalSteps = 0;
    while (delta > EPSILON * EPSILON) {
      PointD next = prev.add(dir.mult(factor));
      calcF(function, next);
      delta = next.sub(prev).length(2);
      df = next.get(2) - prev.get(2);
      /*MyLog.v(LOG_TAG, "steps: " + steps + " delta: " + delta
        + " f: " + next.get(2) + " df: " + df);*/
      if (df < 0) {
        prev.set(next);
        steps++;
      } else {
        factor /= 2;
        if (steps > 0) {
          prev.set(next);
          dir = dir.negative();
          totalSteps += steps;
          steps = 0;
        }
      }
    }

    if (totalSteps == 0)
      throw new IllegalStateException("Can't do step");

    return prev;
  }

  static void calcF(FunctionR2 fun, PointD p) {
    p.set(2, fun.f(p));
  }
}
