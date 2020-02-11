package ru.didim99.tstu.core.modeling;

import android.content.Context;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 15.09.18.
 */
public class ModelingTask extends CallbackTask<Config, Result> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ModelingTask";

  public ModelingTask(Context context) {
    super(context);
  }

  @Override
  protected Result doInBackgroundInternal(Config config) {
    Result result = new Result();
    switch (config.getTaskType()) {
      case Config.TaskType.STATIC_CURVE:
        StaticProcessor sp = new StaticProcessor(config.getVariable());
        sp.precess();
        result.setSeries(sp.getSeries());
        result.setDescription(sp.getDescription());
        return result;
      default:
        return null;
    }
  }
}
