package ru.didim99.tstu.core.optimization.variation;

import android.content.Context;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.optimization.Result;
import ru.didim99.tstu.core.optimization.math.Function;
import ru.didim99.tstu.core.optimization.math.PointD;

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
      R.string.opt_points, solution.size()));
    sb.append("\n\n").append(String.format(Locale.US,
      " %-6s  %-6s", "t", "x(t)"));

    sb.append("\n");
    for (PointD point : solution) {
      sb.append(String.format(Locale.US, "%7.4f %7.4f\n",
        point.get(0), point.get(1)));
    }

    return sb.toString();
  }

  public ArrayList<PointD> getSolution() {
    return solution;
  }

  public static void drawGraph(Context ctx, Result result, GraphView view) {
    if (result.getDelta() != null) {
      LineGraphSeries<PointD> delta = makeSeries(result.getDelta());
      delta.setColor(ctx.getResources().getColor(R.color.graph2));
      delta.setTitle("delta");
      view.addSeries(delta);
    } else {
      LineGraphSeries<PointD> reference = makeSeries(result.getReference());
      reference.setColor(ctx.getResources().getColor(R.color.graph1));
      reference.setTitle("ref");
      view.addSeries(reference);

      LineGraphSeries<PointD> solution = makeSeries(result.getSolutionSeries());
      solution.setColor(ctx.getResources().getColor(R.color.colorAccent));
      solution.setTitle("x(t)");
      view.addSeries(solution);
    }

    view.getLegendRenderer().setVisible(true);
    view.getViewport().setScalable(true);
  }

  private static LineGraphSeries<PointD> makeSeries(ArrayList<PointD> data) {
    LineGraphSeries<PointD> series = new LineGraphSeries<>();

    if (data.size() <= 500) {
      series.resetData(data.toArray(new PointD[0]));
    } else {
      int step = Math.round(data.size() / 500f);
      for (int pos = 0; pos < data.size(); pos += step) {
        series.appendData(data.get(pos),
          false, 500);
      }
    }

    return series;
  }
}
