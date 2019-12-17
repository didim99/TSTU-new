package ru.didim99.tstu.core.graphics.utils;

import android.graphics.Point;
import android.graphics.PointF;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 09.03.19.
 */
public class Model {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_EM";
  private static final String FILE_CREATOR = "# Created by ru.didim99.tstu";
  private static final String FILE_INFO = "# OBJ file: \'%s\'";
  private static final int DEFAULT_COLOR = 0xff000000;
  private static final float DEFAULT_WIDTH = 2f;
  public static final String FILE_MASK = ".obj";

  private static final String SEP_PART = " ";
  private static final String SEP_VERTEX = "/";
  private static final String L_COMMENT = "#";
  private static final String L_OBJECT = "o";
  private static final String L_VERTEX = "v";
  private static final String L_NORMAL = "vn";
  private static final String L_TEXEL = "vt";
  private static final String L_SOLID = "s";
  private static final String L_EDGE = "l";
  private static final String L_FACE = "f";
  private static final String V_OFF = "off";

  public static final class Type {
    public static final int EDGE = 1;
    public static final int FACE = 2;
  }

  private int type;
  private String name;
  private final ArrayList<Vertex> vertices;
  private final ArrayList<Vertex> vNormals;
  private final ArrayList<Vertex> normals;
  private final ArrayList<PointF> texels;
  private final ArrayList<Edge> edges;
  private final ArrayList<Face> faces;
  private boolean useVNormals;
  private float[] rastered;
  private float width;
  private int color;
  private double kd;

  public Model() {
    this(Type.EDGE, null, DEFAULT_WIDTH, DEFAULT_COLOR);
  }

  Model(int type, String name, float width, int color) {
    vertices = new ArrayList<>();
    vNormals = new ArrayList<>();
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

  private void configure() {
    int size = getRasteredSize();
    if (rastered.length < size)
      rastered = new float[size];
  }

  public void clearData() {
    vertices.clear();
    edges.clear();
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

  public int getRasteredSize() {
    return edges.size() * 4;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setKd(double kd) {
    this.kd = kd;
  }

  public void render(Mat4 transform, Projection p, Point center) {
    synchronized (vertices) {
      for (Vertex v : vertices)
        v.render(transform, p, center);
    }

    int i = 0;
    PointF p1, p2;
    configure();

    synchronized (edges) {
      for (Edge e : edges) {
        p1 = vertices.get(e.v1).rastered;
        p2 = vertices.get(e.v2).rastered;
        rastered[i] = p1.x;
        rastered[i + 1] = p1.y;
        rastered[i + 2] = p2.x;
        rastered[i + 3] = p2.y;
        i += 4;
      }
    }
  }

  public void rotateNormals(Mat4 transform) {
    ArrayList<Vertex> list = useVNormals ? vNormals : normals;
    for (Vertex n : list) n.transform(transform);
  }

  public void calcLight(Vec4 lamp) {
    ArrayList<Vertex> list = useVNormals ? vNormals : normals;
    for (Vertex n : list) n.calcLight(lamp, kd);
  }

  public void useVNormals(boolean state) {
    useVNormals = state;
    for (Face face : faces) {
      if (!state) face.linkNormalFace(normals);
      else face.linkNormalVertex(vNormals);
    }
  }

  private void computeVNormals() {
    for (int vi = 0; vi < vertices.size(); vi++) {
      Vec4 tmp = new Vec4(0, 0, 0);
      int count = 0;

      for (Face face : faces) {
        if (face.useVertex(vi)) {
          tmp.add(face.n1.world());
          count++;
        }
      }

      tmp.div(count);
      vNormals.add(new Vertex(tmp));
    }
  }

  public boolean isEmpty() {
    return vertices.isEmpty() || edges.isEmpty();
  }

  public void addVertex(Vec4 v) {
    synchronized (vertices) {
      vertices.add(new Vertex(v));
    }
  }

  public void addEdge(int v1, int v2) {
    synchronized (edges) {
      edges.add(new Edge(v1, v2));
    }
  }

  @SuppressWarnings("DefaultLocale")
  public String save(String path) throws IOException {
    String fileName = name.concat(FILE_MASK);
    path = new File(path, fileName).getAbsolutePath();
    ArrayList<String> data = new ArrayList<>(vertices.size());

    data.add(FILE_CREATOR);
    data.add(String.format(FILE_INFO, fileName));
    data.add(String.format("%s %s", L_OBJECT, name));

    for (Vertex vertex : vertices) {
      Vec4 v = vertex.world();
      data.add(String.format("%s %.6f %.6f %.6f",
        L_VERTEX, v.x(), v.y(), v.z()));
    }

    for (PointF point : texels) {
      data.add(String.format("%s %.6f %.6f",
        L_TEXEL, point.x, point.y));
    }

    for (Vertex vertex : normals) {
      Vec4 v = vertex.world();
      data.add(String.format("%s %.4f %.4f %.4f",
        L_NORMAL, v.x(), v.y(), v.z()));
    }

    for (Edge edge : edges) {
      data.add(String.format("%s %d %d",
        L_EDGE, edge.v1 + 1, edge.v2 + 1));
    }

    if (!faces.isEmpty()) {
      data.add(String.format("%s %s", L_SOLID, V_OFF));
      boolean useTexels = !texels.isEmpty();
      boolean useNormals = !normals.isEmpty();
      int[] v, n, t;

      for (Face face : faces) {
        v = face.vertices();
        n = face.normals();
        t = face.texels();

        String[] line = new String[3];
        for (int i = 0; i < 3; i++) {
          if (useTexels && useNormals)
            line[i] = String.format("%d/%d/%d", v[i], t[i], n[i]);
          else if (useTexels)
            line[i] = String.format("%d/%d", v[i], t[i]);
          else if (useNormals)
            line[i] = String.format("%d//%d", v[i], n[i]);
          else
            line[i] = Integer.toString(v[i]);
        }

        data.add(String.format("%s %s %s %s",
          L_FACE, line[0], line[1], line[2]));
      }
    }

    Utils.writeFile(path, data);
    return path;
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
        parts = line.split(SEP_PART);
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
          case L_EDGE:
            addEdge(edges, parts[1], parts[2]);
          case L_FACE:
            count = parts.length - 1;
            if (useTextures || useNormals) {
              for (int i = 1; i <= count; i++) {
                part = parts[i].split(SEP_VERTEX);
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
      if (useNormals) face.linkNormalFace(normals);
      if (useTextures) face.linkTexture(texels);
    }

    model.configure();
    if (useNormals) model.computeVNormals();
    model.type = useNormals ? Type.FACE : Type.EDGE;
    MyLog.d(LOG_TAG, "Data loaded (name: " + model.name
      + ", vertices: " + vertices.size() + ", normals: " + normals.size()
      + ", texels: " + texels.size() + ", edges: " + edges.size()
      + ", faces: " + faces.size() + ")");
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
