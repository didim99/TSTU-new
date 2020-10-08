package ru.didim99.tstu.core.numeric;

import android.content.Context;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.optimization.math.PointD;
import ru.didim99.tstu.core.optimization.math.PointRN;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 26.10.18.
 */
public class Integrator {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_integrator";

  private static final double A1    = 0.0;
  private static final double B1    = Math.PI;
  private static final double A2    = 0.0;
  private static final double B2    = 1.0;
  private static final double MU    = -0.05;
  private static final double C     = 1.0;
  private static final double D     = 2.0;
  private static final int    M     = 20;
  private static final int    N     = 1000;
  private static final double STEP  = (D - C) / M;

  private Result result;
  private double t;

  Integrator(Config config) {
    this.result = new Result(config);
  }

  Result solve() {
    ArrayList<Double> series = new ArrayList<>();
    double u, s;

    u = integrate(Integrator::f, A1, B1);
    MyLog.d(LOG_TAG, "u = " + u);

    t = C;
    for (int i = 0; i <= M; i++) {
      s = integrate(this::F, A2, B2);
      MyLog.d(LOG_TAG, String.format(Locale.US,
        "t: %.3f s: %f", t, s));
      series.add(s);
      t += STEP;
    }

    result.setSolution(u);
    result.setSolutionSeries(series);
    result.setGraphData(getGraph(Integrator::f, A1, B1));
    return result;
  }

  private static double integrate(Function f, double start, double end) {
    double h = (end - start) / N;
    double x = start, res;
    MyLog.d(LOG_TAG, "Integrating: h = " + h);
    res = f.compute(start) / 2;
    for (int i = 1; i < N; i++)
      res += f.compute(x += h);
    res += f.compute(end) / 2;
    return res * h;
  }

  private double F(double x) {
    return phi(t / (1 + Math.pow(x, 2)) + MU * x) * psi(x);
  }

  private static double f(double x) {
    return Math.exp(x) * Math.sin(x);
  }

  private static double phi(double z) {
    return Math.sqrt(2.0 - Math.cos(z));
  }

  private static double psi(double x) {
    return (2.0 + x) / (Math.pow(x, 2) - x + 1.0);
  }

  public static String getTextResult(Result result) {
    StringBuilder sb = new StringBuilder();
    sb.append("f(x):\n");
    sb.append(String.format(Locale.US,
      "  u: %.6f\n", result.getSolution()));
    sb.append("\n").append("F(t):\n");
    sb.append("  t     F     \n");

    double t = C;
    for (Double solution : result.getSolutionSeries()) {
      sb.append(String.format(Locale.US,
        "  %-5.2f %7.5f\n", t, solution));
      t += STEP;
    }

    return sb.toString();
  }

  private static ArrayList<PointRN> getGraph(Function f, double start, double end) {
    int count = N / 10;
    ArrayList<PointRN> points = new ArrayList<>(count);
    double h = (end - start) / count, x = start;
    MyLog.d(LOG_TAG, "Calculating graph: h = " + h);

    for (int i = 0; i <= count; i++) {
      points.add(new PointD(x, f.compute(x)));
      x += h;
    }

    return points;
  }

  public static void drawGraph(Context ctx, Result result, GraphView view) {
    ArrayList<PointRN> data = result.getGraphData();
    LineGraphSeries<PointRN> series = new LineGraphSeries<>(
      data.toArray(new PointRN[0]));
    series.setBackgroundColor(ctx.getResources()
      .getColor(R.color.colorAccentBg));
    series.setColor(ctx.getResources()
      .getColor(R.color.colorAccent));
    series.setDrawBackground(true);
    series.setTitle("f(x)");
    view.addSeries(series);
    view.getLegendRenderer().setVisible(true);
    view.getViewport().setYAxisBoundsManual(true);
    view.getViewport().setScalable(true);
    view.getViewport().setMinY(0.0);
  }

  @FunctionalInterface
  private interface Function {
    double compute(double x);
  }
}
