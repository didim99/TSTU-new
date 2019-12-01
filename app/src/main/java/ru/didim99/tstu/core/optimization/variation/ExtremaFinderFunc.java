package ru.didim99.tstu.core.optimization.variation;

import android.content.Context;
import java.util.ArrayList;
import ru.didim99.tstu.core.optimization.Result;
import ru.didim99.tstu.core.optimization.math.Function;
import ru.didim99.tstu.core.optimization.math.PointD;

/**
 * Created by didim99 on 30.11.19.
 */
public abstract class ExtremaFinderFunc {

  ArrayList<PointD> solution;

  public abstract void solve(Function p, Function f, PointD start, PointD end);
  public abstract ArrayList<PointD> getReference(Function ref, PointD start, PointD end);
  public abstract ArrayList<PointD> getDelta(ArrayList<PointD> reference);
  public abstract String getDescription(Context context, Result result);

  public ArrayList<PointD> getSolution() {
    return solution;
  }
}
