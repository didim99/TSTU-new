package ru.didim99.tstu.core.graphics.curve;

import android.graphics.PointF;
import java.util.ArrayList;
import java.util.Collections;
import ru.didim99.tstu.core.graphics.curve.builder.BezierBuilder;
import ru.didim99.tstu.core.graphics.curve.builder.Builder;
import ru.didim99.tstu.core.graphics.curve.builder.LagrangeBuilder;

/**
 * Created by didim99 on 16.02.20.
 */
public class Curve {

  private static final class Type {
    private static final int LAGRANGE  = 0;
    private static final int BEZIER    = 1;
  }

  private int type;
  private boolean drawFrame;
  private final Object renderLock;
  private ArrayList<Point> points;
  private Point activePoint;
  private Builder builder;

  public Curve() {
    this.drawFrame = true;
    this.renderLock = new Object();
    this.points = new ArrayList<>();
    this.activePoint = null;
    setType(Type.LAGRANGE);
  }

  public Object getRenderLock() {
    return renderLock;
  }

  public ArrayList<Point> getPoints() {
    return points;
  }

  public int getType() {
    return type;
  }

  public boolean isDrawFrame() {
    return drawFrame;
  }

  public Builder getBuilder() {
    return builder;
  }

  public void setType(int type) {
    switch (type) {
      case Type.LAGRANGE: builder = new LagrangeBuilder(this); break;
      case Type.BEZIER: builder = new BezierBuilder(this); break;
    }
    this.type = type;
    onPointsChanged();
  }

  public void setDrawFrame(boolean drawFrame) {
    this.drawFrame = drawFrame;
    builder.checkFrameBuffer();
    onPointsChanged();
  }

  public boolean clear() {
    if (points.isEmpty())
      return false;
    points.clear();
    return true;
  }

  public void addPoint(PointF position) {
    synchronized (renderLock) {
      points.add(new Point(position));
      builder.checkPointsBuffer();
      builder.checkFrameBuffer();
      onPointsChanged();
    }
  }

  public void removePoint(PointF position) {
    int removeIndex = getPointIndex(position);
    if (removeIndex >= 0) points.remove(removeIndex);
  }

  public boolean checkPoint(PointF position) {
    for (Point point : points)
      if (point.nearest(position)) return true;
    return false;
  }

  private Point getPoint(PointF position) {
    int index = getPointIndex(position);
    return index >= 0 ? points.get(index) : null;
  }

  private int getPointIndex(PointF position) {
    int index = -1;

    double distance, minDistance = Double.POSITIVE_INFINITY;
    for (int i = 0; i < points.size(); i++) {
      distance = points.get(i).distance(position);
      if (distance < minDistance) {
        minDistance = distance;
        index = i;
      }
    }

    return index;
  }

  private void onPointsChanged() {
    if (type == Type.LAGRANGE) {
      Collections.sort(points, Point::compareX);
    }
  }

  public void setActivePoint(PointF position) {
    if (position == null) {
      if (activePoint != null)
        activePoint.setActive(false);
      activePoint = null;
    } else {
      activePoint = getPoint(position);
      if (activePoint != null)
        activePoint.setActive(true);
    }
  }

  public void moveActivePoint(PointF position) {
    synchronized (renderLock) {
      activePoint.getPosition().set(position);
      onPointsChanged();
    }
  }
}
