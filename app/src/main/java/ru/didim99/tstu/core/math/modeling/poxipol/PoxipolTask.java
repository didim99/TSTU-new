package ru.didim99.tstu.core.math.modeling.poxipol;

import android.content.Context;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.math.common.FunctionTabulator;
import ru.didim99.tstu.core.math.common.PointD;
import ru.didim99.tstu.core.math.common.PointRN;
import ru.didim99.tstu.core.math.modeling.randproc.RandomProcessor;
import ru.didim99.tstu.core.math.optimization.multidim.UniformSearchMethod;
import ru.didim99.tstu.utils.Timer;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 05.10.20.
 */

public class PoxipolTask extends CallbackTask<PoxipolTask.Action, Result> {
  private static final String SERIES_C3 = "C3";
  private static final String SERIES_TAU = "Tau";

  public enum Action { INTEGRATE, OPTIMIZE, MODEL }

  public PoxipolTask(Context context) {
    super(context);
  }

  @Override
  protected Result doInBackgroundInternal(Action action) {
    Result result = new Result();
    Timer timer = new Timer();

    ArrayList<PointRN> pointList = new ArrayList<>();
    PoxipolSystem system = new PoxipolSystem();

    timer.start();
    switch (action) {
      case INTEGRATE:
        system.integrate(pointList::add);
        result.setSeries(Utils.buildSeries(pointList, SERIES_C3));
        result.setDescription(String.format(Locale.US,
          "Process time: %.3f s", system.getPoint().getX()));
        break;
      case OPTIMIZE:
        UniformSearchMethod optimizer = new UniformSearchMethod();
        optimizer.setBoundaries(system.getMinBound(), system.getMaxBound());
        PointD res = optimizer.find(system);
        result.setDescription(
          String.format(Locale.US, "Process time: %.3f s\n", res.getY())
          + String.format(Locale.US, "Optimal area: %.3f m^2", res.getX()));
        FunctionTabulator tabulator = new FunctionTabulator(system);
        result.setSeries(Utils.buildSeries(tabulator.tabulate(
          PoxipolSystem.F_MIN, PoxipolSystem.F_MAX), SERIES_TAU));
        break;
      case MODEL:
        system.setF(PoxipolSystem.F_MIN);
        RandomProcessor processor = new RandomProcessor(true);
        PointD randomParams = processor.configure(PoxipolSystem.RANDOM_PARAMS);
        system.setTTSource(processor.getProcess()::next);
        system.integrate(pointList::add);
        result.setSeries(Utils.buildSeries(pointList, SERIES_C3));
        processor.computeDelta(randomParams);
        result.setDescription(processor.getParamsDescription());
        break;
    }

    timer.stop();
    result.setTime(timer.getMillis());
    return result;
  }
}
