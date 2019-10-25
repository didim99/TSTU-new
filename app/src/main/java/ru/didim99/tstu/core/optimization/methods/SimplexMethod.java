package ru.didim99.tstu.core.optimization.methods;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import ru.didim99.tstu.core.optimization.math.FunctionRN;
import ru.didim99.tstu.core.optimization.math.PointD;
import ru.didim99.tstu.core.optimization.math.RectD;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

import static ru.didim99.tstu.core.optimization.methods.MathUtils.calcF;

/**
 * Created by didim99 on 03.10.19.
 */
public class SimplexMethod extends ExtremaFinderRN {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Simplex";

  private static final double EPSILON = 0.0001;
  private static final double START_RANGE = 1.0;
  private static final double ALPHA = 1.0;
  private static final double GAMMA = -0.5;

  private FunctionRN fun;
  private ArrayList<PointD[]> series;

  public SimplexMethod() {
    series = new ArrayList<>();
  }

  @Override
  public PointD find(FunctionRN function, PointD startH) {
    Random random = new Random();
    fun = function;

    PointD startG = new PointD(
      Utils.randInRangeD(random,
        startH.get(0) - START_RANGE, startH.get(0) + START_RANGE),
      Utils.randInRangeD(random,
        startH.get(1) - START_RANGE, startH.get(1) + START_RANGE), 0);
    PointD startL = new PointD(
      Utils.randInRangeD(random,
        startH.get(0) - START_RANGE, startH.get(0) + START_RANGE),
      Utils.randInRangeD(random,
        startH.get(1) - START_RANGE, startH.get(1) + START_RANGE), 0);

    calcF(fun, startH);
    calcF(fun, startG);
    calcF(fun, startL);

    PointD[] simplex = new PointD[] {startH, startG, startL};

    double avg, sigma = 1.0;
    while (sigma > EPSILON) {
      logStep(simplex);
      //log(simplex);

      simplex = step(simplex);

      avg = 0;
      for (PointD point : simplex)
        avg += point.get(point.size() - 1);
      avg /= simplex.length;

      sigma = 0;
      for (PointD point : simplex)
        sigma += Math.pow(point.get(point.size() - 1) - avg, 2);
      sigma = Math.sqrt(sigma / (simplex.length + 1));
    }

    logStep(simplex);
    PointD min = simplex[0];
    for (PointD point : simplex) {
      if (point.get(2) < min.get(2))
        min = point;
    }

    solution = min;
    solutionSteps = series.size();
    MyLog.d(LOG_TAG, "Solved in " + solutionSteps + " iterations");
    return min;
  }

  @Override
  public RectD getRange() {
    RectD rect = new RectD(
      Double.MAX_VALUE, -Double.MAX_VALUE,
      Double.MAX_VALUE, -Double.MAX_VALUE);

    for (PointD[] entry : series) {
      for (PointD point : entry) {
        if (point.get(0) < rect.xMin)
          rect.xMin = point.get(0);
        if (point.get(0) > rect.xMax)
          rect.xMax = point.get(0);
        if (point.get(1) < rect.yMin)
          rect.yMin = point.get(1);
        if (point.get(1) > rect.yMax)
          rect.yMax = point.get(1);
      }
    }

    return rect;
  }

  @Override
  public void drawSteps(Bitmap bitmap, RectD range, Paint paint) {
    float[] points = new float[series.size() * 12];
    Canvas canvas = new Canvas(bitmap);

    int base = 0, offset;
    for (PointD[] simplex : series) {
      for (PointD point : simplex) {
        point.set(0, Utils.map(point.get(0), range.xMin, range.xMax, 0, canvas.getWidth()));
        point.set(1, Utils.map(point.get(1), range.yMin, range.yMax, canvas.getHeight(), 0));
      }

      for (int i = 0; i < 3; i++) {
        offset = base + i * 4;
        points[offset]      = (float) simplex[i].get(0);
        points[offset + 1]  = (float) simplex[i].get(1);
        points[offset + 2]  = (float) simplex[(i + 1) % 3].get(0);
        points[offset + 3]  = (float) simplex[(i + 1) % 3].get(1);
      }

      base += 12;
    }

    canvas.drawLines(points, paint);
  }

  @Override
  public void describeSteps(StringBuilder sb) {
    for (PointD[] simplex : series) {
      for (PointD point : simplex) {
        sb.append(String.format(Locale.US, "%7.4f %7.4f\n",
          point.get(0), point.get(1)));
      }

      sb.append("\n");
    }
  }

  private PointD[] step(PointD[] simplex) {
    PointD pH = simplex[0], pG = simplex[1], pL = simplex[2];

    PointD tmp;
    if (pH.get(2) < pG.get(2)) { tmp = pH; pH = pG; pG = tmp; }
    if (pH.get(2) < pL.get(2)) { tmp = pH; pH = pL; pL = tmp; }
    if (pG.get(2) < pL.get(2)) { tmp = pG; pG = pL; pL = tmp; }

    PointD pC = pG.add(pL).div(2.0);
    calcF(fun, pC);
    PointD p0 = flip(pH, pC, ALPHA);

    if (p0.get(2) < pL.get(2)) {
      PointD pR = flip(pC, p0, ALPHA);
      pH = pR.get(2) < pL.get(2) ? pR : p0;
      return new PointD[] {pH, pG, pL};
    } else if (!(p0.get(2) > pG.get(2))) {
      return new PointD[] {p0, pG, pL};
    }

    PointD pS = flip(pC, p0.get(2) < pH.get(2) ? p0 : pH, GAMMA);

    if (pS.get(2) < pH.get(2))
      return new PointD[] {pS, pG, pL};

    pH = flip(pH, pL, GAMMA);
    pG = flip(pG, pL, GAMMA);
    return new PointD[] {pH, pG, pL};
  }

  private PointD flip(PointD p, PointD base, double factor) {
    PointD r = p.add(base.sub(p).mult(1.0 + factor));
    calcF(fun, r);
    return r;
  }

  private void log(PointD[] simplex) {
    StringBuilder sb = new StringBuilder();
    for (PointD point : simplex)
      sb.append('\n').append(point);
    MyLog.v(LOG_TAG, sb.toString());
  }

  private void logStep(PointD[] simplex) {
    PointD[] entry = new PointD[simplex.length];
    for (int i = 0; i < simplex.length; i++)
      entry[i] = new PointD(simplex[i]);
    series.add(entry);
  }
}
