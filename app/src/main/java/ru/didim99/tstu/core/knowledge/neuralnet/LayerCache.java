package ru.didim99.tstu.core.knowledge.neuralnet;

import ru.didim99.tstu.core.math.common.PointD;

/**
 * Created by didim99 on 22.02.21.
 */

class LayerCache {
  private final int size;
  public PointD out, dOut, delta;

  public LayerCache(int size) {
    this.out = new PointD(size);
    this.dOut = new PointD(size);
    this.delta = new PointD(size);
    this.size = size;
  }

  public int size() {
    return size;
  }
}
