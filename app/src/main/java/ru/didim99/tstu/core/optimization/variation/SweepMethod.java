package ru.didim99.tstu.core.optimization.variation;

import android.content.Context;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.optimization.Result;
import ru.didim99.tstu.core.optimization.math.Function;
import ru.didim99.tstu.core.optimization.math.PointD;

/**
 * Created by didim99 on 21.11.19.
 */
public class SweepMethod {
  private static final double STEP = 1E-4;
  private static final double STEP2 = STEP * STEP;

  private static final int T = 0;
  private static final int X = 1;
  private static final int C = 0;
  private static final int PHI = 1;

  private ArrayList<PointD> solution;

  public void solve(Function p, Function f, PointD start, PointD end) {
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

  public ArrayList<PointD> getReference(Function fun, PointD start, PointD end) {
    ArrayList<PointD> series = new ArrayList<>();
    double x = start.get(0);

    while (x < end.get(0)) {
      series.add(new PointD(x, fun.f(x)));
      x += STEP;
    }

    return series;
  }

  public ArrayList<PointD> getSolution() {
    return solution;
  }

  public String getDescription(Context context) {
    StringBuilder sb = new StringBuilder();

    sb.append(String.format(Locale.US, " %-6s  %-6s\n", "t", "x(t)"));
    for (PointD point : solution) {
      sb.append(String.format(Locale.US, "%7.4f %7.4f\n",
        point.get(0), point.get(1)));
    }

    return sb.toString();
  }

  public static void drawGraph(Context ctx, Result result, GraphView view) {
    LineGraphSeries<PointD> reference = makeSeries(result.getReference());
    reference.setColor(ctx.getResources().getColor(R.color.graph1));
    reference.setTitle("reference");
    view.addSeries(reference);

    LineGraphSeries<PointD> solution = makeSeries(result.getSolutionSeries());
    solution.setColor(ctx.getResources().getColor(R.color.colorAccent));
    solution.setTitle("x(t)");
    view.addSeries(solution);

    view.getLegendRenderer().setVisible(true);
    view.getViewport().setScalable(true);
  }

  private static LineGraphSeries<PointD> makeSeries(ArrayList<PointD> data) {
    LineGraphSeries<PointD> series = new LineGraphSeries<>();

    if (data.size() <= 500) {
      series.resetData(data.toArray(new PointD[0]));
    } else {
      int step = (int) Math.round(data.size() / 500f);
      for (int pos = 0; pos < data.size(); pos += step) {
        series.appendData(data.get(pos),
          false, 500);
      }
    }

    return series;
  }
}
