package ru.didim99.tstu.core.graphics.utils;

import android.graphics.PointF;

/**
 * Created by didim99 on 05.04.19.
 */
public class Vertex {
  private Vec4 world, transformed;
  public PointF rastered;

  Vertex(double x, double y, double z) {
    world = new Vec4(x, y, z);
  }

  void render(Mat4 transform, Projection p) {
    transformed = world.multiply(transform);
    rastered = p.project(transformed);
  }

  double depth() { return transformed.z(); }

  public Vec4 rastered() {
    return new Vec4(rastered.x, rastered.y, transformed.z());
  }
}
