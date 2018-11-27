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

  private static LangStruct instance = new LangStruct();
  public static LangStruct getInstance() { return instance; }
  private LangStruct() {}

  private Map<String, Integer> symbolMap;
  private SparseArray<String> symbolMapReverse;

  public Map<String, Integer> getSymbolMap() {
    return symbolMap;
  }

  String getMnemonic(int id) {
    return symbolMapReverse.get(id);
  }

  boolean initCompleted() {
    return symbolMap != null && symbolMapReverse != null;
  }

  void initStatic(String fileName) throws IOException {
    MyLog.d(LOG_TAG, "Static init started");
    ArrayList<String> lines = Utils.readFile(fileName);
    symbolMapReverse = new SparseArray<>(lines.size());
    symbolMap = new HashMap<>(lines.size());

    for (String line : lines) {
      if (line.isEmpty()) continue;
      String[] subLines = line.split("\\s+");
      int id = Integer.parseInt(subLines[1], 16);
      String name = subLines[0];
      symbolMap.put(name, id);
      symbolMapReverse.append(id, name);
    }

    MyLog.d(LOG_TAG, "Static init completed");
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
