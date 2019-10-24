package ru.didim99.tstu.core.optimization.methods;

import java.util.ArrayList;
import java.util.Random;

import ru.didim99.tstu.core.optimization.math.FunctionRN;
import ru.didim99.tstu.core.optimization.math.PointD;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 02.10.19.
 */
public class PaulMethod extends ExtremaFinderRN {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Paul";

  private static final double EPSILON = 0.0001;
  private static final double START_MIN = -10.0;
  private static final double START_MAX = 10.0;
  private static final double DEFAULT_STEP = 0.1;

  private double step = DEFAULT_STEP;

  @Override
  public PointD find(FunctionRN fun) {
    series = new ArrayList<>();
    Random random = new Random();

    PointD start = new PointD(
      Utils.randInRangeD(random, START_MIN, START_MAX),
      Utils.randInRangeD(random, START_MIN, START_MAX)
    );

    boolean done = false;
    while (!done) {
      try {
        series.add(new PointD(start));
        PointD next = step(fun, start);
        if (next.sub(start).isZero(EPSILON))
          done = true;
        start.set(next);
      } catch (IllegalStateException e) {
        MyLog.d(LOG_TAG, "Correcting step down: " + step + " to " + step / 2);
        step /= 2;
      }
    }

    series.add(new PointD(start));
    solution = new PointD(start.get(0), start.get(1), fun.f(start));
    solutionSteps = series.size() - 1;
    MyLog.d(LOG_TAG, "Solved in " + solutionSteps + " iterations");
    return solution;
  }

  private PointD step(FunctionRN fun, PointD start) {
    double f1 = fun.f(start), fs = f1, f2;
    PointD vec = findVector(fun, start).sub(start);
    PointD p1 = new PointD(start);
    PointD p2 = new PointD(start);

    int steps = 0;
    double df = -1;
    while (df < 0) {
      p2 = p2.add(vec);
      f2 = fun.f(p2);
      df = f2 - f1;
      if (df < 0) {
        p1.set(p2);
        f1 = f2;
        steps++;
      }
    }

    MyLog.v(LOG_TAG, "delta: " + (fs - f1) + " steps: " + steps);

    if (steps > 10) {
      MyLog.d(LOG_TAG, "Correcting step up: " + step + " to " + step * 2);
      step *= 2;
    }

    return p1;
  }

  private PointD findVector(FunctionRN fun, PointD start) {
    double f1 = fun.f(start), fs = f1, f2;
    PointD p1 = new PointD(start);
    PointD p2 = new PointD(start);
    int totalSteps = 0;

    for (int i = 0; i < start.size(); i++) {
      boolean first = true;
      double step = this.step;
      double df = -1;
      int steps = 0;
      p2.set(p1);

      while (df < 0) {
        p2.add(i, step);
        f2 = fun.f(p2);
        df = f2 - f1;

        if (df < 0) {
          p1.set(i, p2.get(i));
          f1 = f2;
          steps++;
        }

        if (first) {
          first = false;
          if (df > 0) {
            step = -step;
            df = -df;
            f1 = f2;
            steps--;
          }
        }
      }

      if (steps > 0)
        totalSteps += steps;
    }

    if (totalSteps == 0)
      throw new IllegalStateException("Can't do step");
    MyLog.v(LOG_TAG, "delta: " + (fs - f1) + " steps: "
      + totalSteps + " vector: " + p1.sub(start));
    return p1;
  }
}
