package ru.didim99.tstu.core.optimization.methods;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import ru.didim99.tstu.core.optimization.ExtremaFinderR2;
import ru.didim99.tstu.core.optimization.FunctionR2;
import ru.didim99.tstu.core.optimization.PointD;
import ru.didim99.tstu.core.optimization.RectD;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 02.10.19.
 */
public class PaulMethod extends ExtremaFinderR2 {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Paul";

  private static final double EPSILON = 0.0001;
  private static final double START_MIN = -10.0;
  private static final double START_MAX = 10.0;
  private static final double DEFAULT_STEP = 0.1;

  private double step = DEFAULT_STEP;
  private ArrayList<PointD> series;

  @Override
  public PointD find(FunctionR2 fun) {
    series = new ArrayList<>();
    Random random = new Random();

    PointD start = new PointD(
      Utils.randInRangeD(random, START_MIN, START_MAX),
      Utils.randInRangeD(random, START_MIN, START_MAX)
    );

    boolean done = false;
    while (!done) {
      try {
        series.add(new PointD(start));
        PointD next = step(fun, start);
        if (next.sub(start).isZero(EPSILON))
          done = true;
        start.set(next);
      } catch (IllegalStateException e) {
        MyLog.d(LOG_TAG, "Correcting step down: " + step + " to " + step / 2);
        step /= 2;
      }
    }

    series.add(new PointD(start));
    MyLog.d(LOG_TAG, "Solved in " + series.size() + " iterations");
    start = new PointD(start.get(0), start.get(1), fun.f(start));
    solutionSteps = series.size();
    solution = start;
    return start;
  }

  @Override
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

  @Override
  public void drawSteps(Bitmap bitmap, RectD range, Paint paint) {
    Canvas canvas = new Canvas(bitmap);

    for (PointD point : series) {
      point.set(0, Utils.map(point.get(0), range.xMin, range.xMax, 0, canvas.getWidth()));
      point.set(1, Utils.map(point.get(1), range.yMin, range.yMax, canvas.getHeight(), 0));
    }

    for (int i = 0; i < series.size() - 1; i++) {
      PointD p1 = series.get(i), p2 = series.get(i + 1);
      canvas.drawLine((float) p1.get(0), (float) p1.get(1),
        (float) p2.get(0), (float) p2.get(1), paint);
    }
  }

  @Override
  public void describeSteps(StringBuilder sb) {
    for (PointD point : series) {
      sb.append(String.format(Locale.US, "%7.4f %7.4f\n",
        point.get(0), point.get(1)));
    }
  }

  private PointD step(FunctionR2 fun, PointD start) {
    double f1 = fun.f(start), fs = f1, f2;
    PointD vec = findVector(fun, start).sub(start);
    PointD p1 = new PointD(start);
    PointD p2 = new PointD(start);

    int steps = 0;
    double df = -1;
    while (df < 0) {
      p2 = p2.add(vec);
      f2 = fun.f(p2);
      df = f2 - f1;
      if (df < 0) {
        p1.set(p2);
        f1 = f2;
        steps++;
      }
    }

    MyLog.v(LOG_TAG, "delta: " + (fs - f1) + " steps: " + steps);

    if (steps > 10) {
      MyLog.d(LOG_TAG, "Correcting step up: " + step + " to " + step * 2);
      step *= 2;
    }

    return p1;
  }

  private PointD findVector(FunctionR2 fun, PointD start) {
    double f1 = fun.f(start), fs = f1, f2;
    PointD p1 = new PointD(start);
    PointD p2 = new PointD(start);
    int totalSteps = 0;

    for (int i = 0; i < start.size(); i++) {
      boolean first = true;
      double step = this.step;
      double df = -1;
      int steps = 0;
      p2.set(p1);

      while (df < 0) {
        p2.add(i, step);
        f2 = fun.f(p2);
        df = f2 - f1;

        if (df < 0) {
          p1.set(i, p2.get(i));
          f1 = f2;
          steps++;
        }

        if (first) {
          first = false;
          if (df > 0) {
            step = -step;
            df = -df;
            f1 = f2;
            steps--;
          }
        }
      }

      if (steps > 0)
        totalSteps += steps;
    }

    if (totalSteps == 0)
      throw new IllegalStateException("Can't do step");
    MyLog.v(LOG_TAG, "delta: " + (fs - f1) + " steps: "
      + totalSteps + " vector: " + p1.sub(start));
    return p1;
  }
}
