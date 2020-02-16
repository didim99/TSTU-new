package ru.didim99.tstu.core.graphics.task;

import android.content.Context;
import java.io.IOException;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.graphics.model.Model;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 09.03.19.
 */
public class ModelLoader extends CallbackTask<String, Model> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ModelLoader";

  public ModelLoader(Context context) {
    super(context);
  }

  @Override
  protected Model doInBackgroundInternal(String path) {
    try {
      return Model.load(path);
    } catch (IOException | Model.ParserException e) {
      MyLog.e(LOG_TAG, "Unable to load item: " + e);
    }

    return null;
  }
}
