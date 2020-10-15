package ru.didim99.tstu.core.math.analysis;

import android.content.Context;
import java.io.IOException;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 21.11.18.
 */
class Storage {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Storage"
    ;
  private static Storage instance = new Storage();
  static Storage getInstance() { return instance; }
  private Storage() {}

  private FunctionTable laplas;
  private FunctionTableR2 student;
  private FunctionTableR2 chiSq;

  boolean initCompleted() {
    return laplas != null && student != null && chiSq != null;
  }

  void init(Context ctx) throws IOException {
    try {
      laplas = new FunctionTable(ctx, R.raw.laplas, "Laplas");
      student = new FunctionTableR2(ctx, R.raw.student, "Student");
      chiSq = new FunctionTableR2(ctx, R.raw.chisq, "ChiSq");
    } catch (IOException e) {
      MyLog.e(LOG_TAG, "Can't load data tables: " + e);
      throw e;
    }
  }

  FunctionTable getLaplas() { return laplas; }
  FunctionTableR2 getStudent() { return student; }
  FunctionTableR2 getChiSq() { return chiSq; }
}
