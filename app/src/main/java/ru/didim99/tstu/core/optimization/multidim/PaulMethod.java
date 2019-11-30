package ru.didim99.tstu.core.optimization.multidim;

import ru.didim99.tstu.core.optimization.math.FunctionRN;
import ru.didim99.tstu.core.optimization.math.PointD;
import ru.didim99.tstu.utils.MyLog;

import static ru.didim99.tstu.core.optimization.multidim.MathUtils.calcF;

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
    boolean done = false;
    while (!done) {
      try {
        series.add(new PointD(start));
        PointD next = step(fun, start);
        if (next.sub(start).length(2) < EPSILON)
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
    int n = start.size() - 1;

    int steps = 0;
    double df = -1;
    while (df < 0) {
      p2 = p2.add(vec);
      calcF(fun, p2);
      df = p2.get(n) - p1.get(n);
      if (df < 0) {
        p1.set(p2);
        steps++;
      }
    }

    MyLog.v(LOG_TAG, "delta: " + (start.get(n) - p1.get(n)) + " steps: " + steps);

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
    int n = start.size() - 1;
    int totalSteps = 0;

    for (int i = 0; i < n; i++) {
      boolean first = true;
      double step = this.step;
      double df = -1;
      int steps = 0;
      p2.set(p1);

      while (df < 0) {
        p2.add(i, step);
        calcF(fun, p2);
        df = p2.get(n) - p1.get(n);

        if (df < 0) {
          p1.set(i, p2.get(i));
          p1.set(n, p2.get(n));
          steps++;
        }

        if (first) {
          first = false;
          if (df > 0) {
            p1.set(n, p2.get(n));
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
    MyLog.v(LOG_TAG, "delta: " + (start.get(n) - p1.get(n)) + " steps: "
      + totalSteps + " vector: " + p1.sub(start));
    return p1;
  }
}
