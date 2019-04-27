package ru.didim99.tstu.core.os.scheduler;

import android.content.Context;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 23.04.19.
 */
class ProcessListLoader extends CallbackTask<String, ArrayList<Process.Info>> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_PLLoader";

  public ProcessListLoader(Context context) {
    super(context);
  }

  @Override
  protected ArrayList<Process.Info> doInBackgroundInternal(String path) {
    MyLog.d(LOG_TAG, "Loading process list...");
    ArrayList<Process.Info> result = new ArrayList<>();
    Gson parser = new Gson();

    try {
      ArrayList<String> data = Utils.readFile(path);
      for (String lise : data)
        result.add(parser.fromJson(lise, Process.Info.class));
      return result;
    } catch (IOException e) {
      MyLog.e(LOG_TAG, "Can't read file: " + e);
      return null;
    }
  }
}
