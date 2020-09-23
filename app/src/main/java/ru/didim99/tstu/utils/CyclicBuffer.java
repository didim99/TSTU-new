package ru.didim99.tstu.utils;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created by didim99 on 23.09.20.
 */

public class CyclicBuffer<T> {
  private T[] buffer;
  private int size, pointer;

  @SuppressWarnings("unchecked")
  public CyclicBuffer(Class<T> type, int size) {
    this.buffer = (T[]) Array.newInstance(type, size);
    Arrays.fill(buffer, 0.0);
    this.size = buffer.length;
    this.pointer = 0;
  }

  public void push(T val) {
    checkPointer();
    buffer[pointer++] = val;
  }

  public void fill(Supplier<T> source) {
    checkPointer();
    while (pointer < size)
      push(source.nextElement());
  }

  public T get(int pos) {
    pos = (pointer + pos) % size;
    return buffer[pos];
  }

  public T[] getAll() {
    return Arrays.copyOf(buffer, size);
  }

  private void checkPointer() {
    if (pointer == size) pointer = 0;
  }

  @FunctionalInterface
  public interface Supplier<T> {
    T nextElement();
  }
}
