package ru.didim99.tstu.core.optimization.methods;

import ru.didim99.tstu.core.optimization.math.FunctionRN;
import ru.didim99.tstu.core.optimization.math.PointD;
import ru.didim99.tstu.utils.MyLog;

import static ru.didim99.tstu.core.optimization.methods.MathUtils.*;

/**
 * Created by didim99 on 17.10.19.
 */
public class GradientMethod extends ExtremaFinderRN {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_CGM";

  @Override
  public PointD find(FunctionRN function, PointD start) {
    calcF(function, start);
    PointD xPrev = new PointD(start), xNext;
    PointD gPrev = null, gNext;
    PointD sPrev = null, sNext;
    double beta;

    while (true) {
      series.add(new PointD(xPrev));
      gNext = gradient(function, xPrev);
      sNext = gNext.negative();
      if (sPrev != null) {
        beta = gNext.length2(2) / gPrev.length2(2);
        sNext = sNext.add(sPrev.mult(beta));
      }

      double delta = gNext.length(2);
      MyLog.v(LOG_TAG, "Point: " + xPrev + " gradient: " + gNext + " delta: " + delta);
      if (delta < EPSILON) {
        MyLog.d(LOG_TAG, "Stopped by gradient delta");
        break;
      }

      try {
        xNext = minimize(function, xPrev, sNext);
        if (xNext.sub(xPrev).length(2) < EPSILON / 10) {
          MyLog.d(LOG_TAG, "Stopped by position delta");
          break;
        }

        xPrev.set(xNext);
      } catch (IllegalStateException e) {
        MyLog.v(LOG_TAG, e.getMessage());
        break;
      }

      gPrev = gNext;
      sPrev = sNext;
    }

    solution = xPrev;
    solutionSteps = series.size() - 1;
    MyLog.d(LOG_TAG, "Solved in " + solutionSteps + " iterations");
    return xPrev;
  }
}
