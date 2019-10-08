package ru.didim99.tstu.core.optimization;

import java.util.Random;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 02.10.19.
 */
class PaulMethod implements OptTask.ExtremaFinderR2 {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Paul";

  private static final double EPSILON = 0.001;
  private static final double START_MIN = -10.0;
  private static final double START_MAX = 10.0;
  private static final double DEFAULT_STEP = 0.1;

  private double step = DEFAULT_STEP;

  @Override
  public Result find(FunctionR2 fun) {
    Result result = new Result();
    Random random = new Random();

    PointD start = new PointD(
      Utils.randInRangeD(random, START_MIN, START_MAX),
      Utils.randInRangeD(random, START_MIN, START_MAX)
    );

    boolean done = false;
    while (!done) {
      try {
        PointD next = step(fun, start);
        MyLog.d(LOG_TAG, "start: " + start
          + " next: " + next + " d: " + next.sub(start));
        if (next.sub(start).isZero(EPSILON))
          done = true;
        start.set(next);
      } catch (IllegalStateException e) {
        MyLog.d(LOG_TAG, "Correcting step: " + step + " to " + step / 2);
        step /= 2;
      }
    }

    result.setSolution(start);
    return result;
  }

  private PointD step(FunctionR2 fun, PointD start) {
    double f1 = fun.f(start.get(0), start.get(1)), f2;
    PointD vec = findVector(fun, start).sub(start);
    PointD p1 = new PointD(start);
    PointD p2 = new PointD(start);

    double df = -1;
    while (df < 0) {
      p2 = p2.add(vec);
      f2 = fun.f(p2.get(0), p2.get(1));
      df = f2 - f1;
      if (df < 0)
        p1.set(p2);
      f1 = f2;
    }

    return p1;
  }

  private PointD findVector(FunctionR2 fun, PointD start) {
    double f1 = fun.f(start.get(0), start.get(1)), f2;
    PointD p1 = new PointD(start);
    PointD p2 = new PointD(start);
    int totalSteps = 0;

    for (int i = 0; i < 2; i++) {
      boolean first = true;
      double step = this.step;
      double df = -1;
      int steps = 0;
      p2.set(p1);

      while (df < 0) {
        p2.add(i, step);
        f2 = fun.f(p2.get(0), p2.get(1));
        df = f2 - f1;
        f1 = f2;

        if (df < 0) {
          p1.set(i, p2.get(i));
          steps++;
        }

        if (first) {
          first = false;
          if (df > 0) {
            step = -step;
            df = -df;
            steps--;
          }
        }
      }

      if (steps > 0)
        totalSteps++;
    }

    if (totalSteps == 0)
      throw new IllegalStateException("Can't do step");
    return p1;
  }
}
