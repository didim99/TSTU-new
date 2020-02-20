package ru.didim99.tstu.core.graphics.curve;

import android.graphics.PointF;
import java.util.ArrayList;
import java.util.Collections;
import ru.didim99.tstu.core.graphics.curve.builder.BezierBuilder;
import ru.didim99.tstu.core.graphics.curve.builder.Builder;
import ru.didim99.tstu.core.graphics.curve.builder.HermiteBuilder;
import ru.didim99.tstu.core.graphics.curve.builder.LagrangeBuilder;

/**
 * Created by didim99 on 16.02.20.
 */
public class Curve {

  private static final class Type {
    private static final int LAGRANGE  = 0;
    private static final int BEZIER    = 1;
    private static final int HERMITE   = 2;
  }

  private int type;
  private boolean drawPoints, drawFrame;
  private boolean syncControls;
  private final Object renderLock;
  private ArrayList<Point> basePoints;
  private ArrayList<Point> controlPoints;
  private Point activePoint;
  private Builder builder;

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

  public Builder getBuilder() {
    return builder;
  }

  public boolean isEmpty() {
    return basePoints.isEmpty();
  }

  public boolean hasControlPoints() {
    return type == Type.HERMITE;
  }

  public void setType(int type) {
    this.type = type;
    switch (type) {
      case Type.LAGRANGE: builder = new LagrangeBuilder(this); break;
      case Type.BEZIER: builder = new BezierBuilder(this); break;
      case Type.HERMITE: builder = new HermiteBuilder(this); break;
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
      if (hasControlPoints()) {
        controlPoints.removeAll(point.getControls());
        onPointsChanged(false);
      }
    }
  }

  public boolean checkPoint(PointF position, boolean baseOnly) {
    for (Point point : basePoints)
      if (point.near(position)) return true;
    if (hasControlPoints() && !baseOnly) {
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

    double distance, minDistance = Point.TAP_DISTANCE * 1.5;
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
    if (type == Type.LAGRANGE) {
      Collections.sort(basePoints, Point::compareX);
    } else if (!movedOnly && type == Type.HERMITE) {
      ((HermiteBuilder) builder).computeArms();
    }
  }

  public void setActivePoint(PointF position) {
    if (position == null) {
      if (activePoint != null)
        activePoint.setActive(false);
      activePoint = null;
    } else {
      activePoint = getPoint(basePoints, position);
      if (activePoint == null && hasControlPoints())
        activePoint = getPoint(controlPoints, position);
      if (activePoint != null)
        activePoint.setActive(true);
    }
  }

  public void moveActivePoint(PointF position) {
    synchronized (renderLock) {
      activePoint.moveTo(position);
      onPointsChanged(true);
    }
  }
}
