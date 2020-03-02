package ru.didim99.tstu.core.graphics.curve.builder;

import ru.didim99.tstu.core.graphics.curve.Curve;
import ru.didim99.tstu.core.graphics.curve.Point;
import ru.didim99.tstu.core.graphics.model.Mat4;
import ru.didim99.tstu.core.graphics.model.Vec4;

/**
 * Created by didim99 on 20.02.20.
 */
public class HermiteBuilder extends ControlPointBuilder {
  private static final float R_FACTOR = 5;
  private static final Mat4 HERMITE_MATRIX = new Mat4(
    2, -2, 1, 1, -3, 3, -2, -1, 0, 0, 1, 0, 1, 0, 0, 0);

  public HermiteBuilder(Curve curve) {
    super(curve, HERMITE_MATRIX);
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
        Point r1 = p1.getControlNext();
        Point r4 = p4.getControlPrev();
        Vec4 gx = new Vec4(p1.getVisibleX(), p4.getVisibleX(),
          (r1.getRealX() - p1.getVisibleX()) * R_FACTOR,
          (r4.getRealX() - p4.getVisibleX()) * R_FACTOR);
        Vec4 gy = new Vec4(p1.getVisibleY(), p4.getVisibleY(),
          (r1.getRealY() - p1.getVisibleY()) * R_FACTOR,
          (r4.getRealY() - p4.getVisibleY()) * R_FACTOR);
        interpolator.interpolate(i, gx, gy);
      }

      drawFrame();
      drawArms();
      return true;
    }
  }
}
