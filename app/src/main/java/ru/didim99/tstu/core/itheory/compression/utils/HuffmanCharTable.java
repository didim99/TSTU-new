package ru.didim99.tstu.core.itheory.compression.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by didim99 on 29.02.20.
 */
public class HuffmanCharTable {
  private static Map<String, String> escapeMap;

  static {
    escapeMap = new HashMap<>();
    escapeMap.put("\n", "\\n");
    escapeMap.put("\t", "\\t");
    escapeMap.put(" ", "\\s");
  }

  private HuffmanTreeEntry root;
  private Map<Character, Entry> table;
  private List<Entry> entryList;

  public HuffmanCharTable(Map<Character, Integer> frequencyMap) {
    this.table = new HashMap<>();
    HuffmanTree tree = new HuffmanTree(frequencyMap);
    for (HuffmanTreeEntry entry : tree.getLeafs())
      table.put(entry.getCharacter(), new Entry(tree, entry));
    this.entryList = getEntryList();
    this.root = tree.getRoot();
  }

  public HuffmanTreeEntry getRoot() {
    return root;
  }

  public ArrayList<Boolean> getCode(char c) {
    return table.get(c).code;
  }

  public String getCodeStr(char c) {
    return table.get(c).codeStr;
  }

  public String describe() {
    StringBuilder sb = new StringBuilder();
    sb.append("Char  Freq   Code\n");
    for (Entry entry : entryList) {
      String str = String.valueOf(entry.character);
      if (escapeMap.containsKey(str)) str = escapeMap.get(str);
      sb.append(String.format(Locale.US, "%-4s  %-5d  %s\n",
        str, entry.frequency, entry.codeStr));
    }

    return sb.toString();
  }

  private List<Entry> getEntryList() {
    List<Entry> entries = new ArrayList<>(table.values());
    Collections.sort(entries, Entry::naturalCompare);
    return entries;
  }

  public void serializeTo(DataOutput target) throws IOException {
    target.writeInt(table.size());
    for (Entry entry : entryList) {
      target.writeChar(entry.character);
      target.writeInt(entry.frequency);
    }
  }

  public static HuffmanCharTable readFrom(DataInput source)
    throws IOException {
    int size = source.readInt();
    Map<Character, Integer> freqMap = new HashMap<>(size);
    while (size-- > 0) freqMap.put(
      source.readChar(), source.readInt());
    return new HuffmanCharTable(freqMap);
  }

  private static class Entry {
    private ArrayList<Boolean> code;
    private String codeStr;
    private char character;
    private int frequency;

    private Entry(HuffmanTree tree, HuffmanTreeEntry entry) {
      this.character = entry.getCharacter();
      this.code = tree.getCharCode(character);
      this.codeStr = getCodeStr(code);
      this.frequency = entry.getWeight();
    }

    private String getCodeStr(ArrayList<Boolean> code) {
      StringBuilder sb = new StringBuilder();
      for (Boolean bit : code)
        sb.append(bit ? "1" : "0");
      return sb.toString();
    }

    private static int naturalCompare(Entry e1, Entry e2) {
      int c = Integer.compare(e1.code.size(), e2.code.size());
      if (c == 0) c = e1.codeStr.compareTo(e2.codeStr);
      if (c == 0) c = Character.compare(e1.character, e2.character);
      return c;
    }
  }
}
