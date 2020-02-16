package ru.didim99.tstu.core.graphics.curve;

import android.graphics.PointF;
import java.util.ArrayList;
import java.util.Collections;

import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 16.02.20.
 */
public class Curve {
  private static final int STEP_POINTS = 1000;
  private static final int BUFFER_SIZE = (STEP_POINTS + 1) * 4;

  private static final class Type {
    private static final int LAGRANGE  = 0;
    private static final int BEZIER    = 1;
  }

  private int type;
  private ArrayList<Point> points;
  private final Object renderLock;
  private float[] factorBuffer;
  private float[] pointsPuffer;
  private Point activePoint;

  public Curve() {
    this.type = Type.LAGRANGE;
    this.renderLock = new Object();
    this.points = new ArrayList<>();
    this.pointsPuffer = new float[BUFFER_SIZE];
    this.factorBuffer = new float[0];
    this.activePoint = null;
  }

  public ArrayList<Point> getPoints() {
    return points;
  }

  public float[] getPointsPuffer() {
    return pointsPuffer;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
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
      int bufferSize = points.size();
      if (factorBuffer.length < bufferSize)
        factorBuffer = new float[bufferSize];
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

  public boolean rebuild() {
    switch (type) {
      case Type.LAGRANGE: return rebuildLagrange();
      case Type.BEZIER: return rebuildBezier();
      default: return false;
    }
  }

  private boolean rebuildLagrange() {
    synchronized (renderLock) {
      int size = points.size();
      if (size < 2) return false;

      for (int i = 1; i < size; i++) {
        Point point = points.get(i);
        if (point.xEquals(points.get(i - 1)))
          point.getPosition().x += 10;
      }

      for (int x1 = 0; x1 < size; x1++) {
        PointF p1 = points.get(x1).getPosition();
        factorBuffer[x1] = p1.y;

        for (int x2 = 0; x2 < size; x2++) {
          if (x1 == x2) continue;
          factorBuffer[x1] /= p1.x - points.get(x2).getPosition().x;
        }
      }

      float xStart = points.get(0).getPosition().x;
      float xEnd = points.get(size - 1).getPosition().x;
      float dx = (xEnd - xStart) / STEP_POINTS, tmp = 1;
      float x = xStart, y = points.get(0).getPosition().y;
      PointF prev = new PointF(x, y);

      int index = 0;
      while (index + 3 < pointsPuffer.length) {
        x += dx;
        y = 0;

        for (int x1 = 0; x1 < size; x1++, tmp = 1) {
          for (int x2 = 0; x2 < size; x2++) {
            if (x1 == x2) continue;
            tmp *= x - points.get(x2).getPosition().x;
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

      return true;
    }
  }

  private boolean rebuildBezier() {
    synchronized (renderLock) {
      int size = points.size();
      if (size < 3) return false;

      float t = 0, dt = 1f / STEP_POINTS;
      float x = points.get(0).getPosition().x;
      float y = points.get(0).getPosition().y;
      PointF prev = new PointF(x, y);

      for (int i = 0; i < size; i++)
        factorBuffer[i] = Utils.combination(size - 1, i);

      int index = 0;
      while (index + 3 < pointsPuffer.length) {
        t += dt;

        x = y = 0;
        for (int i = 0; i < size; i++) {
          PointF point = points.get(i).getPosition();
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

      return true;
    }
  }
}
