package ru.didim99.tstu.core.graphics.curve.builder;

import android.graphics.PointF;
import java.util.ArrayList;
import java.util.Arrays;
import ru.didim99.tstu.core.graphics.curve.Curve;
import ru.didim99.tstu.core.graphics.curve.Point;
import ru.didim99.tstu.core.graphics.model.Mat4;

/**
 * Created by didim99 on 20.02.20.
 */
public abstract class BaseBuilder {
  static final float T_MIN = 0f;
  static final float T_MAX = 1f;
  static final int STEP_POINTS = 1000;
  private static final int BUFFER_SIZE = (STEP_POINTS + 1) * 4;

  Curve curve;
  final Object renderLock;
  ArrayList<Point> basePoints;
  ArrayList<Point> controlPoints;
  CubicInterpolator interpolator;
  private boolean hasControlPoints;
  // Internal drawing buffers
  float[] factorBuffer, pointsPuffer;
  private float[] framePuffer;
  private float[] armBuffer;

  BaseBuilder(Curve curve) {
    this(curve, false, null);
  }

  BaseBuilder(Curve curve, boolean hasControlPoints, Mat4 basisMatrix) {
    this.curve = curve;
    this.hasControlPoints = hasControlPoints;
    this.renderLock = curve.getRenderLock();
    this.basePoints = curve.getBasePoints();
    this.controlPoints = curve.getControlPoints();
    this.pointsPuffer = new float[BUFFER_SIZE];
    this.factorBuffer = new float[0];
    this.framePuffer = new float[0];
    if (hasControlPoints)
      this.armBuffer = new float[0];
    if (basisMatrix != null) {
      interpolator = new CubicInterpolator(
        pointsPuffer, basisMatrix);
    }

    checkBuffers();
  }

  public float[] getPointsPuffer() {
    return pointsPuffer;
  }

  public float[] getFramePuffer() {
    return framePuffer;
  }

  public float[] getArmBuffer() {
    return armBuffer;
  }

  public int getFrameBufferSize() {
    return (basePoints.size() - 1) * 4;
  }

  public int getArmBufferSize() {
    return controlPoints.size() * 4;
  }

  public boolean hasControlPoints() {
    return hasControlPoints;
  }

  public void clearBuffers() {
    Arrays.fill(pointsPuffer, 0f);
  }

  public void checkBuffers() {
    int bufferSize = basePoints.size();
    if (factorBuffer.length < bufferSize)
      factorBuffer = new float[bufferSize];
    checkFrameBuffer();
    checkArmBuffer();
  }

  public void checkFrameBuffer() {
    if (curve.isDrawFrame()) {
      int frameSize = getFrameBufferSize();
      if (framePuffer.length < frameSize)
        framePuffer = new float[frameSize];
    }
  }

  public void checkArmBuffer() {
    if (hasControlPoints && curve.isDrawPoints()) {
      int frameSize = getArmBufferSize();
      if (armBuffer.length < frameSize)
        armBuffer = new float[frameSize];
    }
  }

  void drawFrame() {
    if (!curve.isDrawFrame()) return;
    int last = basePoints.size() - 1;
    int index = 0;

    for (int i = 0; i <= last; i++) {
      PointF point = basePoints.get(i).getPosition();
      framePuffer[index]      = point.x;
      framePuffer[index + 1]  = point.y;
      if (i > 0 && i < last) {
        framePuffer[index + 2] = point.x;
        framePuffer[index + 3] = point.y;
        index += 2;
      }
      index += 2;
    }
  }

  void drawArms() {
    if (!curve.isDrawPoints()) return;
    if (controlPoints.size() < 2) return;
    int index = 0;

    for (Point point : controlPoints) {
      armBuffer[index]      = point.getVisibleX();
      armBuffer[index + 1]  = point.getVisibleY();
      armBuffer[index + 2]  = point.getParent().getVisibleX();
      armBuffer[index + 3]  = point.getParent().getVisibleY();
      index += 4;
    }
  }

  public abstract boolean rebuild();
}
