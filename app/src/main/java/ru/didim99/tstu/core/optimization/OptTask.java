package ru.didim99.tstu.core.optimization;

import android.content.Context;
import android.graphics.Paint;
import java.util.ArrayList;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.optimization.methods.FastDescentMethod;
import ru.didim99.tstu.core.optimization.methods.GradientMethod;
import ru.didim99.tstu.core.optimization.methods.PaulMethod;
import ru.didim99.tstu.core.optimization.methods.SimplexMethod;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 15.09.18.
 */
public class OptTask extends CallbackTask<Config, ArrayList<Result>> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_OptTask";

  private static final double VIEW_MARGIN = 0.1;
  private static final int STEPS_CLR = 0xfffaca40;
  private static final float STEPS_W = 2.5f;

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
      case Config.TaskType.MULTI_ARG:
        Result result = new Result();
        IsolinePlotter plotter = new IsolinePlotter();
        ExtremaFinderRN finderR2;
        FunctionRN function;

        switch (config.getMethod()) {
          case Config.Method.PAUL: finderR2 = new PaulMethod(); break;
          case Config.Method.SIMPLEX: finderR2 = new SimplexMethod(); break;
          case Config.Method.FDESCENT: finderR2 = new FastDescentMethod(); break;
          case Config.Method.GRADIENT: finderR2 = new GradientMethod(); break;
          default: throw new IllegalArgumentException("Unknown solve method");
        }

        switch (config.getFunction()) {
          case Config.Function.PARABOLA: function = Functions.parabola; break;
          case Config.Function.PARABOLA2: function = Functions.parabola2; break;
          case Config.Function.RESENBROK: function = Functions.resenbrok; break;
          default: throw new IllegalArgumentException("Unknown function");
        }

        result.setSolution(finderR2.find(function));
        result.setDescription(finderR2.getDescription(appContext.get()));
        MyLog.d(LOG_TAG, "Solution: " + result.getSolution());
        RectD vRange = finderR2.getRange().margin(VIEW_MARGIN)
          .coverRelative(plotter.getWidth(), plotter.getHeight());
        plotter.setBounds(vRange);
        plotter.plot(function);

        if (config.isUseLimits())
          plotter.drawLimits(Functions.limits);

        Paint paint = new Paint();
        paint.setColor(STEPS_CLR);
        paint.setStrokeWidth(STEPS_W);
        finderR2.drawSteps(plotter.getBitmap(), vRange, paint);
        result.setBitmap(plotter.getBitmap());

        results.add(result);
        return results;
      default:
        return null;
    }
  }
}
