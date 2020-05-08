package ru.didim99.tstu.core.modeling.randproc;

import java.util.Arrays;

/**
 * Created by didim99 on 08.05.20.
 */
public class CyclicBuffer {
  private double[] buffer;
  private int size, pointer;

  public CyclicBuffer(int size) {
    this.buffer = new double[size];
    Arrays.fill(buffer, 0.0);
    this.size = buffer.length;
    this.pointer = 0;
  }

  public void push(double val) {
    checkPointer();
    buffer[pointer++] = val;
  }

  public void fill(Supplier source) {
    checkPointer();
    while (pointer < size)
      push(source.nextDouble());
  }

  public double get(int pos) {
    pos = (pointer + pos) % size;
    return buffer[pos];
  }

  public double[] getAll() {
    return Arrays.copyOf(buffer, size);
  }

  private void checkPointer() {
    if (pointer == size) pointer = 0;
  }

  @FunctionalInterface
  public interface Supplier {
    double nextDouble();
  }
}
