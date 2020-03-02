package ru.didim99.tstu.core.graphics.curve.builder;

import ru.didim99.tstu.core.graphics.curve.Curve;
import ru.didim99.tstu.core.graphics.curve.Point;
import ru.didim99.tstu.core.graphics.model.Mat4;
import ru.didim99.tstu.core.graphics.model.Vec4;

/**
 * Created by didim99 on 29.02.20.
 */
public class BSpline3Builder extends BaseBuilder {
  private static final Mat4 B_SPLINE_MATRIX = new Mat4(
    -1, 3, -3, 1, 3, -6, 3, 0, -3, 0, 3, 0, 1, 4, 1, 0).multiply(1.0 / 6.0);

  public BSpline3Builder(Curve curve) {
    super(curve, false, B_SPLINE_MATRIX);
  }

  @Override
  public boolean rebuild() {
    synchronized (renderLock) {
      int last = basePoints.size() - 3;
      if (last < 1) return false;

      interpolator.setLastPointIndex(last);
      for (int i = 0; i < last; i++) {
        Point p1 = basePoints.get(i);
        Point p2 = basePoints.get(i + 1);
        Point p3 = basePoints.get(i + 2);
        Point p4 = basePoints.get(i + 3);
        Vec4 gx = new Vec4(p1.getVisibleX(), p2.getVisibleX(),
          p3.getVisibleX(), p4.getVisibleX());
        Vec4 gy = new Vec4(p1.getVisibleY(), p2.getVisibleY(),
          p3.getVisibleY(), p4.getVisibleY());
        interpolator.interpolate(i, gx, gy);
      }

      drawFrame();
      return true;
    }
  }
}
