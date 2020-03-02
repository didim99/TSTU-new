package ru.didim99.tstu.core.graphics.curve.builder;

import android.graphics.PointF;

import ru.didim99.tstu.core.graphics.curve.Curve;
import ru.didim99.tstu.core.graphics.curve.Point;
import ru.didim99.tstu.core.graphics.model.Mat4;
import ru.didim99.tstu.core.graphics.model.Vec4;

/**
 * Created by didim99 on 29.02.20.
 */
public class CatmullRomBuilder extends BaseBuilder {
  private static final Mat4 CATMULL_ROM_MATRIX = new Mat4(
    0, -1, 2, -1, 2, 0, -5, 3, 0, 1, 4, -3, 0, 0, -1, 1).multiply(0.5);

  public CatmullRomBuilder(Curve curve) {
    super(curve);
  }

  @Override
  public boolean rebuild() {
    synchronized (renderLock) {
      int last = basePoints.size() - 3;
      if (last < 1) return false;

      int step = STEP_POINTS / last;
      double t, dt = T_MAX / step;
      for (int i = 0; i < last; i++) {
        Point p1 = basePoints.get(i);
        Point p2 = basePoints.get(i + 1);
        Point p3 = basePoints.get(i + 2);
        Point p4 = basePoints.get(i + 3);
        Vec4 gx = new Vec4(p1.getVisibleX(), p2.getVisibleX(),
          p3.getVisibleX(), p4.getVisibleX());
        Vec4 gy = new Vec4(p1.getVisibleY(), p2.getVisibleY(),
          p3.getVisibleY(), p4.getVisibleY());
        Vec4 vt = new Vec4();
        PointF prev = null;

        t = T_MIN;
        float x, y;
        int offset = i * step * 4;
        for (int index = 0; index < step * 4; index += 4) {
          vt.set(1, t, t * t, t * t * t);
          //vt.set(t * t * t, t * t, t, 1);
          vt = vt.multiply(CATMULL_ROM_MATRIX);
          x = (float) gx.multiply(vt);
          y = (float) gy.multiply(vt);
          if (prev != null) {
            pointsPuffer[offset + index]      = prev.x;
            pointsPuffer[offset + index + 1]  = prev.y;
            pointsPuffer[offset + index + 2]  = x;
            pointsPuffer[offset + index + 3]  = y;
          } else {
            prev = new PointF();
            index -= 4;
          }

          prev.set(x, y);
          t += dt;
        }
      }

      drawFrame();
      return true;
    }
  }
}
