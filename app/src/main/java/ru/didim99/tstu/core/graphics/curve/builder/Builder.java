package ru.didim99.tstu.core.graphics.curve.builder;

import android.graphics.PointF;
import java.util.ArrayList;
import ru.didim99.tstu.core.graphics.curve.Curve;
import ru.didim99.tstu.core.graphics.curve.Point;

/**
 * Created by didim99 on 20.02.20.
 */
public abstract class Builder {
  static final int STEP_POINTS = 1000;
  private static final int BUFFER_SIZE = (STEP_POINTS + 1) * 4;

  Curve curve;
  final Object renderLock;
  ArrayList<Point> basePoints;
  ArrayList<Point> controlPoints;
  // Internal drawing buffers
  float[] factorBuffer, pointsPuffer;
  private float[] framePuffer;
  private float[] armBuffer;

  public Builder(Curve curve) {
    this.curve = curve;
    this.renderLock = curve.getRenderLock();
    this.basePoints = curve.getBasePoints();
    this.controlPoints = curve.getControlPoints();
    this.pointsPuffer = new float[BUFFER_SIZE];
    this.factorBuffer = new float[0];
    this.framePuffer = new float[0];
    if (curve.hasControlPoints())
      this.armBuffer = new float[0];
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
    if (curve.hasControlPoints() && curve.isDrawPoints()) {
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
