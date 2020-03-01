package ru.didim99.tstu.core.itheory.compression.utils;

import android.support.annotation.NonNull;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by didim99 on 29.02.20.
 */
public class HuffmanCharTable extends BaseCharTable<HuffmanCharTable.Entry> {
  private static final String TABLE_HEADER = "  Code";

  private HuffmanTreeEntry root;

  public HuffmanCharTable(Map<Character, Integer> frequencyMap) {
    super();
    HuffmanTree tree = new HuffmanTree(frequencyMap);
    for (HuffmanTreeEntry entry : tree.getLeafs())
      table.put(entry.getCharacter(), new Entry(tree, entry));
    this.entryList = getEntryList();
    this.root = tree.getRoot();
  }

  public HuffmanCharTable(DataInput source) throws IOException {
    this(readFreqMap(source));
  }

  @Override
  String getHeader() {
    return super.getHeader() + TABLE_HEADER;
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

  private List<Entry> getEntryList() {
    List<Entry> entries = new ArrayList<>(table.values());
    Collections.sort(entries);
    return entries;
  }

  static class Entry extends BaseCharTable.Entry
    implements Comparable<Entry> {
    private ArrayList<Boolean> code;
    private String codeStr;

    private Entry(HuffmanTree tree, HuffmanTreeEntry entry) {
      super(entry.getCharacter(), entry.getWeight());
      this.code = tree.getCharCode(character);
      this.codeStr = getCodeStr(code);
    }

    private String getCodeStr(ArrayList<Boolean> code) {
      StringBuilder sb = new StringBuilder();
      for (Boolean bit : code)
        sb.append(bit ? "1" : "0");
      return sb.toString();
    }

    @Override
    public int compareTo(@NonNull Entry o) {
      int c = Integer.compare(code.size(), o.code.size());
      if (c == 0) c = codeStr.compareTo(o.codeStr);
      if (c == 0) c = Character.compare(character, o.character);
      return c;
    }

    @Override
    public String toString() {
      return String.format(Locale.US, "%s  %s",
        super.toString(), codeStr);
    }
  }
}
