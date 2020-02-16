package ru.didim99.tstu.core.graphics.model;

import android.graphics.PointF;

/**
 * Vertex data holder for raster processing
 * Created by didim99 on 05.04.19.
 */
public class VertexHolder {
  public float x, y, z, u, v;
  public float nx, ny, nz, c;

  public VertexHolder(Vec4 v, PointF t) {
    this.x = (float) v.x();
    this.y = (float) v.y();
    this.z = (float) v.z();
    this.u = t.x;
    this.v = t.y;
  }

  public VertexHolder(PointF v, PointF t) {
    this.x = v.x;
    this.y = v.y;
    this.u = t.x;
    this.v = t.y;
  }

  public VertexHolder(Vec4 v, Vec4 n) {
    this.x = (float) v.x();
    this.y = (float) v.y();
    this.z = (float) v.z();
    this.nx = (float) n.x();
    this.ny = (float) n.y();
    this.nz = (float) n.z();
    this.c = 0;
  }
}
