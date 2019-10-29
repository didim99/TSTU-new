package ru.didim99.tstu.core.optimization.methods;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.optimization.math.Fine;
import ru.didim99.tstu.core.optimization.math.FunctionRN;
import ru.didim99.tstu.core.optimization.math.PointD;
import ru.didim99.tstu.core.optimization.math.RectD;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

import static ru.didim99.tstu.core.optimization.methods.MathUtils.*;

/**
 * Created by didim99 on 11.10.19.
 */
public abstract class ExtremaFinderRN {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_EFRN";

  private static final double START_MIN = -10.0;
  private static final double START_MAX = 10.0;

  private Random random;
  private ArrayList<PointD> globalSeries;
  private int globalSolutionSteps;

  ArrayList<PointD> series;
  int solutionSteps;
  PointD solution;
  Config config;

  ExtremaFinderRN() {
    random = new Random();
    config = new Config();
    config.startMin = START_MIN;
    config.startMax = START_MAX;
    series = new ArrayList<>();
  }

  public abstract PointD find(FunctionRN fun, PointD start);

  public PointD find(FunctionRN function) {
    return find(function, randPoint());
  }

  public PointD find(FunctionRN function, Fine fine) {
    globalSeries = new ArrayList<>();
    globalSolutionSteps = 0;

    PointD start = randPoint();
    if (!fine.check(start)) {
      while (!fine.check(start))
        start = randPoint();
    }

    MathUtils.resetStep();
    PointD xPrev = new PointD(start), xNext;
    PointD delta = new PointD(1, 1, 0);
    globalSeries.add(new PointD(start));

    while (delta.length(2) > EPSILON) {
      xNext = find(p -> function.f(p) + fine.f(p), new PointD(xPrev));
      MyLog.d(LOG_TAG, "xPrev: " + xPrev + " xNext: " + xNext);
      globalSolutionSteps += solutionSteps;
      globalSeries.add(new PointD(xNext));
      delta = xNext.sub(xPrev);
      MathUtils.correctStep();
      fine.increaseFactor();
      xPrev.set(xNext);
    }

    MathUtils.resetStep();
    this.solutionSteps = globalSolutionSteps;
    this.globalSolutionSteps = globalSeries.size() - 1;
    return xPrev;
  }

  private PointD randPoint() {
    return new PointD(
      Utils.randInRangeD(random, config.startMin, config.startMax),
      Utils.randInRangeD(random, config.startMin, config.startMax), 0);
  }

  public RectD getRange() {
    RectD rect = new RectD(
      Double.MAX_VALUE, -Double.MAX_VALUE,
      Double.MAX_VALUE, -Double.MAX_VALUE);

    for (PointD point : series) {
      if (point.get(0) < rect.xMin)
        rect.xMin = point.get(0);
      if (point.get(0) > rect.xMax)
        rect.xMax = point.get(0);
      if (point.get(1) < rect.yMin)
        rect.yMin = point.get(1);
      if (point.get(1) > rect.yMax)
        rect.yMax = point.get(1);
    }

    return rect;
  }

  public void drawSteps(Bitmap bitmap, RectD range, Paint paint) {
    Canvas canvas = new Canvas(bitmap);
    int w = canvas.getWidth();
    int h = canvas.getHeight();

    for (PointD point : series) {
      point.set(0, Utils.map(point.get(0), range.xMin, range.xMax, 0, w));
      point.set(1, Utils.map(point.get(1), range.yMin, range.yMax, h, 0));
    }

    for (int i = 0; i < series.size() - 1; i++) {
      PointD p1 = series.get(i), p2 = series.get(i + 1);
      if (p1.equals(p2)) continue;

      canvas.drawLine((float) p1.get(0), (float) p1.get(1),
        (float) p2.get(0), (float) p2.get(1), paint);
    }
  }

  public void drawGlobalSteps(Bitmap bitmap, RectD range, Paint paint) {
    Canvas canvas = new Canvas(bitmap);
    int w = canvas.getWidth();
    int h = canvas.getHeight();

    for (PointD point : globalSeries) {
      canvas.drawPoint(
        (float) Utils.map(point.get(0), range.xMin, range.xMax, 0, w),
        (float) Utils.map(point.get(1), range.yMin, range.yMax, h, 0), paint);
    }
  }

  public void describeSteps(StringBuilder sb) {
    for (PointD point : series) {
      sb.append(String.format(Locale.US, "%7.4f %7.4f\n",
        point.get(0), point.get(1)));
    }
  }

  public String getDescription(Context context) {
    StringBuilder sb = new StringBuilder();

    sb.append(context.getString(
      R.string.opt_solutionR2, solution.get(0), solution.get(1)));
    sb.append("\n").append(context.getString(
      R.string.opt_valueR2, solution.get(2)));
    sb.append("\n").append(context.getString(
      R.string.opt_iterationsR2, solutionSteps));
    if (globalSolutionSteps > 0)
      sb.append(" ").append(context.getString(
        R.string.opt_globalIterationsR2, globalSolutionSteps));
    sb.append("\n\n").append(context.getString(
      R.string.numeric_solutionSteps));
    sb.append("\n");
    describeSteps(sb);

    return sb.toString();
  }

  static class Config {
    double startMin, startMax;
  }
}
