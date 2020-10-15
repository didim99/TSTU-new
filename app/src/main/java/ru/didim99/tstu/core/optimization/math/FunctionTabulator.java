package ru.didim99.tstu.core.optimization.math;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by didim99 on 15.10.20.
 */

public class FunctionTabulator {
  private static final int DEFAULT_STEP_COUNT = 100;

  private final Function function;
  private int stepCount;

  public FunctionTabulator(Function f) {
    this.stepCount = DEFAULT_STEP_COUNT;
    this.function = f;
  }

  public void setStepCount(int stepCount) {
    this.stepCount = stepCount;
  }

  public List<PointRN> tabulate(double from, double to) {
    List<PointRN> series = new ArrayList<>(stepCount);
    double step = (to - from) / stepCount;
    while (from <= to) {
      series.add(new PointD(from, function.f(from)));
      from += step;
    }

    return series;
  }

  public List<PointRN> tabulate(List<PointRN> ref) {
    List<PointRN> series = new ArrayList<>(ref.size());
    for (PointRN point : ref)
      series.add(new PointD(point.get(0), function.f(point.get(0))));
    return series;
  }
}
