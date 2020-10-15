package ru.didim99.tstu.core.math.optimization.multidim;

import ru.didim99.tstu.core.math.common.FunctionRN;
import ru.didim99.tstu.core.math.common.PointD;
import ru.didim99.tstu.utils.MyLog;

import static ru.didim99.tstu.core.math.optimization.multidim.MathUtils.*;

/**
 * Created by didim99 on 17.10.19.
 */
public class DownhillMethod extends ExtremaFinderRN {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_DHM";

  private static final double START_MIN = -5.0;
  private static final double START_MAX = 5.0;

  public DownhillMethod() {
    config.startMin = START_MIN;
    config.startMax = START_MAX;
  }

  @Override
  public PointD find(FunctionRN function, PointD start) {
    int r = start.size() - 1;
    calcF(function, start);
    PointD xPrev = new PointD(start), xNext;

    while (true) {
      series.add(new PointD(xPrev));
      PointD g = gradient(function, xPrev).negative();
      double delta = g.length(r);
      MyLog.v(LOG_TAG, "Point: " + xPrev + " gradient: " + g + " delta: " + delta);
      if (delta < EPSILON) {
        MyLog.d(LOG_TAG, "Stopped by gradient delta");
        break;
      }

      try {
        xNext = minimize(function, xPrev, g);
        if (xNext.sub(xPrev).length(r) < EPSILON / 10) {
          MyLog.d(LOG_TAG, "Stopped by position delta");
          break;
        }

        xPrev.set(xNext);
      } catch (IllegalStateException e) {
        MyLog.v(LOG_TAG, e.getMessage());
        break;
      }
    }


    solution = xPrev;
    solutionSteps = series.size() - 1;
    MyLog.d(LOG_TAG, "Solved in " + solutionSteps + " iterations");
    return xPrev;
  }
}
