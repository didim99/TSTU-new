package ru.didim99.tstu.core.graphics.curve;

import android.graphics.PointF;
import java.util.ArrayList;
import java.util.Collections;
import ru.didim99.tstu.core.graphics.curve.builder.BSpline2Builder;
import ru.didim99.tstu.core.graphics.curve.builder.BSpline3Builder;
import ru.didim99.tstu.core.graphics.curve.builder.BaseBuilder;
import ru.didim99.tstu.core.graphics.curve.builder.BezierBuilder;
import ru.didim99.tstu.core.graphics.curve.builder.CatmullRomBuilder;
import ru.didim99.tstu.core.graphics.curve.builder.ControlPointBuilder;
import ru.didim99.tstu.core.graphics.curve.builder.CubicBezierBuilder;
import ru.didim99.tstu.core.graphics.curve.builder.HermiteBuilder;
import ru.didim99.tstu.core.graphics.curve.builder.LagrangeBuilder;

/**
 * Created by didim99 on 16.02.20.
 */
public class Curve {

  private static final class Type {
    private static final int LAGRANGE     = 0;
    private static final int BEZIER       = 1;
    private static final int HERMITE      = 2;
    private static final int CUBIC_BEZIER = 3;
    private static final int B_SPLINE2    = 4;
    private static final int B_SPLINE3    = 5;
    private static final int CATMULL_ROM  = 6;
  }

  private int type;
  private boolean drawPoints, drawFrame;
  private boolean syncControls;
  private final Object renderLock;
  private ArrayList<Point> basePoints;
  private ArrayList<Point> controlPoints;
  private Point activePoint;
  private BaseBuilder builder;

  public Curve() {
    this.drawPoints = true;
    this.drawFrame = true;
    this.syncControls = true;
    this.renderLock = new Object();
    this.basePoints = new ArrayList<>();
    this.controlPoints = new ArrayList<>();
    this.activePoint = null;
    setType(Type.LAGRANGE);
  }

  public Object getRenderLock() {
    return renderLock;
  }

  public ArrayList<Point> getBasePoints() {
    return basePoints;
  }

  public ArrayList<Point> getControlPoints() {
    return controlPoints;
  }

  public int getType() {
    return type;
  }

  public boolean isDrawPoints() {
    return drawPoints;
  }

  public boolean isDrawFrame() {
    return drawFrame;
  }

  public boolean isSyncControls() {
    return syncControls;
  }

  public BaseBuilder getBuilder() {
    return builder;
  }

  public boolean isEmpty() {
    return basePoints.isEmpty();
  }

  public void setType(int type) {
    this.type = type;
    switch (type) {
      case Type.LAGRANGE: builder = new LagrangeBuilder(this); break;
      case Type.BEZIER: builder = new BezierBuilder(this); break;
      case Type.HERMITE: builder = new HermiteBuilder(this); break;
      case Type.CUBIC_BEZIER: builder = new CubicBezierBuilder(this); break;
      case Type.B_SPLINE2: builder = new BSpline2Builder(this); break;
      case Type.B_SPLINE3: builder = new BSpline3Builder(this); break;
      case Type.CATMULL_ROM: builder = new CatmullRomBuilder(this); break;
    }

    onPointsChanged(false);
  }

  public void setDrawPoints(boolean drawPoints) {
    this.drawPoints = drawPoints;
    builder.checkArmBuffer();
  }

  public void setDrawFrame(boolean drawFrame) {
    this.drawFrame = drawFrame;
    builder.checkFrameBuffer();
  }

  public void setSyncControls(boolean syncControls) {
    this.syncControls = syncControls;
  }

  public boolean clear() {
    if (basePoints.isEmpty())
      return false;
    basePoints.clear();
    return true;
  }

  public void addPoint(PointF position) {
    synchronized (renderLock) {
      basePoints.add(new Point(position));
      onPointsChanged(false);
      builder.checkBuffers();
    }
  }

  public void removePoint(PointF position) {
    int removeIndex = getPointIndex(basePoints, position);
    if (removeIndex >= 0) {
      Point point = basePoints.remove(removeIndex);
      if (builder.hasControlPoints()) {
        controlPoints.removeAll(point.getControls());
        onPointsChanged(false);
      }
    }
  }

  public boolean checkPoint(PointF position, boolean baseOnly) {
    for (Point point : basePoints)
      if (point.near(position)) return true;
    if (builder.hasControlPoints() && !baseOnly) {
      for (Point point : controlPoints)
        if (point.near(position)) return true;
    }

    return false;
  }

  private Point getPoint(ArrayList<Point> points, PointF position) {
    int index = getPointIndex(points, position);
    return index >= 0 ? points.get(index) : null;
  }

  private int getPointIndex(ArrayList<Point> points, PointF position) {
    int index = -1;

    double distance, minDistance = Point.TAP_DISTANCE;
    for (int i = 0; i < points.size(); i++) {
      distance = points.get(i).distance(position);
      if (distance < minDistance) {
        minDistance = distance;
        index = i;
      }
    }

    return index;
  }

  private void onPointsChanged(boolean movedOnly) {
    if (!movedOnly) builder.clearBuffers();
    if (type == Type.LAGRANGE) {
      Collections.sort(basePoints, Point::compareX);
    } else if (!movedOnly && builder.hasControlPoints()) {
      ((ControlPointBuilder) builder).computeArms();
    }
  }

  public void setActivePoint(PointF position) {
    if (position == null) {
      if (activePoint != null)
        activePoint.setActive(false);
      activePoint = null;
    } else {
      activePoint = getPoint(basePoints, position);
      if (activePoint == null && builder.hasControlPoints())
        activePoint = getPoint(controlPoints, position);
      if (activePoint != null)
        activePoint.setActive(true);
    }
  }

  public void moveActivePoint(PointF position) {
    synchronized (renderLock) {
      if (activePoint.isControlPoint())
        ((ControlPointBuilder) builder).moveControlPoint(activePoint, position);
      else activePoint.moveTo(position);
      onPointsChanged(true);
    }
  }
}
