package ru.didim99.tstu.core.optimization;

import android.content.Context;
import java.util.ArrayList;
import ru.didim99.tstu.core.CallbackTask;

/**
 * Created by didim99 on 15.09.18.
 */
public class OptTask extends CallbackTask<Config, ArrayList<Result>> {

  public OptTask(Context context) {
    super(context);
  }

  @Override
  protected ArrayList<Result> doInBackgroundInternal(Config config) {
    ArrayList<Result> results = new ArrayList<>();
    switch (config.getTaskType()) {
      case Config.TaskType.SINGLE_ARG:
        ExtremaFinder finder = new ExtremaFinder();
        for (int method : ExtremaFinder.Method.ALL)
          results.add(finder.solve(method));
        return results;
      case Config.TaskType.ZERO_ORDER:
        IsolinePlotter plotter = new IsolinePlotter();
        plotter.setBounds(new RectD(-1, 2, -1, 2));
        plotter.plot((x, y) -> x*x+y*y);
        Result result = new Result();
        result.setBitmap(plotter.getBitmap());
        results.add(result);
        return results;
      default:
        return null;
    }
  }
}
