package ru.didim99.tstu.core.graphics.utils;

import android.graphics.PointF;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 09.03.19.
 */
public class Model {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_EM";
  private static final int DEFAULT_COLOR = 0xff000000;
  private static final float DEFAULT_WIDTH = 2f;
  public static final String FILE_MASK = ".obj";

  private static final String SET_PART = " ";
  private static final String SET_VERTEX = "/";
  private static final String L_COMMENT = "#";
  private static final String L_OBJECT = "o";
  private static final String L_VERTEX = "v";
  private static final String L_TEXEL = "vt";
  private static final String L_FACE = "f";

  public static final class Type {
    public static final int EDGE = 1;
    public static final int FACE = 2;
  }

  private int type;
  private String name;
  ArrayList<Vertex> vertices;
  ArrayList<Edge> edges;
  float[] rastered;
  private ArrayList<PointF> texels;
  private ArrayList<Face> faces;
  private float width;
  private int color;

  private Model() {
    this(Type.EDGE, null, DEFAULT_WIDTH, DEFAULT_COLOR);
  }

  Model(int type, String name, float width, int color) {
    vertices = new ArrayList<>();
    texels = new ArrayList<>();
    faces = new ArrayList<>();
    edges = new ArrayList<>();
    rastered = new float[0];
    this.width = width;
    this.color = color;
    this.type = type;
    this.name = name;
  }

  public int getType() {
    return type;
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

  public ArrayList<Vertex> getVertices() {
    return vertices;
  }

  public ArrayList<PointF> getTexels() {
    return texels;
  }

  public ArrayList<Face> getFaces() {
    return faces;
  }

  public void render(Mat4 transform, Projection p) {
    for (Vertex v : vertices)
      v.render(transform, p);
    /*if (type == Type.FACE)
      Collections.sort(faces, this::faceCompare);*/

    int i = 0;
    PointF p1, p2;
    for (Edge e : edges) {
      p1 = vertices.get(e.v1).rastered;
      p2 = vertices.get(e.v2).rastered;
      rastered[i]   = p1.x;
      rastered[i+1] = p1.y;
      rastered[i+2] = p2.x;
      rastered[i+3] = p2.y;
      i += 4;
    }
  }

  private int faceCompare(Face f1, Face f2) {
    Vertex v1 = vertices.get(f1.v1);
    Vertex v2 = vertices.get(f1.v2);
    Vertex v3 = vertices.get(f1.v3);
    double z1 = (v1.depth() + v2.depth() + v3.depth()) / 3;
    v1 = vertices.get(f2.v1);
    v2 = vertices.get(f2.v2);
    v3 = vertices.get(f2.v3);
    double z2 = (v1.depth() + v2.depth() + v3.depth()) / 3;
    return Double.compare(z2, z1);
  }

  public static Model load(String fileName)
    throws IOException, ParserException {
    ArrayList<String> src = Utils.readFile(fileName);
    MyLog.d(LOG_TAG, "Loading OBJ data");
    Model model = new Model();
    ArrayList<PointF> texels = model.texels;
    ArrayList<Vertex> vertices = model.vertices;
    ArrayList<Face> faces = model.faces;
    ArrayList<Edge> edges = model.edges;
    boolean useTextures = false;
    int[] vt = new int[3];
    int[] v = new int[3];

    try {
      int count;
      String[] parts, part;
      for (String line : src) {
        if (line.isEmpty()) continue;
        if (line.startsWith(L_COMMENT)) continue;
        parts = line.split(SET_PART);
        switch (parts[0]) {
          case L_OBJECT:
            model.name = parts[1];
            break;
          case L_VERTEX:
            vertices.add(new Vertex(
              Double.parseDouble(parts[1]),
              Double.parseDouble(parts[2]),
              Double.parseDouble(parts[3])));
            break;
          case L_TEXEL:
            useTextures = true;
            texels.add(new PointF(
              Float.parseFloat(parts[1]),
              Float.parseFloat(parts[2])
            ));
            break;
          case L_FACE:
            count = parts.length - 1;
            if (useTextures) {
              for (int i = 1; i <= count; i++) {
                part = parts[i].split(SET_VERTEX);
                v[i - 1] = Integer.parseInt(part[0]) - 1;
                vt[i - 1] = Integer.parseInt(part[1]) - 1;
              }
              faces.add(new Face(v, vt));
              for (int i = 0; i < count - 1; i++)
                addEdge(edges, v[i], v[i+1]);
              addEdge(edges, v[0], v[count - 1]);
            } else {
              for (int i = 1; i < count; i++)
                addEdge(edges, parts[i], parts[i+1]);
              addEdge(edges, parts[1], parts[count]);
            }
            break;
        }
      }
    } catch (NumberFormatException
      | ArrayIndexOutOfBoundsException e) {
      throw new ParserException("Incorrect input data", e);
    }

    model.rastered = new float[edges.size() * 4];
    model.type = useTextures ? Type.FACE : Type.EDGE;
    MyLog.d(LOG_TAG, "Data loaded (name: " + model.name
      + ", vertices: " + vertices.size() + ", edges: " + edges.size() + ")");
    return model;
  }

  private static void addEdge(ArrayList<Edge> data, String v1, String v2) {
    Edge e = new Edge(Integer.parseInt(v1) - 1, Integer.parseInt(v2) - 1);
    if (!data.contains(e)) data.add(e);
  }

  private static void addEdge(ArrayList<Edge> data, int v1, int v2) {
    Edge e = new Edge(v1, v2);
    if (!data.contains(e)) data.add(e);
  }

  public static class ParserException extends Exception {
    private ParserException(String msg, Throwable cause) { super(msg, cause); }
  }
}
