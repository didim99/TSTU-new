package ru.didim99.tstu.core.graphics.curve.builder;

import android.graphics.PointF;
import ru.didim99.tstu.core.graphics.curve.Curve;
import ru.didim99.tstu.core.graphics.curve.Point;

/**
 * Created by didim99 on 20.02.20.
 */
public class LagrangeBuilder extends Builder {

  public LagrangeBuilder(Curve curve) {
    super(curve);
  }

  @Override
  public boolean rebuild() {
    synchronized (renderLock) {
      int size = basePoints.size();
      if (size < 2) return false;

      for (int i = 1; i < size; i++) {
        Point point = basePoints.get(i);
        if (point.xEquals(basePoints.get(i - 1)))
          point.getPosition().x += 10;
      }

      for (int x1 = 0; x1 < size; x1++) {
        PointF p1 = basePoints.get(x1).getPosition();
        factorBuffer[x1] = p1.y;

        for (int x2 = 0; x2 < size; x2++) {
          if (x1 == x2) continue;
          factorBuffer[x1] /= p1.x - basePoints.get(x2).getVisibleX();
        }
      }

      float xStart = basePoints.get(0).getVisibleX();
      float xEnd = basePoints.get(size - 1).getVisibleX();
      float dx = (xEnd - xStart) / STEP_POINTS, tmp = 1;
      float x = xStart, y = basePoints.get(0).getVisibleY();
      PointF prev = new PointF(x, y);

      int index = 0;
      while (index + 3 < pointsPuffer.length) {
        x += dx;
        y = 0;

        for (int x1 = 0; x1 < size; x1++, tmp = 1) {
          for (int x2 = 0; x2 < size; x2++) {
            if (x1 == x2) continue;
            tmp *= x - basePoints.get(x2).getVisibleX();
          }

          y += factorBuffer[x1] * tmp;
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
