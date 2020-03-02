package ru.didim99.tstu.core.graphics.curve.builder;

import ru.didim99.tstu.core.graphics.curve.Curve;
import ru.didim99.tstu.core.graphics.curve.Point;
import ru.didim99.tstu.core.graphics.math.Mat4;
import ru.didim99.tstu.core.graphics.math.Vec4;

/**
 * Created by didim99 on 25.02.20.
 */
public class CubicBezierBuilder extends ControlPointBuilder {
  private static final Mat4 BEZIER_MATRIX = new Mat4(
    -1, 3, -3, 1, 3, -6, 3, 0, -3, 3, 0, 0, 1, 0, 0, 0);

  public CubicBezierBuilder(Curve curve) {
    super(curve, BEZIER_MATRIX);
  }

  @Override
  public boolean rebuild() {
    synchronized (renderLock) {
      int last = basePoints.size() - 1;
      if (last < 1) return false;

      interpolator.setLastPointIndex(last);
      for (int i = 0; i < last; i++) {
        Point p1 = basePoints.get(i);
        Point p4 = basePoints.get(i + 1);
        Point p2 = p1.getControlNext();
        Point p3 = p4.getControlPrev();
        Vec4 gx = new Vec4(p1.getVisibleX(), p2.getVisibleX(),
          p3.getVisibleX(), p4.getVisibleX());
        Vec4 gy = new Vec4(p1.getVisibleY(), p2.getVisibleY(),
          p3.getVisibleY(), p4.getVisibleY());
        interpolator.interpolate(i, gx, gy);
      }

      drawFrame();
      drawArms();
      return true;
    }
  }
}
