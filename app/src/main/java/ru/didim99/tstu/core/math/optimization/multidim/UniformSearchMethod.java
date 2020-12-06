package ru.didim99.tstu.core.math.optimization.multidim;

import ru.didim99.tstu.core.math.common.FunctionRN;
import ru.didim99.tstu.core.math.common.PointD;
import ru.didim99.tstu.core.math.common.RectD;

import static ru.didim99.tstu.core.math.optimization.multidim.MathUtils.calcF;

/**
 * Created by didim99 on 15.10.20.
 */

public class UniformSearchMethod extends ExtremaFinderRN {
  private static final int STEP_COUNT = 1000;
  private static final double DEFAULT_MIN = -10;
  private static final double DEFAULT_MAX = 10;

  private PointD min, max;
  private PointD step, res;
  private int r;

  public void setBoundaries(PointD min, PointD max) {
    if (min == null || max == null)
      throw new IllegalArgumentException("Argument is null");
    if (min.size() != max.size())
      throw new IllegalArgumentException("Bounds must be same size");
    this.r = min.size();
    this.min = min;
    this.max = max;
  }

  @Override
  public PointD find(FunctionRN fun, PointD start) {
    return find(fun);
  }

  @Override
  public PointD find(FunctionRN fun) {
    checkBoundsState();
    step = max.sub(min).div(STEP_COUNT);
    PointD start = new PointD(r + 1);
    start.set(min);
    calcF(fun, start);
    solutionSteps = 0;
    res = new PointD(start);
    find(fun, start, r - 1);
    solution = res;
    return res;
  }

  private void find(FunctionRN fun, PointD p, int r) {
    while (p.get(r) < max.get(r)) {
      if (r > 0) {
        find(fun, p, r - 1);
        p.set(min, r);
      }

      p.add(r, step.get(r));
      calcF(fun, p);
      if (p.getLast() < res.getLast())
        res.set(p);
      solutionSteps++;
    }
  }

  @Override
  public RectD getRange() {
    return new RectD(
      min.get(0), max.get(0),
      min.get(1), max.get(1));
  }

  public void setDefaultBounds(int r) {
    setBoundaries(
      new PointD(r, DEFAULT_MIN),
      new PointD(r, DEFAULT_MAX)
    );
  }

  private void checkBoundsState() {
    if (min == null || max == null)
      throw new IllegalStateException("Configure bounds first");
  }
}
