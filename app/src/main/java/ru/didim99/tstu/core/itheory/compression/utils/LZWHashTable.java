package ru.didim99.tstu.core.itheory.compression.utils;

import android.util.SparseArray;
import android.util.SparseIntArray;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by didim99 on 09.03.20.
 */
public class LZWHashTable {
  public static final int BUFFER_SIZE = 64;
  private static final int CODE_BITS = 16;
  private static final int INVALID_CODE = -1;
  private static final int TABLE_SIZE_MIN = 256;
  private static final int TABLE_SIZE_MAX = (1 << CODE_BITS);

  private boolean reversed;
  private SparseIntArray codeTable;
  private SparseArray<ArrayList<Integer>> sequenceTable;
  private ArrayList<Integer> buffer;
  private int buildCount;
  private int nextCode;

  public LZWHashTable(boolean reversed) {
    this.codeTable = new SparseIntArray(TABLE_SIZE_MIN);
    this.sequenceTable = new SparseArray<>(TABLE_SIZE_MIN);
    this.buffer = new ArrayList<>(BUFFER_SIZE);
    this.reversed = reversed;
    this.buildCount = 0;
    this.buffer.add(0);
    rebuild();
  }

  public void rebuild() {
    if (!reversed) {
      codeTable.clear();
      for (int v = 0; v < TABLE_SIZE_MIN; v++)
        codeTable.put(hashOf(v), v);
    } else
      sequenceTable.clear();
    nextCode = TABLE_SIZE_MIN;
    buildCount++;
  }

  public boolean isFull() {
    return nextCode == TABLE_SIZE_MAX;
  }

  public boolean contains(ArrayList<Integer> sequence) {
    return getCode(sequence) != INVALID_CODE;
  }

  public boolean contains(int code) {
    return getSequence(code) != null;
  }

  public int getCode(ArrayList<Integer> sequence) {
    return codeTable.get(hashOf(sequence), INVALID_CODE);
  }

  private int getCode(ArrayList<Integer> sequence, int length) {
    return codeTable.get(hashOf(sequence, length), INVALID_CODE);
  }

  public ArrayList<Integer> getSequence(int code) {
    buffer.clear();
    if (code < TABLE_SIZE_MIN) {
      buffer.add(code);
      return buffer;
    } else {
      ArrayList<Integer> sequence = sequenceTable.get(code);
      if (sequence == null) return null;
      buffer.addAll(sequence);
      return buffer;
    }
  }

  public int add(ArrayList<Integer> sequence) {
    int code = getCode(sequence, sequence.size() - 1);
    codeTable.append(hashOf(sequence), nextCode++);
    return code;
  }

  public void add(int code, int symbol) {
    ArrayList<Integer> sequence = new ArrayList<>(getSequence(code));
    sequence.add(symbol);
    sequenceTable.append(nextCode++, sequence);
  }

  public String describe() {
    return "Sequence code table\n" +
      String.format(Locale.US, "Current size: %d\n",
        !reversed ? codeTable.size() : sequenceTable.size() + TABLE_SIZE_MIN) +
      String.format(Locale.US, "Build count: %d\n", buildCount);
  }

  @Override
  public String toString() {
    return describe();
  }

  private static int hashOf(int symbol) {
    return 31 + symbol;
  }

  private static int hashOf(ArrayList<Integer> sequence) {
    return hashOf(sequence, sequence.size());
  }

  private static int hashOf(ArrayList<Integer> sequence, int length) {
    if (length == 1) return hashOf(sequence.get(0));
    int hashCode = 1;
    for (int i = 0; i < length; i++)
      hashCode = 31 * hashCode + sequence.get(i);
    return hashCode;
  }
}
