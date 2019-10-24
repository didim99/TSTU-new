package ru.didim99.tstu.core.optimization.methods;

import java.util.ArrayList;
import ru.didim99.tstu.core.optimization.math.FunctionRN;
import ru.didim99.tstu.core.optimization.math.PointD;
import ru.didim99.tstu.utils.MyLog;

import static ru.didim99.tstu.core.optimization.methods.MathUtils.*;

/**
 * Created by didim99 on 17.10.19.
 */
public class GradientMethod extends ExtremaFinderRN {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_CGM";

  private static final double EPSILON = 0.001;

  @Override
  public PointD find(FunctionRN fun, PointD start) {
    series = new ArrayList<>();
    calcF(fun, start);

    PointD gPrev = null, gNext;
    PointD sPrev = null, sNext;
    double beta;

    while (true) {
      series.add(new PointD(start));
      gNext = gradient(fun, start);
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
