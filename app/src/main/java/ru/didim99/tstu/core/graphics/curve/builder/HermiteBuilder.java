package ru.didim99.tstu.core.graphics.curve.builder;

import android.graphics.PointF;
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
    super(curve);
  }

  @Override
  public boolean rebuild() {
    synchronized (renderLock) {
      int last = basePoints.size() - 1;
      if (last < 1) return false;

      int step = STEP_POINTS / last;
      double t, dt = T_MAX / step;
      for (int i = 0; i < last; i++) {
        Point p1 = basePoints.get(i);
        Point p4 = basePoints.get(i + 1);
        Point r1 = p1.getControlNext();
        Point r4 = p4.getControlPrev();
        PointF prev = new PointF(p1.getVisibleX(), p1.getVisibleY());
        Vec4 gx = new Vec4(prev.x, p4.getVisibleX(),
          (r1.getRealX() - prev.x) * R_FACTOR,
          (r4.getRealX() - p4.getVisibleX()) * R_FACTOR);
        Vec4 gy = new Vec4(prev.y, p4.getVisibleY(),
          (r1.getRealY() - prev.y) * R_FACTOR,
          (r4.getRealY() - p4.getVisibleY()) * R_FACTOR);
        Vec4 vt = new Vec4();

        t = dt;
        float x, y;
        int offset = i * step * 4;
        for (int index = 0; index < step * 4; index += 4) {
          vt.set(t * t * t, t * t, t, 1);
          vt = vt.multiply(HERMITE_MATRIX);
          x = (float) gx.multiply(vt);
          y = (float) gy.multiply(vt);
          pointsPuffer[offset + index]     = prev.x;
          pointsPuffer[offset + index + 1] = prev.y;
          pointsPuffer[offset + index + 2] = x;
          pointsPuffer[offset + index + 3] = y;
          prev.set(x, y);
          t += dt;
        }
      }

      drawFrame();
      drawArms();
      return true;
    }
  }
}
