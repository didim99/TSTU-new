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
        //plotter.setBounds(new RectD(-0, 2, -0, 2));
        plotter.setBounds(new RectD(-10, 10, -10, 10));
        //plotter.plot(parabola);
        Result result = new Result();
        result.setBitmap(plotter.getBitmap());

        ExtremaFinderR2 finderR2 = new PaulMethod();
        finderR2.find(parabola);

        results.add(result);
        return results;
      default:
        return null;
    }
  }

  private static double PA = -4;
  private static double PB = -5;
  private static double PC = -3;
  private static double PD = -5;
  private static double PALPHA = Math.toRadians(115);

  private FunctionR2 parabola = (x, y) ->
    Math.pow((x - PA) * Math.cos(PALPHA) + (y - PB) * Math.sin(PALPHA), 2) / (PC * PC)
      + Math.pow((y - PB) * Math.cos(PALPHA) - (x - PA) * Math.sin(PALPHA), 2) / (PD * PD);

  private FunctionR2 resenbrok = (x, y) ->
    100 * Math.pow(y - x*x, 2) + Math.pow(1 - x, 2);

  interface ExtremaFinderR2 {
    Result find(FunctionR2 fun);
  }
}
