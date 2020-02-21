package ru.didim99.tstu.core.graphics.curve.builder;

import android.graphics.PointF;
import ru.didim99.tstu.core.graphics.curve.Curve;
import ru.didim99.tstu.core.graphics.curve.Point;
import ru.didim99.tstu.core.graphics.model.Mat4;
import ru.didim99.tstu.core.graphics.model.Vec4;

/**
 * Created by didim99 on 20.02.20.
 */
public class HermiteBuilder extends Builder {
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

  public void computeArms() {
    int last = basePoints.size() - 1;

    if (last == 0) {
      basePoints.get(0).clearControls();
      controlPoints.clear();
      return;
    }

    boolean pointsAdded = false;
    for (int i = 0; i <= last; i++) {
      Point current = basePoints.get(i);
      PointF controlPos;

      Point cn = current.getControlNext();
      if (i == last && cn != null) {
        current.setControlNext(null);
        controlPoints.remove(cn);
      } else if (i < last && cn == null) {
        controlPos = getControlPos(current, i, false);
        cn = new Point(controlPos, current);
        current.setControlNext(cn);
        controlPoints.add(cn);
        pointsAdded = true;
      }

      Point cp = current.getControlPrev();
      if (i == 0 && cp != null) {
        current.setControlPrev(null);
        controlPoints.remove(cp);
      } else if (i > 0 && cp == null) {
        controlPos = getControlPos(current, i, true);
        cp = new Point(controlPos, current);
        current.setControlPrev(cp);
        controlPoints.add(cp);
        pointsAdded = true;
      }
    }

    if (pointsAdded) checkArmBuffer();
  }

  private PointF getControlPos(Point p1, int i, boolean reverse) {
    Point control = reverse ? p1.getControlNext() : p1.getControlPrev();
    Point p2 = basePoints.get(i + (reverse ? -1 : 1));
    if (control != null) return new PointF(
      control.getReverseX(), control.getReverseY());
    else return new PointF(
      (p1.getVisibleX() * 3 + p2.getVisibleX()) / 4,
      (p1.getVisibleY() * 3 + p2.getVisibleY()) / 4
    );
  }

  public void moveControlPoint(Point activePoint, PointF position) {
    Point basePoint = activePoint.getParent();
    if (curve.isSyncControls() && basePoint.isMultiControl()) {
      double angle = getAngle(position, basePoint)
       - getAngle(activePoint.getPosition(), basePoint);
      basePoint.getSlaveControl(activePoint).rotate(angle);
    }

    activePoint.moveTo(position);
  }

  private double getAngle(PointF point, Point base) {
    double dx = (point.x - base.getVisibleX());
    double dy = (point.y - base.getVisibleY());
    return Math.atan2(dx, dy);
  }
}
