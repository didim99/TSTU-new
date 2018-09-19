package ru.didim99.tstu.core.numeric;

import android.content.Context;
import ru.didim99.tstu.core.CallbackTask;

/**
 * Created by didim99 on 15.09.18.
 */
public class NumericTask extends CallbackTask<Config, Result> {

  public NumericTask(Context context) {
    super(context);
  }

  @Override
  protected Result doInBackgroundInternal(Config config) {
    switch (config.getTaskType()) {
      case Config.TaskType.TRANSCENDENT:
        return new TranscendentSolver(config).solve();
      default:
        return null;
    }
  }
}
