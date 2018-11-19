package ru.didim99.tstu.core.translator.utils;

import android.util.SparseArray;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import ru.didim99.tstu.core.translator.LangStruct;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 01.11.18.
 */
public class PrecedenceTable {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_PT";
  private static final String DELIMITER = "\\s+";

  private static PrecedenceTable instance = new PrecedenceTable();
  public static PrecedenceTable getInstance() { return instance; }
  private PrecedenceTable() {}

  public static final class RT {
    public static final byte NONE   = 0;
    public static final byte LOWER  = 1;
    public static final byte EQUALS = 2;
    public static final byte HIGHER = 3;
  }

  private Map<String, Byte> valueMap;
  private Map<String, Integer> symbolMap;
  private SparseArray<SparseArray<Byte>> table;

  public byte get(int first, int second) {
    Byte rel = table.get(first).get(second);
    return rel == null ? RT.NONE : rel;
  }

  public boolean isInitCompleted() {
    return table != null;
  }

  public void initStatic(String fileName) throws IOException {
    MyLog.d(LOG_TAG, "Static init started");
    ArrayList<String> src = Utils.readFile(fileName);
    initMaps();

    String firstLine;
    String[] subLines, headers;

    while ((firstLine = src.get(0)).isEmpty())
      src.remove(0);

    subLines = headers = firstLine.trim().split(DELIMITER);
    table = new SparseArray<>(subLines.length);
    for (String key : subLines)
      table.append(symbolMap.get(key), new SparseArray<>());
    src.remove(0);

    for (String line : src) {
      if (line.isEmpty()) continue;
      subLines = line.trim().split(DELIMITER);
      SparseArray<Byte> row = table.get(symbolMap.get(subLines[0]));
      for (int i = 1; i < subLines.length; i++) {
        row.append(symbolMap.get(headers[i - 1]), valueMap.get(subLines[i]));
      }
    }

    MyLog.d(LOG_TAG, "Static init completed");
    MyLog.v(LOG_TAG, this.toString());
  }

  private void initMaps() {
    symbolMap = new HashMap<>();
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.PROGRAM, LangStruct.KEYWORD.PROGRAM);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.VAR, LangStruct.KEYWORD.VAR);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.INTEGER, LangStruct.KEYWORD.INTEGER);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.REAL, LangStruct.KEYWORD.REAL);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.BEGIN, LangStruct.KEYWORD.BEGIN);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.END, LangStruct.KEYWORD.END);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.FOR, LangStruct.KEYWORD.FOR);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.DOWNTO, LangStruct.KEYWORD.DOWNTO);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.DO, LangStruct.KEYWORD.DO);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.TO, LangStruct.KEYWORD.TO);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.READ, LangStruct.KEYWORD.READ);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.WRITELN, LangStruct.KEYWORD.WRITELN);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.WRITE, LangStruct.KEYWORD.WRITE);
    symbolMap.put(LangStruct.MNEMONIC.OPERATOR.ASSIGN, LangStruct.OPERATOR.ASSIGN);
    symbolMap.put(LangStruct.MNEMONIC.OPERATOR.PLUS, LangStruct.OPERATOR.PLUS);
    symbolMap.put(LangStruct.MNEMONIC.OPERATOR.MINUS, LangStruct.OPERATOR.MINUS);
    symbolMap.put(LangStruct.MNEMONIC.OPERATOR.PRODUCT, LangStruct.OPERATOR.PRODUCT);
    symbolMap.put(LangStruct.MNEMONIC.OPERATOR.DIV, LangStruct.OPERATOR.DIV);
    symbolMap.put(LangStruct.MNEMONIC.DIVIDER.END_OP, LangStruct.DIVIDER.END_OP);
    symbolMap.put(LangStruct.MNEMONIC.DIVIDER.SEP_VL, LangStruct.DIVIDER.SEP_VL);
    symbolMap.put(LangStruct.MNEMONIC.DIVIDER.END_VL, LangStruct.DIVIDER.END_VL);
    symbolMap.put(LangStruct.MNEMONIC.DIVIDER.BEG_CALL, LangStruct.DIVIDER.BEG_CALL);
    symbolMap.put(LangStruct.MNEMONIC.DIVIDER.END_CALL, LangStruct.DIVIDER.END_CALL);
    symbolMap.put(LangStruct.MNEMONIC.DIVIDER.END_PROG, LangStruct.DIVIDER.END_PROG);
    symbolMap.put(LangStruct.MNEMONIC.CUSTOM.ID, LangStruct.CUSTOM.ID);
    symbolMap.put(LangStruct.MNEMONIC.CUSTOM.LITERAL, LangStruct.CUSTOM.LITERAL);

    valueMap = new HashMap<>();
    valueMap.put("-", RT.NONE);
    valueMap.put("<", RT.LOWER);
    valueMap.put("=", RT.EQUALS);
    valueMap.put(">", RT.HIGHER);
  }

  @Override
  public String toString() {
    if (table == null) return "null";

    StringBuilder sb = new StringBuilder();
    sb.append("Table: ").append(table.size()).append("x")
      .append(table.valueAt(0).size()).append("\n");
    for (int i = 0; i < table.size(); i++) {
      SparseArray<Byte> row = table.valueAt(i);
      sb.append(String.format(Locale.US, "%5X", table.keyAt(i)));
      for (int j = 0; j < row.size(); j++)
        sb.append(String.format(Locale.US, "%3d", row.valueAt(j)));
      sb.append("\n");
    }

    return sb.toString();
  }
}
