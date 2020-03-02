package ru.didim99.tstu.core.graphics.curve.builder;

import android.graphics.PointF;
import ru.didim99.tstu.core.graphics.math.Mat4;
import ru.didim99.tstu.core.graphics.math.Vec4;

/**
 * Created by didim99 on 02.03.20.
 */
class CubicInterpolator {

  private Mat4 basisMatrix;
  private float[] pointsPuffer;
  private int step;
  private double dt;

  CubicInterpolator(float[] pointsPuffer, Mat4 basisMatrix) {
    this.pointsPuffer = pointsPuffer;
    this.basisMatrix = basisMatrix;
  }

  void setLastPointIndex(int index) {
    this.step = BaseBuilder.STEP_POINTS / index;
    this.dt = BaseBuilder.T_MAX / step;
  }

  void interpolate(int i, Vec4 gx, Vec4 gy) {
    double t = BaseBuilder.T_MIN;
    Vec4 vt = new Vec4();
    PointF prev = null;

    float x, y;
    int offset = i * step * 4;
    for (int index = 0; index < step * 4; index += 4) {
      vt.set(t * t * t, t * t, t, 1);
      vt = vt.multiply(basisMatrix);
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
}
