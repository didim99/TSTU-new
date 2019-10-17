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
 * Created by didim99 on 17.10.19.
 */
public class FastDescentMethod extends ExtremaFinderR2 {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_FDM";

  private static final double EPSILON = 0.001;
  private static final double START_MIN = -10.0;
  private static final double START_MAX = 10.0;
  private static final double G_STEP = 0.01;

  private FunctionR2 fun;
  private ArrayList<PointD> series;

  @Override
  public PointD find(FunctionR2 function) {
    fun = function;
    series = new ArrayList<>();
    Random random = new Random();

    PointD start = new PointD(
      Utils.randInRangeD(random, START_MIN, START_MAX),
      Utils.randInRangeD(random, START_MIN, START_MAX), 0
    );



    return start;
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

  @Override
  public RectD getRange() {
    /*RectD rect = new RectD(
      Double.MAX_VALUE, -Double.MAX_VALUE,
      Double.MAX_VALUE, -Double.MAX_VALUE);*/
    RectD rect = new RectD(START_MAX, START_MIN, START_MAX, START_MIN);

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

  private PointD gradient(FunctionR2 fun, PointD p) {
    PointD g = new PointD(0, 0, 0);
    PointD p2 = new PointD(p);
    calcF(fun, p);

    for (int i = 0; i < p.size() - 1; i++) {
      p2.set(p).add(i, G_STEP);
      g.set(i, (fun.f(p2) - p.get(2)) / G_STEP);
    }

    return g;
  }

  private void calcF(FunctionR2 fun, PointD p) {
    p.set(2, fun.f(p));
  }
}
