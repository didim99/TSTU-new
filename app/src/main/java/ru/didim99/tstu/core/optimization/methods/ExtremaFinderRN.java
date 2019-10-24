package ru.didim99.tstu.core.optimization.methods;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.optimization.math.Fine;
import ru.didim99.tstu.core.optimization.math.FunctionRN;
import ru.didim99.tstu.core.optimization.math.PointD;
import ru.didim99.tstu.core.optimization.math.RectD;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 11.10.19.
 */
public abstract class ExtremaFinderRN {
  ArrayList<PointD> series;
  PointD solution;
  int solutionSteps;

  public abstract PointD find(FunctionRN fun);

  public PointD find(FunctionRN function, Fine fine) {
    return null;
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
    sb.append("\n\n").append(context.getString(
      R.string.numeric_solutionSteps));
    sb.append("\n");
    describeSteps(sb);

    return sb.toString();
  }
}
