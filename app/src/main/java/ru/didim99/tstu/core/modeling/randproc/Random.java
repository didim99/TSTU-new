package ru.didim99.tstu.core.modeling.randproc;

/**
 * Created by didim99 on 08.05.20.
 */
public class Random {
  private final long a, m, seed;
  private long prev;

  public Random(long a, long m, long seed) {
    this.seed = seed;
    this.prev = seed;
    this.a = a;
    this.m = m;
  }

  public int nextInt() {
    int num = (int) prev;
    prev = (prev * a) % m;
    return num;
  }

  public double nextDouble() {
    return (double) nextInt() / m;
  }

  public double nextDoubleCentered() {
    return nextDouble() - 0.5;
  }

  public void reset() {
    prev = seed;
  }
}
