package ru.didim99.tstu.core.graphics.model;

/**
 * Created by didim99 on 09.03.19.
 */
public class Edge {
  int v1, v2;

  Edge(int v1, int v2) {
    this.v1 = v1;
    this.v2 = v2;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Edge)) return false;
    Edge e = (Edge) obj;
    return e.v1 == v1 && e.v2 == v2
      || e.v1 == v2 && e.v2 == v1;
  }

  @Override
  public String toString() {
    return "[" + v1 + " " + v2 + "]";
  }
}
