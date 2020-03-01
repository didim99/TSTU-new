package ru.didim99.tstu.core.itheory.compression.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by didim99 on 01.03.20.
 */
public class BaseCharTable<E extends BaseCharTable.Entry> {
  private static final String TABLE_HEADER = "Char  Freq ";
  private static Map<String, String> escapeMap;

  static {
    escapeMap = new HashMap<>();
    escapeMap.put("\n", "\\n");
    escapeMap.put("\t", "\\t");
    escapeMap.put(" ", "\\s");
  }

  Map<Character, E> table;
  List<E> entryList;

  BaseCharTable() {
    table = new HashMap<>();
  }

  String getHeader() {
    return TABLE_HEADER;
  }

  public String describe() {
    StringBuilder sb = new StringBuilder();
    sb.append(getHeader()).append('\n');
    for (Entry entry : entryList)
      sb.append(entry.toString()).append('\n');
    return sb.toString();
  }

  public void serializeTo(DataOutput target) throws IOException {
    target.writeInt(entryList.size());
    for (Entry entry : entryList) {
      target.writeChar(entry.character);
      target.writeInt(entry.frequency);
    }
  }

  static Map<Character, Integer> readFreqMap(DataInput source)
    throws IOException {
    int size = source.readInt();
    Map<Character, Integer> freqMap = new HashMap<>(size);
    while (size-- > 0) freqMap.put(
      source.readChar(), source.readInt());
    return freqMap;
  }

  static class Entry {
    char character;
    int frequency;

    Entry(char character, int frequency) {
      this.character = character;
      this.frequency = frequency;
    }

    @Override
    public String toString() {
      String str = String.valueOf(character);
      if (escapeMap.containsKey(str)) str = escapeMap.get(str);
      return String.format(Locale.US, "%-4s  %-5d", str, frequency);
    }
  }
}
