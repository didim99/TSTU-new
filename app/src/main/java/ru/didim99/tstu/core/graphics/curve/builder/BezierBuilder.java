package ru.didim99.tstu.core.graphics.curve.builder;

import android.graphics.PointF;
import ru.didim99.tstu.core.graphics.curve.Curve;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 20.02.20.
 */
public class BezierBuilder extends Builder {

  public BezierBuilder(Curve curve) {
    super(curve);
  }

  @Override
  public boolean rebuild() {
    synchronized (renderLock) {
      int size = basePoints.size();
      if (size < 3) return false;

      float t = 0, dt = 1f / STEP_POINTS;
      float x = basePoints.get(0).getVisibleX();
      float y = basePoints.get(0).getVisibleY();
      PointF prev = new PointF(x, y);

      for (int i = 0; i < size; i++)
        factorBuffer[i] = Utils.combination(size - 1, i);

      int index = 0;
      while (index + 3 < pointsPuffer.length) {
        t += dt;

        x = y = 0;
        for (int i = 0; i < size; i++) {
          PointF point = basePoints.get(i).getPosition();
          double tmp = (factorBuffer[i] * Math.pow(t, i))
            * Math.pow(1 - t, size - 1 - i);
          x += tmp * point.x;
          y += tmp * point.y;
        }

        pointsPuffer[index]     = prev.x;
        pointsPuffer[index + 1] = prev.y;
        pointsPuffer[index + 2] = x;
        pointsPuffer[index + 3] = y;
        prev.set(x, y);
        index += 4;
      }

      drawFrame();
      return true;
    }
  }
}
