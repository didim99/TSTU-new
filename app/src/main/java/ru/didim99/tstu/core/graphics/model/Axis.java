package ru.didim99.tstu.core.graphics.model;

/**
 * Created by didim99 on 09.03.19.
 */
public class Axis extends Model {
  private static final int DEFAULT_COLOR = 0xff8080A0;
  private static final float DEFAULT_WIDTH = 2f;

  public Axis(double length, boolean drawNegative) {
    super(Type.EDGE, "Axis", DEFAULT_WIDTH, DEFAULT_COLOR);
    double start = drawNegative ? -length : 0;
    addVertex(new Vec4(start, 0, 0));
    addVertex(new Vec4(length, 0, 0));
    addVertex(new Vec4(0, start, 0));
    addVertex(new Vec4(0, length, 0));
    addVertex(new Vec4(0, 0, start));
    addVertex(new Vec4(0, 0, length));
    for (int i = 0; i < 3; i++)
      addEdge(i * 2, i * 2 + 1);
  }
}
