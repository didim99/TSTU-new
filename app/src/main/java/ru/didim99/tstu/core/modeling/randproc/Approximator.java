package ru.didim99.tstu.core.modeling.randproc;

import java.util.ArrayList;
import ru.didim99.tstu.core.optimization.math.FunctionRN;
import ru.didim99.tstu.core.optimization.math.PointD;
import ru.didim99.tstu.core.optimization.math.PointRN;
import ru.didim99.tstu.core.optimization.multidim.DownhillMethod;
import ru.didim99.tstu.core.optimization.multidim.ExtremaFinderRN;

/**
 * Created by didim99 on 08.05.20.
 */
class Approximator implements FunctionRN {
  private ArrayList<PointRN> reference;
  private FunctionRN target;
  private PointRN params;
  private int r;

  public Approximator(ArrayList<PointRN> reference) {
    this.reference = reference;
  }

  public PointD approximate(FunctionRN target, PointD start, int r) {
    this.target = target;
    this.params = start;
    this.r = r;

    PointD point = new PointD(r + 1).set(start, r);
    ExtremaFinderRN finder = new DownhillMethod();
    return finder.find(this, point);
  }

  @Override
  public double f(PointRN p) {
    for (int i = 0; i < r; i++)
      params.set(i, p.get(i));
    double res = 0;

    for (PointRN point : reference) {
      params.set(r, point.get(0));
      res += Math.pow(target.f(params) - point.get(1), 2);
    }

    return res;
  }
}
