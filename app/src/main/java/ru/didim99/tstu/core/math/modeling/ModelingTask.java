package ru.didim99.tstu.core.math.modeling;

import android.content.Context;
import com.jjoe64.graphview.series.Series;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.math.modeling.processor.DSProcessor;
import ru.didim99.tstu.core.math.modeling.processor.LDProcessor;
import ru.didim99.tstu.core.math.modeling.processor.LSProcessor;
import ru.didim99.tstu.core.math.modeling.processor.Processor;
import ru.didim99.tstu.core.math.modeling.randproc.RandomProcessor;
import ru.didim99.tstu.core.math.common.PointRN;

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
    Processor processor;

    switch (config.getTaskType()) {
      case Config.TaskType.LUMPED_STATIC_CURVE:
        processor = new LSProcessor(config.getVariable());
        break;
      case Config.TaskType.LUMPED_DYNAMIC_CURVE:
        processor = new LDProcessor(config.getVariable());
        break;
      case Config.TaskType.DIST_STATIC_CURVES:
        processor = new DSProcessor(config.getVariable());
        break;
      case Config.TaskType.RANDOM_PROCESSING:
        processor = new RandomProcessor(config.isOptimize());
        break;
      default:
        return result;
    }

    processor.process();

    switch (config.getTaskType()) {
      case Config.TaskType.LUMPED_STATIC_CURVE:
      case Config.TaskType.LUMPED_DYNAMIC_CURVE:
        result.addSeries(processor.getSeries());
        break;
      case Config.TaskType.DIST_STATIC_CURVES:
      case Config.TaskType.RANDOM_PROCESSING:
        Series<PointRN> series;
        while ((series = processor.getSeries()) != null)
          result.addSeries(series);
        break;
    }

    result.setDescription(processor.getDescription());
    return result;
  }
}
