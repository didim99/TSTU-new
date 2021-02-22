package ru.didim99.tstu.core.math.optimization.multidim;

import ru.didim99.tstu.core.math.common.FunctionRN;
import ru.didim99.tstu.core.math.common.PointD;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 20.10.19.
 */
class MathUtils {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MUtils";

  static final double EPSILON = 0.001;
  private static final double G_STEP = 10E-8;
  private static final double STEP = 0.1;
  private static final double STEP_FACTOR = 0.1;

  private static double simpleStep = STEP;

  static PointD gradient(FunctionRN fun, PointD p) {
    int r = p.size() - 1;
    PointD g = new PointD(r + 1);
    PointD p2 = new PointD(p);
    calcF(fun, p);

    for (int i = 0; i <= r; i++) {
      p2.set(p).add(i, G_STEP);
      g.set(i, (fun.f(p2) - p.get(r)) / G_STEP);
    }

    return g;
  }

  static PointD minimize(FunctionRN function, PointD start, PointD dir) {
    int r = start.size() - 1;
    calcF(function, start);
    double factor = simpleStep, df;
    PointD prev = new PointD(start);
    if (dir.length(r) > 1.0)
      dir = dir.norm(r);

    boolean done = false, reversed = false;
    int steps = 0, totalSteps = 0;
    while (!done) {
      PointD next = prev.add(dir.mul(factor));
      calcF(function, next);
      df = next.get(r) - prev.get(r);
      done = next.sub(prev).length(r) < EPSILON * EPSILON;
      /*MyLog.v(LOG_TAG, "steps: " + steps + " delta: " + delta
        + " f: " + next.get(2) + " df: " + df);*/
      if (df < 0) {
        prev.set(next);
        steps++;
      } else {
        factor /= 2;
        if (done && steps == 0) {
          factor = simpleStep;
          dir = dir.negative();
          done = reversed;
          reversed = true;
        }

        totalSteps += steps;
        steps = 0;
      }
    }

    if (totalSteps == 0)
      throw new IllegalStateException("Can't do step");

    return prev;
  }

  static void calcF(FunctionRN fun, PointD p) {
    p.set(p.size() - 1, fun.f(p));
  }

  static void resetStep() {
    simpleStep = STEP;
  }

  static void correctStep() {
    simpleStep *= STEP_FACTOR;
  }
}
