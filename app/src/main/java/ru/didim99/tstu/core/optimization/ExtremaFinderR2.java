package ru.didim99.tstu.core.optimization;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import ru.didim99.tstu.R;

/**
 * Created by didim99 on 11.10.19.
 */
public abstract class ExtremaFinderR2 {
  protected PointD solution;
  protected int solutionSteps;

  public abstract PointD find(FunctionR2 fun);
  public abstract void drawSteps(Bitmap bitmap, RectD range, Paint paint);
  public abstract void describeSteps(StringBuilder sb);
  public abstract RectD getRange();

  String getDescription(Context context) {
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
