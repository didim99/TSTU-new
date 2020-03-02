package ru.didim99.tstu.core.graphics.curve.builder;

import android.graphics.PointF;
import ru.didim99.tstu.core.graphics.curve.Curve;
import ru.didim99.tstu.core.graphics.curve.Point;
import ru.didim99.tstu.core.graphics.math.Mat4;

/**
 * Created by didim99 on 25.02.20.
 */
public abstract class ControlPointBuilder extends BaseBuilder {

  ControlPointBuilder(Curve curve, Mat4 basisMatrix) {
    super(curve, true, basisMatrix);
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
