package ru.didim99.tstu.core.modeling;

import android.content.Context;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.modeling.processor.DynamicProcessor;
import ru.didim99.tstu.core.modeling.processor.StaticProcessor;
import ru.didim99.tstu.core.modeling.processor.VariableProcessor;

/**
 * Created by didim99 on 15.09.18.
 */
public class ModelingTask extends CallbackTask<Config, Result> {

  public ModelingTask(Context context) {
    super(context);
  }

  @Override
  protected Result doInBackgroundInternal(Config config) {
    Result result = new Result();
    VariableProcessor processor;

    switch (config.getTaskType()) {
      case Config.TaskType.STATIC_CURVE:
        processor = new StaticProcessor(config.getVariable());
        break;
      case Config.TaskType.DYNAMIC_CURVE:
        processor = new DynamicProcessor(config.getVariable());
        break;
      default:
        return result;
    }

    processor.process();
    result.setSeries(processor.getSeries());
    result.setDescription(processor.getDescription());
    return result;
  }
}
