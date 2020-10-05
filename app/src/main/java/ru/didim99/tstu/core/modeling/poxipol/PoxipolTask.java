package ru.didim99.tstu.core.modeling.poxipol;

import android.content.Context;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.modeling.GraphUtils;
import ru.didim99.tstu.core.optimization.math.PointRN;
import ru.didim99.tstu.utils.Timer;

/**
 * Created by didim99 on 05.10.20.
 */

public class PoxipolTask extends CallbackTask<PoxipolTask.Action, Result> {
  private static final String SERIES_C3 = "C3";

  public enum Action { INTEGRATE }

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
    system.integrate(pointList::add);
    result.setSeries(GraphUtils.buildSeries(pointList, SERIES_C3));
    result.setDescription(String.format(Locale.US,
      "Process time: %.3f s", system.getPoint().getX()));
    timer.stop();

    result.setTime(timer.getMillis());
    return result;
  }
}
