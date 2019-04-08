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

  void transform(Mat4 transform) {
    transformed = world.multiply(transform);
  }

  void render(Mat4 transform, Projection p) {
    transform(transform);
    rastered = p.project(transformed);
  }

  public Vec4 transformed() {
    return transformed;
  }

  public Vec4 rastered() {
    return new Vec4(rastered.x, rastered.y, transformed.z());
  }
}
