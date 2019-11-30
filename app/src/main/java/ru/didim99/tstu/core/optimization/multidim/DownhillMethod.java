package ru.didim99.tstu.core.optimization.multidim;

import ru.didim99.tstu.core.optimization.math.FunctionRN;
import ru.didim99.tstu.core.optimization.math.PointD;
import ru.didim99.tstu.utils.MyLog;

import static ru.didim99.tstu.core.optimization.multidim.MathUtils.*;

/**
 * Created by didim99 on 17.10.19.
 */
public class DownhillMethod extends ExtremaFinderRN {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_FDM";

  private static final double START_MIN = -5.0;
  private static final double START_MAX = 5.0;

  public DownhillMethod() {
    config.startMin = START_MIN;
    config.startMax = START_MAX;
  }

  @Override
  public PointD find(FunctionRN function, PointD start) {
    calcF(function, start);
    PointD xPrev = new PointD(start), xNext;

    while (true) {
      series.add(new PointD(xPrev));
      PointD g = gradient(function, xPrev).negative();
      double delta = g.length(2);
      MyLog.v(LOG_TAG, "Point: " + xPrev + " gradient: " + g + " delta: " + delta);
      if (delta < EPSILON) {
        MyLog.d(LOG_TAG, "Stopped by gradient delta");
        break;
      }

      try {
        xNext = minimize(function, xPrev, g);
        if (xNext.sub(xPrev).length(2) < EPSILON / 10) {
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
