package ru.didim99.tstu.core.optimization.variation;

import android.content.Context;
import java.util.ArrayList;
import ru.didim99.tstu.core.optimization.Result;
import ru.didim99.tstu.core.optimization.math.Function;
import ru.didim99.tstu.core.optimization.math.PointD;

/**
 * Created by didim99 on 30.11.19.
 */
public class EulerMethod extends ExtremaFinderFunc {

  @Override
  public void solve(Function p, Function f, PointD start, PointD end) {

  }

  @Override
  public ArrayList<PointD> getReference(Function ref, PointD start, PointD end) {
    return new ArrayList<>();
  }

  @Override
  public ArrayList<PointD> getDelta(ArrayList<PointD> reference) {
    return new ArrayList<>();
  }

  @Override
  public String getDescription(Context context, Result result) {
    return null;
  }
}
