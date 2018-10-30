package ru.didim99.tstu.core.numeric;

import android.content.Context;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 25.10.18.
 */
public class Interpolator {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_interpolator";
  private static final String DELIMITER = "\\s+";
  private static final double GRAPH_OFFSET = 2.0;
  private static final int GRAPH_STEPS = 500;

  private Config config;
  private Result result;
  private double[] xSrc, ySrc, xRes, yRes;
  private Matrix delta;
  private int dSize;

  Interpolator(Config config) {
    this.config = config;
    this.result = new Result(config);
  }

  Result interpolate() {
    try {
      initValues(config.getFileName());
      MyLog.d(LOG_TAG, String.format(Locale.US,
        "Parsed initial values: n=%d, m=%d", xSrc.length, xRes.length));
    } catch (IOException e) {
      MyLog.e(LOG_TAG, "Can't open input file: " + e);
      return result;
    }

    calcDelta();
    MyLog.d(LOG_TAG, delta.toString());
    for (int i = 0; i < xRes.length; i++)
      yRes[i] = p(xRes[i]);

    ArrayList<ArrayList<DataPoint>> series = new ArrayList<>();
    ArrayList<DataPoint> res = new ArrayList<>();
    ArrayList<Matrix> matrices = new ArrayList<>();
    matrices.add(delta);

    for (int i = 0; i < xSrc.length; i++)
      res.add(new DataPoint(xSrc[i], ySrc[i]));
    series.add(res);

    res = new ArrayList<>();
    for (int i = 0; i < xRes.length; i++)
      res.add(new DataPoint(xRes[i], yRes[i]));
    Collections.sort(res, (p1, p2)
      -> Double.compare(p1.getX(), p2.getX()));
    series.add(res);

    res = getGraph(xSrc[0], xSrc[xSrc.length - 1]);
    series.add(res);

    result.setPolynom(getPolynom());
    result.setMatrixSeries(matrices);
    result.setGraphDataSeries(series);
    return result;
  }

  private ArrayList<DataPoint> getGraph(double start, double end) {
    ArrayList<DataPoint> graph = new ArrayList<>();
    start -= GRAPH_OFFSET;
    end += GRAPH_OFFSET;

    double step = (end - start) / GRAPH_STEPS;
    for (int i = 0; i < GRAPH_STEPS; i++) {
      graph.add(new DataPoint(start, p(start)));
      start += step;
    }

    return graph;
  }

  private double p(double x) {
    double y = ySrc[0], tmp = 1;

    for (int i = 0; i < dSize; i++) {
      tmp *= x - xSrc[i];
      y += tmp * delta.get(0, i);
    }

    return y;
  }

  private void calcDelta() {
    delta = new Matrix("Delta", dSize, dSize);

    for (int i = 0; i < dSize; i++)
      delta.set(i, 0, (ySrc[i+1] - ySrc[i]) / (xSrc[i+1] - xSrc[i]));

    for (int j = 1; j < dSize; j++) {
      for (int i = 0; i < dSize - j; i++) {
        delta.set(i, j, delta(i, j));
      }
    }
  }

  private double delta(int i, int k) {
    return (delta.get(i+1, k-1) - delta.get(i, k-1)) / (xSrc[i+k+1] - xSrc[i]);
  }

  private void initValues(String fileName) throws IOException {
    ArrayList<String> data = Utils.readFile(fileName);
    if (data.size() < 3)
      throw new IOException("Incorrect input file format");
    xSrc = Utils.stringArrayToDoubleArray(data.get(0).trim().split(DELIMITER));
    ySrc = Utils.stringArrayToDoubleArray(data.get(1).trim().split(DELIMITER));
    xRes = Utils.stringArrayToDoubleArray(data.get(2).trim().split(DELIMITER));
    if (xSrc == null || ySrc == null || xRes == null)
      throw new IOException("Incorrect input file format");
    yRes = new double[xRes.length];
    dSize = xSrc.length - 1;
  }

  private String getPolynom() {
    StringBuilder sb = new StringBuilder();
    sb.append(fd(ySrc[0]));

    double tmp;
    for (int i = 0; i < dSize; i++) {
      tmp = delta.get(0, i);
      sb.append(tmp < 0 ? " - " : " + ");
      sb.append(fd(Math.abs(tmp))).append(" * ");
      for (int j = 0; j <= i; j++)
        sb.append(String.format(
          Locale.US, "(x - %.2f)", xSrc[j]));
    }

    return sb.toString();
  }

  private String fd(double d) {
    return String.format(Locale.US, "%.4f", d);
  }

  public static String getTextResult(Result result, boolean verbose) {
    StringBuilder sb = new StringBuilder();

    if (verbose) {
      sb.append("Polynom:\n").append(result.getPolynom());
      sb.append("\n\n").append(result.getMatrixSeries().get(0));
      sb.append("\n\n");
    }

    ArrayList<DataPoint> points = result.getGraphDataSeries().get(0);
    sb.append("f(x_i) values:\n").append("  x_i      f     \n");
    for (DataPoint point : points) {
      sb.append(String.format(Locale.US,
        "%8.5f %8.5f\n", point.getX(), point.getY()));
    }

    points = result.getGraphDataSeries().get(1);
    sb.append("\nP(x_j) values:\n").append("  x_j      P     \n");
    for (DataPoint point : points) {
      sb.append(String.format(Locale.US,
        "%8.5f %8.5f\n", point.getX(), point.getY()));
    }

    return sb.toString();
  }

  public static void drawGraph(Context ctx, Result result, GraphView view) {
    ArrayList<ArrayList<DataPoint>> graphs = result.getGraphDataSeries();
    LineGraphSeries<DataPoint> linSeries = new LineGraphSeries<>();
    linSeries.resetData(graphs.get(2).toArray(new DataPoint[0]));
    linSeries.setColor(ctx.getResources().getColor(R.color.colorAccent));
    linSeries.setTitle("f(x)");
    view.addSeries(linSeries);

    PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>();
    series.resetData(graphs.get(0).toArray(new DataPoint[0]));
    series.setColor(ctx.getResources().getColor(R.color.graph1));
    series.setSize(10f);
    series.setTitle("f(x_i)");
    view.addSeries(series);

    series = new PointsGraphSeries<>();
    series.resetData(graphs.get(1).toArray(new DataPoint[0]));
    series.setColor(ctx.getResources().getColor(R.color.graph2));
    series.setSize(10f);
    series.setTitle("P(x_j)");
    view.addSeries(series);

    view.getLegendRenderer().setVisible(true);
    view.getViewport().setScalable(true);
  }
}
