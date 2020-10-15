package ru.didim99.tstu.core.math.analysis;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 20.11.18.
 */
class FunctionTable {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Func";
  private static final String ENTRY_REGEX = "\\s+\\|\\s+";
  private static final String VALUE_REGEX = "\\s+";

  private Map<Double, Double> table;
  private Map<Double, Double> tableReverse;

  FunctionTable(Context context, int resID, String name)
    throws IOException {
    try {
      MyLog.d(LOG_TAG, "Loading data (" + name + ")...");
      table = new HashMap<>();
      tableReverse = new HashMap<>();
      BufferedReader reader = new BufferedReader(new InputStreamReader(
        context.getResources().openRawResource(resID)));

      String line;
      double x, f;
      while ((line = reader.readLine()) != null) {
        if (line.isEmpty()) continue;
        String[] entries = line.split(ENTRY_REGEX);
        for (String e : entries) {
          String[] v = e.trim().split(VALUE_REGEX);
          x = Double.parseDouble(v[0]);
          f = Double.parseDouble(v[1]);
          tableReverse.put(f, x);
          table.put(x, f);
        }
      }

      reader.close();
      MyLog.d(LOG_TAG, "Data loaded");
    } catch (IOException e) {
      throw new IOException("Can't init table, resources damaged", e);
    }
  }

  double getF(double x) {
    Double res = table.get(x);

    if (res == null) {
      double minDelta = Double.MAX_VALUE;
      for (Map.Entry<Double, Double> e : table.entrySet()) {
        if (Math.abs(e.getKey() - x) < minDelta) {
          minDelta = Math.abs(e.getKey() - x);
          res = e.getValue();
        }
      }
    }

    return res;
  }

  double getX(double f) {
    Double res = tableReverse.get(f);

    if (res == null) {
      double minDelta = Double.MAX_VALUE;
      for (Map.Entry<Double, Double> e : table.entrySet()) {
        if (Math.abs(e.getValue() - f) < minDelta) {
          minDelta = Math.abs(e.getValue() - f);
          res = e.getKey();
        }
      }
    }

    return res;
  }
}
