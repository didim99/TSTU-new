package ru.didim99.tstu.core.math.optimization.variation;

import ru.didim99.tstu.core.math.common.FunctionRN;
import ru.didim99.tstu.core.math.common.Functional;
import ru.didim99.tstu.core.math.common.PointD;
import ru.didim99.tstu.core.math.optimization.multidim.DownhillMethod;
import ru.didim99.tstu.core.math.optimization.multidim.ExtremaFinderRN;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 30.11.19.
 */
public class EulerMethod extends ExtremaFinderFunc {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_EDM";
  private static final double EPSILON = 2E-3;
  private static final int R_START = 2;

  private Functional j;

  public EulerMethod(FunctionRN j) {
    this.j = new Functional(j);
  }

  @Override
  public void solve(PointD start, PointD end) {
    ExtremaFinderRN finder = new DownhillMethod();
    int resolution = R_START;
    double delta = 1.0;

    j.setBounds(start, end);
    j.setResolution(resolution++);
    PointD jStart = j.getStartValues();
    PointD jPrev = finder.find(j, jStart), jNext;

    while (delta > EPSILON) {
      MyLog.d(LOG_TAG, "Solving with resolution: " + resolution);
      j.setResolution(resolution);
      jStart = j.getStartValues();
      jNext = finder.find(j, jStart);
      delta = jNext.getLast() - jPrev.getLast();
      MyLog.d(LOG_TAG, "Functional delta: " + delta);
      jPrev = jNext;
      resolution++;
    }

    solution = j.getSeries(jPrev);
  }
}
