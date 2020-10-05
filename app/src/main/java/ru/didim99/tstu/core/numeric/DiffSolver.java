package ru.didim99.tstu.core.numeric;

import android.content.Context;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BaseSeries;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.optimization.math.PointD;
import ru.didim99.tstu.core.optimization.math.PointRN;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 27.10.18.
 */
public class DiffSolver {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_diffSolver";

  private static final int    RANK      = 2;
  private static final double EPS_X     = 10E-3;
  private static final double MAX_EPS   = 10E-3;
  private static final double MIN_EPS   = MAX_EPS / Math.pow(2, RANK + 1);
  private static final double E_FACTOR  = Math.pow(2, RANK) + 1;
  private static final double H0        = 0.5;
  private static final double X0        = -1.0;
  private static final double Y0        = 8.0;
  private static final double B         = 2.0 * Math.PI - 1.0;

  private boolean calcDelta;
  private Result result;

  DiffSolver(Config config) {
    this.result = new Result(config);
    calcDelta = config.isTConst();
  }

  Result solve() {
    ArrayList<PointRN> autoPoints = solveAutoStep(H0, true);
    double h = calcStepSize(autoPoints.size());
    ArrayList<PointRN> fixedPoints = solveFixedStep(h);

    if (calcDelta) {
      autoPoints = solveAutoStep(H0, false);
      h = calcStepSize(autoPoints.size());
      ArrayList<PointRN> fixedPointsE = solveFixedStep(h);
      ArrayList<PointRN> delta = new ArrayList<>();
      PointRN other;
      int start = 0;

      for (PointRN point : fixedPointsE) {
        for (int i = start; i < fixedPoints.size(); i++) {
          other = fixedPoints.get(i);
          if (doubleEquals(point.getX(), other.getX())) {
            delta.add(new PointD(point.getX(), point.getY() - other.getY()));
            start = i;
            break;
          }
        }
      }

      result.setGraphData(delta);
    } else {
      result.setGraphData(fixedPoints);
    }

    return result;
  }

  private ArrayList<PointRN> solveAutoStep(double initH, boolean keepEps) {
    MyLog.d(LOG_TAG, "Solving with auto step: " + initH);
    ArrayList<PointRN> points = new ArrayList<>();
    PointRN testPoint, point = new PointD(X0, Y0);
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
          point = new PointD(X0, Y0);
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

  private ArrayList<PointRN> solveFixedStep(double h) {
    MyLog.d(LOG_TAG, "Solving with fixed step: " + h);
    ArrayList<PointRN> points = new ArrayList<>();
    PointRN point = new PointD(X0, Y0);
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

  private static PointRN step(PointRN point, double h) {
    double xNext = point.getX() + h;
    double yTmp = f(point.getX(), point.getY());
    double yNext = point.getY() + h * yTmp;
    yNext = point.getY() + h / 2 * (yTmp + f(xNext, yNext));
    return new PointD(xNext, yNext);
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

  private static boolean doubleEquals(double x1, double x2) {
    return Math.abs(x1 - x2) < EPS_X;
  }

  public static String getTextResult(Result result) {
    ArrayList<PointRN> points = result.getGraphData();
    StringBuilder sb = new StringBuilder();
    sb.append(String.format(Locale.US,
      "steps:     %d\n", points.size() - 1));
    sb.append(String.format(Locale.US,
      "step size: %.5f\n",calcStepSize(points.size())));
    if (result.getConfig().isTConst())
      sb.append("\ndelta(x) values:\n").append("  x        delta \n");
    else
      sb.append("\ny(x) values:\n").append("  x        y     \n");

    for (PointRN point : points) {
      sb.append(String.format(Locale.US,
        "%8.4f %8.4f\n", point.getX(), point.getY()));
    }

    return sb.toString();
  }

  public static void drawGraph(Context ctx, Result result, GraphView view) {
    ArrayList<PointRN> data = result.getGraphData();
    String name = result.getConfig().isTConst() ? "delta(x)" : "y(x)";
    BaseSeries<PointRN> series = (BaseSeries<PointRN>)
      Utils.buildSeries(data, name);
    series.setColor(ctx.getResources().getColor(R.color.colorAccent));
    view.getLegendRenderer().setVisible(true);
    view.addSeries(series);
  }
}
