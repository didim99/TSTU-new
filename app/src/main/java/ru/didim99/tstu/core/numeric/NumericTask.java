package ru.didim99.tstu.core.numeric;

import android.content.Context;
import java.util.ArrayList;
import ru.didim99.tstu.core.CallbackTask;

/**
 * Created by didim99 on 15.09.18.
 */
public class NumericTask extends CallbackTask<Config, ArrayList<Result>> {

  public NumericTask(Context context) {
    super(context);
  }

  @Override
  protected ArrayList<Result> doInBackgroundInternal(Config config) {
    ArrayList<Result> results = new ArrayList<>();
    switch (config.getTaskType()) {
      case Config.TaskType.TRANSCENDENT:
        for (int method : TranscendentSolver.Method.ALL) {
          Config newConfig = new Config.Builder(config).solveMethod(method).build();
          results.add(new TranscendentSolver(newConfig).solve());
        }
        return results;
      case Config.TaskType.LINEAR_SYSTEM:
        results.add(new LinearSystemSolver(config).solve());
        return results;
      case Config.TaskType.INTERPOLATION:
        results.add(new Interpolator(config).interpolate());
        return results;
      case Config.TaskType.INTEGRATION:
        results.add(new Integrator(config).solve());
        return results;
      case Config.TaskType.DIFF_EQUATION:
        results.add(new DiffSolver(config).solve());
        return results;
      default:
        return null;
    }
  }
}
