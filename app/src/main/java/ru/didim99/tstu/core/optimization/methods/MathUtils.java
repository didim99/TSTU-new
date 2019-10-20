package ru.didim99.tstu.core.optimization.methods;

import ru.didim99.tstu.core.optimization.FunctionR2;
import ru.didim99.tstu.core.optimization.PointD;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 20.10.19.
 */
class MathUtils {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MUtils";

  private static final double GOLD_FACTOR = 0.382;
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

  static PointD minimize(FunctionR2 fun, PointD start, PointD dir) {
    PointD test = minimize2(fun, start, dir);
    PointD end = new PointD(start);
    PointD tmp = new PointD(start);

    double df = -1;
    boolean first = true;
    while (df < 0) {
      start.set(tmp);
      tmp.set(end);
      end = end.add(dir);
      calcF(fun, end);
      df = end.get(2) - tmp.get(2);

      if (first) {
        first = false;
        if (df > 0) {
          start.set(end);
          tmp.set(end);
          dir = dir.negative();
          df = -df;
        }
      }
    }

    PointD delta = end.sub(start).mult(GOLD_FACTOR);
    PointD newStart = start.add(delta);
    PointD newEnd = end.sub(delta);
    calcF(fun, newStart);
    calcF(fun, newEnd);

    while (end.sub(start).length2(2) > EPSILON) {
      if (newStart.get(2) > newEnd.get(2))
        start.set(newStart);
      else
        end.set(newEnd);

      delta = end.sub(start).mult(GOLD_FACTOR);
      if (newStart.get(2) > newEnd.get(2)) {
        newStart.set(newEnd);
        newEnd = end.sub(delta);
        calcF(fun, newEnd);
      } else {
        newEnd.set(newStart);
        newStart = start.add(delta);
        calcF(fun, newStart);
      }
    }

    PointD min = start.add(end).div(2);
    calcF(fun, min);

    MyLog.v(LOG_TAG, " ===== delta: " + min.sub(test).length(2)
      + " df: " + (min.get(2) - test.get(2)));
    return test;
  }

  static PointD minimize2(FunctionR2 function, PointD start, PointD dir) {
    double factor = STEP, delta = EPSILON, df;
    PointD prev = new PointD(start);

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
