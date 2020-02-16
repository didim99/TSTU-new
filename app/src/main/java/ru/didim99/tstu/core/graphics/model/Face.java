package ru.didim99.tstu.core.graphics.model;

import android.graphics.PointF;
import java.util.ArrayList;

/**
 * Created by didim99 on 05.04.19.
 */
public class Face {
  private int iv1, iv2, iv3;
  private int in1, in2, in3;
  private int it1, it2, it3;
  public Vertex v1, v2, v3;
  public Vertex n1, n2, n3;
  public PointF t1, t2, t3;

  Face(int[] v, int[] n, int[] t) {
    this.iv1 = v[0];
    this.iv2 = v[1];
    this.iv3 = v[2];
    this.in1 = n[0];
    this.in2 = n[1];
    this.in3 = n[2];
    this.it1 = t[0];
    this.it2 = t[1];
    this.it3 = t[2];
  }

  void linkVertex(ArrayList<Vertex> vertices) {
    v1 = vertices.get(iv1);
    v2 = vertices.get(iv2);
    v3 = vertices.get(iv3);
  }

  void linkNormalVertex(ArrayList<Vertex> normals) {
    n1 = normals.get(iv1);
    n2 = normals.get(iv2);
    n3 = normals.get(iv3);
  }

  void linkNormalFace(ArrayList<Vertex> normals) {
    n1 = normals.get(in1);
    n2 = normals.get(in2);
    n3 = normals.get(in3);
  }

  void linkTexture(ArrayList<PointF> texels) {
    t1 = texels.get(it1);
    t2 = texels.get(it2);
    t3 = texels.get(it3);
  }

  int[] vertices() {
    return new int[] {iv1 + 1, iv2 + 1, iv3 + 1};
  }

  int[] normals() {
    return new int[] {in1 + 1, in2 + 1, in3 + 1};
  }

  int[] texels() {
    return new int[] {it1 + 1, it2 + 1, it3 + 1};
  }

  boolean useVertex(int v) {
    return iv1 == v || iv2 == v || iv3 == v;
  }
}
