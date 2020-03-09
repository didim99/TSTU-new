package ru.didim99.tstu.core.itheory.compression.utils;

import java.util.ArrayList;

/**
 * Created by didim99 on 09.03.20.
 */
public class LZWHashTable {
  public static final int BUFFER_SIZE = 64;
  private static final int CODE_BITS = 16;

  private ArrayList<Integer> buffer;

  public LZWHashTable() {
    buffer = new ArrayList<>(BUFFER_SIZE);
    rebuild();
  }

  public void rebuild() {

  }

  public boolean isFull() {
    return false;
  }

  public boolean contains(ArrayList<Integer> sequence) {
    return false;
  }

  public boolean contains(int code) {
    return false;
  }

  public int getCode(ArrayList<Integer> sequence) {
    return getCode(sequence, sequence.size());
  }

  private int getCode(ArrayList<Integer> sequence, int length) {
    return 0;
  }

  public ArrayList<Integer> getSequence(int code) {
    return buffer;
  }

  public int add(ArrayList<Integer> sequence) {
    return getCode(sequence, sequence.size() - 1);
  }

  public void add(int code, int symbol) {

  }

  public String describe() {
    StringBuilder sb = new StringBuilder();
    sb.append("Sequence code table\n");
    return sb.toString();
  }

  @Override
  public String toString() {
    return describe();
  }
}
