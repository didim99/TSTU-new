package ru.didim99.tstu.core.graphics.utils;

import android.graphics.PointF;

/**
 * Created by didim99 on 05.04.19.
 */
public class TexturedVertex {
  public float x, y, z, u, v;

  public TexturedVertex(Vec4 v, PointF t) {
    this.x = (float) v.x();
    this.y = (float) v.y();
    this.z = (float) v.z();
    this.u = t.x;
    this.v = t.y;
  }

  public TexturedVertex(PointF v, PointF t) {
    this.x = v.x;
    this.y = v.y;
    this.u = t.x;
    this.v = t.y;
  }
}
