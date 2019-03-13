package ru.didim99.tstu.core.graphics.utils;

import android.graphics.PointF;
import java.io.IOException;
import java.util.ArrayList;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 09.03.19.
 */
public class EdgeModel {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_EM";
  private static final int DEFAULT_COLOR = 0xff000000;
  private static final float DEFAULT_WIDTH = 2f;
  public static final String FILE_MASK = ".obj";

  private static final String SET_PART = " ";
  private static final String L_COMMENT = "#";
  private static final String L_OBJECT = "o";
  private static final String L_VERTEX = "v";
  private static final String L_FACE = "f";

  String name;
  ArrayList<Vec4> vertices;
  ArrayList<Edge> edges;
  private float[] rastered;
  private float width;
  private int color;

  private EdgeModel() {
    this(DEFAULT_WIDTH, DEFAULT_COLOR);
  }

  EdgeModel(float width, int color) {
    vertices = new ArrayList<>();
    edges = new ArrayList<>();
    rastered = new float[0];
    this.width = width;
    this.color = color;
  }

  public int getColor() {
    return color;
  }

  public float getLineWidth() {
    return width;
  }

  public float[] getRastered() {
    return rastered;
  }

  public void render(Mat4 transform, Projection p) {
    ArrayList<Vec4> transformed = new ArrayList<>();
    rastered = new float[edges.size() * 4];
    for (Vec4 v : vertices)
      transformed.add(v.multiply(transform));
    int i = 0;
    for (Edge e : edges) {
      PointF p1 = p.project(transformed.get(e.v1()));
      PointF p2 = p.project(transformed.get(e.v2()));
      rastered[i]   = p1.x;
      rastered[i+1] = p1.y;
      rastered[i+2] = p2.x;
      rastered[i+3] = p2.y;
      i += 4;
    }
  }

  public static EdgeModel loadOBJ(String fileName)
    throws IOException, ParserException {
    ArrayList<String> src = Utils.readFile(fileName);
    MyLog.d(LOG_TAG, "Loading OBJ data");
    EdgeModel model = new EdgeModel();
    ArrayList<Vec4> vertices = model.vertices;
    ArrayList<Edge> edges = model.edges;

    try {
      int count;
      String[] parts;
      for (String line : src) {
        if (line.isEmpty()) continue;
        if (line.startsWith(L_COMMENT)) continue;
        parts = line.split(SET_PART);
        switch (parts[0]) {
          case L_OBJECT:
            model.name = parts[1];
            break;
          case L_VERTEX:
            vertices.add(new Vec4(
              Double.parseDouble(parts[1]),
              Double.parseDouble(parts[2]),
              Double.parseDouble(parts[3])));
            break;
          case L_FACE:
            count = parts.length - 1;
            for (int i = 1; i < count; i++)
              addEdge(edges, parts[i], parts[i+1]);
            addEdge(edges, parts[1], parts[count]);
            break;
        }
      }
    } catch (NumberFormatException
      | ArrayIndexOutOfBoundsException e) {
      throw new ParserException("Incorrect input data", e);
    }

    MyLog.d(LOG_TAG, "Data loaded (name: " + model.name
      + ", vertices: " + vertices.size() + ", edges: " + edges.size() + ")");
    return model;
  }

  private static void addEdge(ArrayList<Edge> data, String v1, String v2) {
    Edge e = new Edge(Integer.parseInt(v1) - 1, Integer.parseInt(v2) - 1);
    if (!data.contains(e)) data.add(e);
  }

  public static class ParserException extends Exception {
    private ParserException(String msg, Throwable cause) { super(msg, cause); }
  }
}
