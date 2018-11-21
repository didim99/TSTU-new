package ru.didim99.tstu.core.math;

import android.content.Context;
import android.util.SparseArray;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 20.11.18.
 */
class FunctionTableR2 {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_FuncR2";
  private static final String ENTRY_REGEX = "\\s+\\|\\s+";
  private static final String VALUE_REGEX = "\\s+";

  private ArrayList<Double> gammaSet;
  private Map<Double, SparseArray<Double>> table;

  FunctionTableR2(Context context, int resID, String name)
    throws IOException {
    try {
      MyLog.d(LOG_TAG, "Loading data (" + name + ")...");
      table = new HashMap<>();
      gammaSet = new ArrayList<>();
      BufferedReader reader = new BufferedReader(new InputStreamReader(
        context.getResources().openRawResource(resID)));

      String l;
      ArrayList<String> lines = new ArrayList<>();
      while ((l = reader.readLine()) != null) {
        if (l.isEmpty()) continue;
        lines.add(l);
      }

      int count = lines.size();
      for (String line : lines) {
        String[] entries = line.split(ENTRY_REGEX);
        String[] values = entries[1].trim().split(VALUE_REGEX);
        int n = Integer.parseInt(entries[0].trim());

        if (n == 0) {
          for (String v : values)
            gammaSet.add(Double.parseDouble(v));
          for (Double k : gammaSet)
            table.put(k, new SparseArray<>(count));
        } else {
          for (int i = 0; i < values.length; i++) {
            table.get(gammaSet.get(i)).put(n,
              Double.parseDouble(values[i]));
          }
        }
      }

      reader.close();
      MyLog.d(LOG_TAG, "Data loaded");
    } catch (IOException e) {
      throw new IOException("Can't init table, resources damaged", e);
    }
  }

  public double get(double gamma, int n) {
    return table.get(findGamma(gamma)).get(n);
  }

  private double findGamma(double gamma) {
    double minDelta = 1, result = gamma;
    for (Double d : gammaSet) {
      if (Math.abs(gamma - d) < minDelta) {
        minDelta = Math.abs(gamma - d);
        result = d;
      }
    }

    return result;
  }
}
