package ru.didim99.tstu.core.math.optimization.multidim;

import ru.didim99.tstu.core.math.common.FunctionRN;
import ru.didim99.tstu.core.math.common.PointD;
import ru.didim99.tstu.utils.MyLog;

import static ru.didim99.tstu.core.math.optimization.multidim.MathUtils.calcF;

/**
 * Created by didim99 on 02.10.19.
 */
public class PaulMethod extends ExtremaFinderRN {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Paul";

  private static final double EPSILON = 0.0001;
  private static final double DEFAULT_STEP = 0.01;

  private double step = DEFAULT_STEP;

  @Override
  public PointD find(FunctionRN fun, PointD start) {
    int r = start.size() - 1;
    boolean done = false;
    while (!done) {
      try {
        series.add(new PointD(start));
        PointD next = step(fun, start);
        if (next.sub(start).length(r) < EPSILON)
          done = true;
        start.set(next);
      } catch (IllegalStateException e) {
        MyLog.d(LOG_TAG, "Correcting step down: " + step + " to " + step / 2);
        step /= 2;
      }
    }

    series.add(new PointD(start));
    solution = start;
    solutionSteps = series.size() - 1;
    MyLog.d(LOG_TAG, "Solved in " + solutionSteps + " iterations");
    return solution;
  }

  private PointD step(FunctionRN fun, PointD start) {
    calcF(fun, start);
    PointD vec = findVector(fun, start).sub(start);
    PointD p1 = new PointD(start);
    PointD p2 = new PointD(start);
    int r = start.size() - 1;

    int steps = 0;
    double df = -1;
    while (df < 0) {
      p2 = p2.add(vec);
      calcF(fun, p2);
      df = p2.get(r) - p1.get(r);
      if (df < 0) {
        p1.set(p2);
        steps++;
      }
    }

    MyLog.v(LOG_TAG, "delta: " + (start.get(r) - p1.get(r)) + " steps: " + steps);

    if (steps > 10) {
      MyLog.d(LOG_TAG, "Correcting step up: " + step + " to " + step * 2);
      step *= 2;
    }

    return p1;
  }

  private PointD findVector(FunctionRN fun, PointD start) {
    calcF(fun, start);
    PointD p1 = new PointD(start);
    PointD p2 = new PointD(start);
    int r = start.size() - 1;
    int totalSteps = 0;

    for (int i = 0; i < r; i++) {
      boolean first = true;
      double step = this.step;
      double df = -1;
      int steps = 0;
      p2.set(p1);

      while (df < 0) {
        p2.add(i, step);
        calcF(fun, p2);
        df = p2.get(r) - p1.get(r);

        if (df < 0) {
          p1.set(i, p2.get(i));
          p1.set(r, p2.get(r));
          steps++;
        }

        if (first) {
          first = false;
          if (df > 0) {
            p1.set(r, p2.get(r));
            step = -step;
            df = -df;
            steps--;
          }
        }
      }

      if (steps > 0)
        totalSteps += steps;
    }

    if (totalSteps == 0)
      throw new IllegalStateException("Can't do step");
    MyLog.v(LOG_TAG, "delta: " + (start.get(r) - p1.get(r)) + " steps: "
      + totalSteps + " vector: " + p1.sub(start));
    return p1;
  }
}
