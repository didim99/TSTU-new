package ru.didim99.tstu.core.graphics.utils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by didim99 on 09.03.19.
 */
public class Axis extends Model {
  private static final int DEFAULT_COLOR = 0xff8080A0;
  private static final float DEFAULT_WIDTH = 2f;
  private static final Edge[] AXIS_EDGES =
    { new Edge(0, 1), new Edge(2, 3), new Edge(4, 5) };

  public Axis(double length, boolean drawNegative) {
    super(Type.EDGE, "Axis", DEFAULT_WIDTH, DEFAULT_COLOR);
    ArrayList<Edge> edges = new ArrayList<>(Arrays.asList(AXIS_EDGES));
    ArrayList<Vertex> vertices = new ArrayList<>();
    double start = drawNegative ? -length : 0;
    vertices.add(new Vertex(start, 0, 0));
    vertices.add(new Vertex(length, 0, 0));
    vertices.add(new Vertex(0, start, 0));
    vertices.add(new Vertex(0, length, 0));
    vertices.add(new Vertex(0, 0, start));
    vertices.add(new Vertex(0, 0, length));
    load(vertices, edges);
  }
}
