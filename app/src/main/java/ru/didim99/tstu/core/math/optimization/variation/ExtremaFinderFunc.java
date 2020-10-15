package ru.didim99.tstu.core.math.optimization.variation;

import android.content.Context;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BaseSeries;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.math.common.Function;
import ru.didim99.tstu.core.math.common.PointD;
import ru.didim99.tstu.core.math.optimization.Result;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 30.11.19.
 */
public abstract class ExtremaFinderFunc {
  private static final double REF_STEP = 1E-4;
  static final int T = 0;
  static final int X = 1;

  ArrayList<PointD> solution;

  public abstract void solve(PointD start, PointD end);

  public ArrayList<PointD> getReference(Function ref, PointD start, PointD end) {
    ArrayList<PointD> series = new ArrayList<>();
    double x = start.get(0);

    while (x < end.get(0)) {
      series.add(new PointD(x, ref.f(x)));
      x += REF_STEP;
    }

    return series;
  }

  public ArrayList<PointD> getDelta(ArrayList<PointD> reference) {
    int size = Math.min(reference.size(), solution.size());
    boolean sMaster = reference.size() > solution.size();
    PointD d = new PointD(2);

    ArrayList<PointD> delta = new ArrayList<>(size);
    ArrayList<PointD> master = sMaster ? solution : reference;
    ArrayList<PointD> slave = sMaster ? reference : solution;

    for (PointD m : master) {
      double dt = Double.MAX_VALUE;
      for (PointD s : slave) {
        double cdt = Math.abs(m.get(T) - s.get(T));
        if (cdt < dt) {
          dt = cdt;
          d = s;
        } else break;
      }

      delta.add(new PointD(m.get(T), d.get(X) - m.get(X)));
    }

    return delta;
  }

  public String getDescription(Context context, Result result) {
    ArrayList<PointD> solution = result.getSolutionSeries();
    StringBuilder sb = new StringBuilder();
    int r = solution.get(0).size() - 1;

    if (result.getDelta() != null) {
      PointD pMin = new PointD(0d, Double.MAX_VALUE);
      PointD pMax = new PointD(0d, -Double.MAX_VALUE);
      for (PointD p : result.getDelta()) {
        if (p.get(X) < pMin.get(X)) pMin.set(p);
        if (p.get(X) > pMax.get(X)) pMax.set(p);
      }

      sb.append(context.getString(R.string.opt_calcDelta));
      sb.append("\n").append(context.getString(
        R.string.opt_minDelta, pMin.get(X), pMin.get(T)));
      sb.append("\n").append(context.getString(
        R.string.opt_maxDelta, pMax.get(X), pMax.get(T)));
      sb.append("\n\n");
    }

    sb.append(context.getString(
      R.string.opt_points, solution.size() / r));
    sb.append("\n\n").append(String.format("%8s", "t"));
    for (int i = 0; i < r; i++)
     sb.append(String.format(Locale.US, "   x%d(t)", i + 1));

    sb.append("\n");
    for (PointD point : solution) {
      sb.append(' ');
      for (int i = 0; i <= r; i++)
        sb.append(String.format(Locale.US, "%7.4f ", point.get(i)));
      sb.append('\n');
    }

    return sb.toString();
  }

  public ArrayList<PointD> getSolution() {
    return solution;
  }

  public static void drawGraph(Context ctx, Result result, GraphView view) {
    if (result.getDelta() != null) {
      BaseSeries<PointD> delta = (BaseSeries<PointD>)
        Utils.buildSeries(result.getDelta(), "delta");
      delta.setColor(ctx.getResources().getColor(R.color.graph2));
      view.addSeries(delta);
    } else {
      BaseSeries<PointD> reference = (BaseSeries<PointD>)
        Utils.buildSeries(result.getReference(), "ref");
      reference.setColor(ctx.getResources().getColor(R.color.graph1));
      view.addSeries(reference);

      BaseSeries<PointD> solution = (BaseSeries<PointD>)
        Utils.buildSeries(result.getSolutionSeries(), "x(t)");
      solution.setColor(ctx.getResources().getColor(R.color.colorAccent));
      view.addSeries(solution);
    }

    view.getLegendRenderer().setVisible(true);
    view.getViewport().setScalable(true);
  }
}
