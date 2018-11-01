package ru.didim99.tstu.core.numeric;

import android.content.Context;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 27.10.18.
 */
public class DiffSolver {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_diffSolver";

  private static final int    RANK      = 2;
  private static final double MAX_EPS   = 10E-3;
  private static final double MIN_EPS   = MAX_EPS / Math.pow(2, RANK + 1);
  private static final double E_FACTOR  = Math.pow(2, RANK) + 1;
  private static final double H0        = 0.5;
  private static final double X0        = -1.0;
  private static final double Y0        = 8.0;
  private static final double B         = 2.0 * Math.PI - 1.0;

  private Result result;

  DiffSolver(Config config) {
    this.result = new Result(config);
  }

  Result solve() {
    ArrayList<DataPoint> autoPoints = solveAutoStep(H0, true);
    double h = calcStepSize(autoPoints.size());
    ArrayList<DataPoint> fixedPoints = solveFixedStep(h);
    result.setGraphData(fixedPoints);
    return result;
  }

  private ArrayList<DataPoint> solveAutoStep(double initH, boolean keepEps) {
    MyLog.d(LOG_TAG, "Solving with auto step: " + initH);
    ArrayList<DataPoint> points = new ArrayList<>();
    DataPoint testPoint, point = new DataPoint(X0, Y0);
    points.add(point);
    double h = initH, delta;
    boolean firstStep = true;

    while (point.getX() < B) {
      testPoint = step(point, h);
      point = step(point, h / 2);
      delta = Math.abs((point.getY() - testPoint.getY()) / E_FACTOR);
      MyLog.v(LOG_TAG, String.format(Locale.US,
        "Step: x = %8.3f, y = %8.3f, h = %8.5f, e = %8.5f",
        point.getX(), point.getY(), h / 2, delta));

      if (firstStep) {
        h /= 2;
        if (!(delta < MAX_EPS)) {
          MyLog.d(LOG_TAG, "Restart (e = " + delta + ")");
          point = new DataPoint(X0, Y0);
          continue;
        } else
          firstStep = false;
      } else if (delta < MIN_EPS) {
        h *= 2;
      } else if (keepEps && delta > MAX_EPS) {
        h /= 2;
      }

      points.add(point);
    }

    MyLog.d(LOG_TAG, "Completed in " + points.size() + " steps");
    return points;
  }

  private ArrayList<DataPoint> solveFixedStep(double h) {
    MyLog.d(LOG_TAG, "Solving with fixed step: " + h);
    ArrayList<DataPoint> points = new ArrayList<>();
    DataPoint point = new DataPoint(X0, Y0);
    points.add(point);

    while (point.getX() < B) {
      point = step(point, h);
      points.add(point);
    }

    MyLog.d(LOG_TAG, "Completed in " + points.size() + " steps");
    return points;
  }

  private static double calcStepSize(int pointsCount) {
    return (B - X0) / (pointsCount - 1);
  }

  private static DataPoint step(DataPoint point, double h) {
    double xNext = point.getX() + h;
    double yTmp = f(point.getX(), point.getY());
    double yNext = point.getY() + h * yTmp;
    yNext = point.getY() + h / 2 * (yTmp + f(xNext, yNext));
    return new DataPoint(xNext, yNext);
  }

  private static double f(double x, double y) {
    return phi(x) * psi(x) - g(x) * y;
  }

  private static double g(double x) {
    return -Math.sin(x + 1);
  }

  private static double phi(double x) {
    return Math.exp(-Math.pow(x, 2));
  }

  private static double psi(double x) {
    return 0.01;
  }

  public static String getTextResult(Result result) {
    ArrayList<DataPoint> points = result.getGraphData();
    StringBuilder sb = new StringBuilder();
    sb.append(String.format(Locale.US,
      "steps:     %d\n", points.size() - 1));
    sb.append(String.format(Locale.US,
      "step size: %.5f\n",calcStepSize(points.size())));
    sb.append("\ny(x) values:\n").append("  x        y     \n");

    for (DataPoint point : points) {
      sb.append(String.format(Locale.US,
        "%8.4f %8.4f\n", point.getX(), point.getY()));
    }

    return sb.toString();
  }

  public static void drawGraph(Context ctx, Result result, GraphView view) {
    ArrayList<DataPoint> data = result.getGraphData();
    LineGraphSeries<DataPoint> series = new LineGraphSeries<>();

    if (data.size() <= 500) {
      series.resetData(data.toArray(new DataPoint[0]));
    } else {
      int step = data.size() / 500;
      for (int pos = 0; pos < data.size(); pos += step) {
        series.appendData(data.get(pos),
          false, 500);
      }
    }

    series.setColor(ctx.getResources()
      .getColor(R.color.colorAccent));
    series.setTitle("y(x)");
    view.getLegendRenderer().setVisible(true);
    view.addSeries(series);
  }
}
