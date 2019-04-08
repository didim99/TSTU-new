package ru.didim99.tstu.core.graphics.utils;

import android.graphics.PointF;
import java.io.IOException;
import java.util.ArrayList;
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
  private static final String L_NORMAL = "vn";
  private static final String L_TEXEL = "vt";
  private static final String L_FACE = "f";

  public static final class Type {
    public static final int EDGE = 1;
    public static final int FACE = 2;
  }

  private int type;
  private String name;
  private ArrayList<Vertex> vertices;
  private ArrayList<Vertex> normals;
  private ArrayList<PointF> texels;
  private ArrayList<Edge> edges;
  private ArrayList<Face> faces;
  private float[] rastered;
  private float width;
  private int color;

  private Model() {
    this(Type.EDGE, null, DEFAULT_WIDTH, DEFAULT_COLOR);
  }

  Model(int type, String name, float width, int color) {
    vertices = new ArrayList<>();
    normals = new ArrayList<>();
    texels = new ArrayList<>();
    faces = new ArrayList<>();
    edges = new ArrayList<>();
    rastered = new float[0];
    this.width = width;
    this.color = color;
    this.type = type;
    this.name = name;
  }

  void load(ArrayList<Vertex> vertices, ArrayList<Edge> edges) {
    this.rastered = new float[edges.size() * 4];
    this.vertices = vertices;
    this.edges = edges;
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

  public ArrayList<Face> getFaces() {
    return faces;
  }

  public void render(Mat4 transform, Projection p) {
    for (Vertex v : vertices)
      v.render(transform, p);

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

  public void rotateNormals(Mat4 transform) {
    for (Vertex n : normals) n.transform(transform);
  }

  public static Model load(String path)
    throws IOException, ParserException {
    ArrayList<String> src = Utils.readFile(path);
    MyLog.d(LOG_TAG, "Loading OBJ data");
    Model model = new Model();

    ArrayList<Vertex> vertices = model.vertices;
    ArrayList<Vertex> normals = model.normals;
    ArrayList<PointF> texels = model.texels;
    ArrayList<Face> faces = model.faces;
    ArrayList<Edge> edges = model.edges;
    int[] v = new int[] {0, 0, 0};
    int[] n = new int[] {0, 0, 0};
    int[] t = new int[] {0, 0, 0};
    boolean useTextures = false;
    boolean useNormals = false;

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
          case L_NORMAL:
            useNormals = true;
            normals.add(new Vertex(
              Double.parseDouble(parts[1]),
              Double.parseDouble(parts[2]),
              Double.parseDouble(parts[3])));
            break;
          case L_TEXEL:
            useTextures = true;
            texels.add(new PointF(
              Float.parseFloat(parts[1]),
              Float.parseFloat(parts[2])));
            break;
          case L_FACE:
            count = parts.length - 1;
            if (useTextures || useNormals) {
              for (int i = 1; i <= count; i++) {
                part = parts[i].split(SET_VERTEX);
                v[i - 1] = Integer.parseInt(part[0]) - 1;
                if (useTextures)
                  t[i - 1] = Integer.parseInt(part[1]) - 1;
                if (useNormals)
                  n[i - 1] = Integer.parseInt(part[2]) - 1;
              }
              faces.add(new Face(v, n, t));
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

    for (Face face : faces) {
      face.linkVertex(vertices);
      if (useNormals) face.linkNormal(normals);
      if (useTextures) face.linkTexture(texels);
    }

    model.rastered = new float[edges.size() * 4];
    model.type = useNormals ? Type.FACE : Type.EDGE;
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
