package ru.didim99.tstu.core.optimization.methods;

import java.util.ArrayList;
import ru.didim99.tstu.core.optimization.math.FunctionRN;
import ru.didim99.tstu.core.optimization.math.PointD;
import ru.didim99.tstu.utils.MyLog;

import static ru.didim99.tstu.core.optimization.methods.MathUtils.*;

/**
 * Created by didim99 on 17.10.19.
 */
public class FastDescentMethod extends ExtremaFinderRN {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_FDM";

  private static final double EPSILON = 0.001;
  private static final double START_MIN = -5.0;
  private static final double START_MAX = 5.0;

  public FastDescentMethod() {
    config.startMin = START_MIN;
    config.startMax = START_MAX;
  }

  @Override
  public PointD find(FunctionRN function, PointD start) {
    series = new ArrayList<>();
    calcF(function, start);

    while (true) {
      series.add(new PointD(start));
      PointD g = gradient(function, start).negative();
      double delta = g.length(2);
      MyLog.v(LOG_TAG, "Point: " + start + " gradient: " + g + " delta: " + delta);
      if (delta < EPSILON) break;

      try {
        start = minimize(function, start, g);
      } catch (IllegalStateException e) {
        MyLog.v(LOG_TAG, e.getMessage());
        break;
      }
    }


    solution = start;
    solutionSteps = series.size() - 1;
    MyLog.d(LOG_TAG, "Solved in " + solutionSteps + " iterations");
    return start;
  }
}
