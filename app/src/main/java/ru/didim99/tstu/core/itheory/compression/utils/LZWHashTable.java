package ru.didim99.tstu.core.itheory.compression.utils;

import android.util.SparseArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by didim99 on 09.03.20.
 */
public class LZWHashTable {
  public static final int BUFFER_SIZE = 64;
  public static final int CODE_BITS = 16;
  private static final int TABLE_SIZE_MIN = 256;
  private static final int TABLE_SIZE_MAX = (1 << CODE_BITS);

  private boolean reversed;
  private HashMap<List<Integer>, Integer> codeTable;
  private SparseArray<ArrayList<Integer>> sequenceTable;
  private ArrayList<Integer> buffer;
  private int buildCount;
  private int nextCode;

  public LZWHashTable(boolean reversed) {
    this.codeTable = new HashMap<>(TABLE_SIZE_MIN);
    this.sequenceTable = new SparseArray<>(TABLE_SIZE_MIN);
    this.buffer = new ArrayList<>(BUFFER_SIZE);
    this.reversed = reversed;
    this.buildCount = 0;
    this.buffer.add(0);
    rebuild();
  }

  public void rebuild() {
    codeTable.clear();
    sequenceTable.clear();
    nextCode = TABLE_SIZE_MIN;
    buildCount++;
  }

  public int getCodeBits() {
    int maxCode = getMaxCode(), codeBits = 1;
    while ((maxCode >>>= 1) > 0) codeBits++;
    return codeBits;
  }

  public boolean isFull() {
    return nextCode == TABLE_SIZE_MAX;
  }

  public boolean contains(ArrayList<Integer> sequence) {
    return getCode(sequence) != null;
  }

  public boolean contains(int code) {
    return getSequence(code) != null;
  }

  public Integer getCode(List<Integer> sequence) {
    if (sequence.size() == 1) return sequence.get(0);
    return codeTable.get(sequence);
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
    int oldLength = sequence.size() - 1;
    int code = getCode(sequence.subList(0, oldLength));
    if (!isFull())
      codeTable.put(new ArrayList<>(sequence), nextCode++);
    return code;
  }

  public void add(int code, int symbol) {
    if (isFull()) return;
    ArrayList<Integer> sequence = new ArrayList<>(getSequence(code));
    sequence.add(symbol);
    sequenceTable.append(nextCode++, sequence);
  }

  public String describe() {
    return "Sequence code table\n" +
      String.format(Locale.US, "Current size: %d\n",
        TABLE_SIZE_MIN + (reversed ? sequenceTable.size() : codeTable.size())) +
      String.format(Locale.US, "Build count: %d\n", buildCount) +
      String.format(Locale.US, "Code bits: %d\n", getCodeBits());
  }

  private int getMaxCode() {
    return (buildCount > 1 ? TABLE_SIZE_MAX : nextCode) - 1;
  }

  @Override
  public String toString() {
    return describe();
  }
}
