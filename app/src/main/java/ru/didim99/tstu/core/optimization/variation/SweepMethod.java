package ru.didim99.tstu.core.optimization.variation;

import java.util.ArrayList;
import java.util.Collections;
import ru.didim99.tstu.core.optimization.math.Function;
import ru.didim99.tstu.core.optimization.math.PointD;

/**
 * Created by didim99 on 21.11.19.
 */
public class SweepMethod extends ExtremaFinderFunc {
  private static final double STEP = 1E-4;
  private static final double STEP2 = STEP * STEP;

  private static final int C = 0;
  private static final int PHI = 1;

  private Function p, f;

  public SweepMethod(Function p, Function f) {
    this.p = p;
    this.f = f;
  }

  @Override
  public void solve(PointD start, PointD end) {
    ArrayList<PointD> factors = new ArrayList<>();
    PointD fPrev = new PointD(0.0, start.get(X));
    PointD fNext = new PointD(fPrev);
    double t = start.get(T);

    factors.add(new PointD(fPrev));
    while ((t += STEP) < end.get(T)) {
      fNext.set(C, 1 / (2 + p.f(t) * STEP2 - fPrev.get(C)));
      fNext.set(PHI, fNext.get(C) * (fPrev.get(PHI) - f.f(t) * STEP2));
      factors.add(new PointD(fNext));
      fPrev.set(fNext);
    }

    solution = new ArrayList<>(factors.size());
    PointD xPrev = new PointD(end);
    PointD xNext = new PointD(xPrev);
    t = end.get(T);

    solution.add(new PointD(xPrev));
    for (int i = factors.size() - 1; i >= 0; i--) {
      PointD factor = factors.get(i);
      xNext.set(X, xPrev.get(X) * factor.get(C) + factor.get(PHI));
      xNext.set(T, t -= STEP);
      solution.add(new PointD(xNext));
      xPrev.set(xNext);
    }

    Collections.reverse(solution);
  }
}
