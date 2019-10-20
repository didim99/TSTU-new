package ru.didim99.tstu.core.optimization.methods;

import java.util.ArrayList;
import java.util.Random;
import ru.didim99.tstu.core.optimization.ExtremaFinderR2;
import ru.didim99.tstu.core.optimization.FunctionR2;
import ru.didim99.tstu.core.optimization.PointD;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

import static ru.didim99.tstu.core.optimization.methods.MathUtils.*;

/**
 * Created by didim99 on 17.10.19.
 */
public class GradientMethod extends ExtremaFinderR2 {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_CGM";

  private static final double EPSILON = 0.001;
  private static final double START_MIN = -2.0;
  private static final double START_MAX = 2.0;
  private static final double G_STEP = 10E-8;

  @Override
  public PointD find(FunctionR2 fun) {
    series = new ArrayList<>();
    Random random = new Random();

    PointD start = new PointD(
      Utils.randInRangeD(random, START_MIN, START_MAX),
      Utils.randInRangeD(random, START_MIN, START_MAX), 0);
    calcF(fun, start);

    PointD gPrev = null, gNext;
    PointD sPrev = null, sNext;
    double beta;

    while (true) {
      series.add(new PointD(start));
      gNext = gradient(fun, start, G_STEP);
      sNext = gNext.negative();
      if (sPrev != null) {
        beta = gNext.length2(2) / gPrev.length2(2);
        sNext = sNext.add(sPrev.mult(beta));
      }

      double delta = gNext.length(2);
      MyLog.v(LOG_TAG, "Point: " + start + " gradient: " + gNext + " delta: " + delta);
      if (delta < EPSILON) break;

      try {
        start = minimize(fun, start, sNext);
      } catch (IllegalStateException e) {
        MyLog.v(LOG_TAG, e.getMessage());
        break;
      }

      gPrev = gNext;
      sPrev = sNext;
    }

    solution = start;
    solutionSteps = series.size() - 1;
    MyLog.d(LOG_TAG, "Solved in " + solutionSteps + " iterations");
    return start;
  }
}
