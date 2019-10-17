package ru.didim99.tstu.core.optimization.methods;

import android.graphics.Bitmap;
import android.graphics.Paint;
import ru.didim99.tstu.core.optimization.ExtremaFinderR2;
import ru.didim99.tstu.core.optimization.FunctionR2;
import ru.didim99.tstu.core.optimization.PointD;
import ru.didim99.tstu.core.optimization.RectD;

/**
 * Created by didim99 on 17.10.19.
 */
public class GradientMethod extends ExtremaFinderR2 {

  private static final double EPSILON = 0.001;

  @Override
  public PointD find(FunctionR2 fun) {
    return null;
  }

  @Override
  public void drawSteps(Bitmap bitmap, RectD range, Paint paint) {

  }

  @Override
  public void describeSteps(StringBuilder sb) {

  }

  @Override
  public RectD getRange() {
    return null;
  }
}
