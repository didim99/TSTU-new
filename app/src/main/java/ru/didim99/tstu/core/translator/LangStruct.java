package ru.didim99.tstu.core.translator;

import android.util.SparseArray;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 11.09.18.
 */
public class LangStruct {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_LangStruct";
  private static final String DIVIDER = "\\s+";

  private static LangStruct instance = new LangStruct();
  public static LangStruct getInstance() { return instance; }
  private LangStruct() {}

  private Map<String, Integer> lexemeMap;
  private SparseArray<String> lexemeMapReverse;
  private SparseArray<String> fortranLexemeMap;

  public Map<String, Integer> getLexemeMap() {
    return lexemeMap;
  }

  public String getMnemonic(int id) {
    return lexemeMapReverse.get(id);
  }

  String getOutMnemonic(int id) {
    return fortranLexemeMap.get(id);
  }

  boolean initCompleted() {
    return lexemeMap != null && lexemeMapReverse != null;
  }

  void initStatic(String ilFile, String olFile) throws IOException {
    MyLog.d(LOG_TAG, "Static init started");
    fortranLexemeMap = new SparseArray<>();
    lexemeMapReverse = new SparseArray<>();
    lexemeMap = new HashMap<>();
    loadLang(ilFile, lexemeMap, lexemeMapReverse);
    loadLang(olFile, null, fortranLexemeMap);
    MyLog.d(LOG_TAG, "Static init completed");
  }

  private void loadLang(String file, Map<String, Integer> sMap,
                        SparseArray<String> rsMap) throws IOException {
    ArrayList<String> lines = Utils.readFile(file);
    for (String line : lines) {
      if (line.isEmpty()) continue;
      String[] subLines = line.split(DIVIDER);
      int id = Integer.parseInt(subLines[1], 16);
      String name = subLines[0];
      if (sMap != null) sMap.put(name, id);
      if (rsMap != null) rsMap.append(id, name);
    }
  }

  static final class DictEntry {
    private String mnemonic;
    private int value;

    DictEntry(String mnemonic, int value) {
      this.mnemonic = mnemonic;
      this.value = value;
    }

    DictEntry(DictEntry other) {
      this(other.mnemonic, other.value);
    }

    String getMnemonic() { return mnemonic; }
    int getValue() { return value; }
  }
}
