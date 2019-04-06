package ru.didim99.tstu.core.graphics.utils;

/**
 * Created by didim99 on 05.04.19.
 */
public class Face {
  public int v1, v2, v3;
  public int t1, t2, t3;

  Face(int[] v, int[] vt) {
    this.v1 = v[0];
    this.v2 = v[1];
    this.v3 = v[2];
    this.t1 = vt[0];
    this.t2 = vt[1];
    this.t3 = vt[2];
  }
}
