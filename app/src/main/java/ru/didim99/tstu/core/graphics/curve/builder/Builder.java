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

  private Curve curve;
  final Object renderLock;
  ArrayList<Point> points;
  float[] factorBuffer;
  float[] pointsPuffer;
  float[] framePuffer;

  public Builder(Curve curve) {
    this.curve = curve;
    this.points = curve.getPoints();
    this.renderLock = curve.getRenderLock();
    this.pointsPuffer = new float[BUFFER_SIZE];
    this.factorBuffer = new float[0];
    this.framePuffer = new float[0];
    checkPointsBuffer();
    checkFrameBuffer();
  }

  public float[] getPointsPuffer() {
    return pointsPuffer;
  }

  public float[] getFramePuffer() {
    return framePuffer;
  }

  public int getFrameSize() {
    return (points.size() - 1) * 4;
  }

  public void checkPointsBuffer() {
    int bufferSize = points.size();
    if (factorBuffer.length < bufferSize)
      factorBuffer = new float[bufferSize];
  }

  public void checkFrameBuffer() {
    if (curve.isDrawFrame()) {
      int frameSize = getFrameSize();
      if (framePuffer.length < frameSize)
        framePuffer = new float[frameSize];
    }
  }

  void drawFrame() {
    if (!curve.isDrawFrame()) return;
    int last = points.size() - 1;
    int index = 0;

    for (int i = 0; i <= last; i++) {
      PointF point = points.get(i).getPosition();
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

  public abstract boolean rebuild();
}
