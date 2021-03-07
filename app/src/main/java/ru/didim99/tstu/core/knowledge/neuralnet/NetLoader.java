package ru.didim99.tstu.core.knowledge.neuralnet;

import android.content.Context;
import com.google.gson.Gson;
import java.io.IOException;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 09.03.19.
 */
public class NetLoader extends CallbackTask<String, Net> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_NetLoader";

  public NetLoader(Context context) {
    super(context);
  }

  @Override
  protected Net doInBackgroundInternal(String path) {
    try {
      Gson gson = new Gson();
      String data = Utils.joinStr("\n", Utils.readFile(path));
      NetConfig config = gson.fromJson(data, NetConfig.class);
      if (config == null || config.isEmpty())
        throw new IOException("Incorrect config format");
      return new Net(config);
    } catch (IOException e) {
      MyLog.e(LOG_TAG, "Unable to load item: " + e);
    }

    return null;
  }
}
