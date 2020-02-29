package ru.didim99.tstu.core.graphics.curve.builder;

import android.graphics.PointF;
import ru.didim99.tstu.core.graphics.curve.Curve;
import ru.didim99.tstu.core.graphics.curve.Point;
import ru.didim99.tstu.core.graphics.model.Vec4;

/**
 * Created by didim99 on 29.02.20.
 */
public class BSpline2Builder extends BaseBuilder {

  public BSpline2Builder(Curve curve) {
    super(curve);
  }

  @Override
  public boolean rebuild() {
    synchronized (renderLock) {
      int last = basePoints.size() - 2;
      if (last < 1) return false;

      int step = STEP_POINTS / last;
      double t, dt = T_MAX / step;
      for (int i = 0; i < last; i++) {
        Point p1 = basePoints.get(i);
        Point p2 = basePoints.get(i + 1);
        Point p3 = basePoints.get(i + 2);
        Vec4 gx = new Vec4(p1.getVisibleX(), p2.getVisibleX(), p3.getVisibleX());
        Vec4 gy = new Vec4(p1.getVisibleY(), p2.getVisibleY(), p3.getVisibleY());
        PointF prev = null;

        t = T_MIN;
        float x, y;
        int offset = i * step * 4;
        for (int index = 0; index < step * 4; index += 4) {
          x = (float) basis(gx, t);
          y = (float) basis(gy, t);
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

  // Values definition: P(i), P(i+1), P(i+2)
  private static double basis(Vec4 p, double t) {
    return (t * t / 2) * p.get(0)
      + (0.75 - Math.pow(t - 0.5, 2)) * p.get(1)
      + 0.5 * Math.pow(1 - t, 2) * p.get(2);
  }
}
