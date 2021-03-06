package ru.didim99.tstu.core.math.optimization;

import android.content.Context;
import android.graphics.Paint;
import java.util.ArrayList;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.math.common.Fine;
import ru.didim99.tstu.core.math.common.FunctionRN;
import ru.didim99.tstu.core.math.common.Limit;
import ru.didim99.tstu.core.math.common.RectD;
import ru.didim99.tstu.core.math.optimization.multidim.DownhillMethod;
import ru.didim99.tstu.core.math.optimization.multidim.ExtremaFinderRN;
import ru.didim99.tstu.core.math.optimization.multidim.GradientMethod;
import ru.didim99.tstu.core.math.optimization.multidim.PaulMethod;
import ru.didim99.tstu.core.math.optimization.multidim.SimplexMethod;
import ru.didim99.tstu.core.math.optimization.multidim.UniformSearchMethod;
import ru.didim99.tstu.core.math.optimization.variation.EulerMethod;
import ru.didim99.tstu.core.math.optimization.variation.ExtremaFinderFunc;
import ru.didim99.tstu.core.math.optimization.variation.SweepMethod;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 15.09.18.
 */
public class OptTask extends CallbackTask<Config, ArrayList<Result>> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_OptTask";

  private static final double VIEW_MARGIN = 0.1;
  private static final int CLR_STEPS = 0xfffaca40;
  private static final int CLR_GSTEPS = 0xfffa4c40;
  private static final int CLR_FINISH = 0xff40fa46;
  private static final float W_STEPS = 2.5f;
  private static final float W_GSTEPS = 20f;

  public enum State { PENDING, INIT, SOLVE, DRAW_BASE,
    DRAW_LIMITS, DRAW_STEPS, COMPLETED }

  private State state;
  private StateChangeListener listener;

  public OptTask(Context context) {
    super(context);
    state = State.PENDING;
  }

  public void setStateChangeListener(StateChangeListener listener) {
    this.listener = listener;
    publishProgress();
  }

  @Override
  protected ArrayList<Result> doInBackgroundInternal(Config config) {
    ArrayList<Result> results = new ArrayList<>();
    Result result = new Result();

    switch (config.getTaskType()) {
      case Config.TaskType.SINGLE_ARG:
        ExtremaFinder finder = new ExtremaFinder();
        for (int method : ExtremaFinder.Method.ALL)
          results.add(finder.solve(method));
        return results;
      case Config.TaskType.MULTI_ARG:
        applyState(State.INIT);
        IsolinePlotter plotter = new IsolinePlotter();
        ExtremaFinderRN finderRN;
        FunctionRN function;
        Limit[] limits;
        Fine fine;

        switch (config.getMethod()) {
          case Config.Method.UNIFORM: finderRN = new UniformSearchMethod(); break;
          case Config.Method.PAUL: finderRN = new PaulMethod(); break;
          case Config.Method.SIMPLEX: finderRN = new SimplexMethod(); break;
          case Config.Method.FDESCENT: finderRN = new DownhillMethod(); break;
          case Config.Method.GRADIENT: finderRN = new GradientMethod(); break;
          default: throw new IllegalArgumentException("Unknown solve method");
        }

        switch (config.getFunction()) {
          case Config.FunctionType.PARABOLA: function = Functions.parabola[0]; break;
          case Config.FunctionType.PARABOLA2: function = Functions.parabola[1]; break;
          case Config.FunctionType.PARABOLA3: function = Functions.parabola[2]; break;
          case Config.FunctionType.RESENBROK: function = Functions.resenbrok; break;
          default: throw new IllegalArgumentException("Unknown function");
        }

        switch (config.getLimitType()) {
          case Config.LimitType.INEQUALITY: limits = Functions.limitsInEq; break;
          case Config.LimitType.EQUALITY: limits = Functions.limitsEq; break;
          default: throw new IllegalArgumentException("Unknown Limit type");
        }

        switch (config.getLimitMethod()) {
          case Config.FineMethod.INTERNAL: fine =
            new Fine(Fine.Type.INTERNAL, limits); break;
          case Config.FineMethod.EXTERNAL: fine =
            new Fine(Fine.Type.EXTERNAL, limits); break;
          case Config.FineMethod.COMBINED: fine =
            new Fine(Fine.Type.COMBINED, limits); break;
          default: throw new IllegalArgumentException("Unknown fine type");
        }

        applyState(State.SOLVE);
        if (finderRN instanceof UniformSearchMethod)
          ((UniformSearchMethod) finderRN).setDefaultBounds(2);
        result.setSolution(config.isUseLimits() ?
          finderRN.find(function, fine) : finderRN.find(function));
        result.setDescription(finderRN.getDescription(appContext.get()));

        applyState(State.DRAW_BASE);
        MyLog.d(LOG_TAG, "Solution: " + result.getSolution());
        RectD vRange = finderRN.getRange().margin(VIEW_MARGIN)
          .coverRelative(plotter.getWidth(), plotter.getHeight());
        plotter.setBounds(vRange);
        plotter.plot(function);

        if (config.isUseLimits()) {
          applyState(State.DRAW_LIMITS);
          plotter.drawLimits(limits);
        }

        applyState(State.DRAW_STEPS);
        Paint paint = new Paint();
        paint.setColor(CLR_STEPS);
        paint.setStrokeWidth(W_STEPS);
        finderRN.drawSteps(plotter.getBitmap(), vRange, paint);

        paint.setStrokeWidth(W_GSTEPS);
        paint.setStrokeCap(Paint.Cap.ROUND);
        if (config.isUseLimits()) {
          paint.setColor(CLR_GSTEPS);
          finderRN.drawGlobalSteps(plotter.getBitmap(), vRange, paint);
        }
        paint.setColor(CLR_FINISH);
        finderRN.drawSolution(plotter.getBitmap(), vRange, paint);

        result.setBitmap(plotter.getBitmap());
        applyState(State.COMPLETED);
        results.add(result);
        return results;
      case Config.TaskType.VARIATION:
        ExtremaFinderFunc finderFunc;

        switch (config.getMethod()) {
          case Config.VarMethod.SWEEP:
            finderFunc = new SweepMethod(Functions.eulerP, Functions.eulerF);
            break;
          case Config.VarMethod.EULER:
            finderFunc = new EulerMethod(Functions.functional);
            break;
          default: throw new IllegalArgumentException("Unknown solve method");
        }

        finderFunc.solve(Functions.functionalStart, Functions.functionalEnd);

        result.setSolutionSeries(finderFunc.getSolution());
        result.setReference(finderFunc.getReference(
          Functions.functionalRef, Functions.functionalStart, Functions.functionalEnd));
        if (config.isCalcDelta())
          result.setDelta(finderFunc.getDelta(result.getReference()));
        result.setDescription(finderFunc.getDescription(appContext.get(), result));
        results.add(result);
        return results;
      default:
        return null;
    }
  }

  private void applyState(State newState) {
    this.state = newState;
    publishProgress();
  }

  @Override
  protected void onProgressUpdate(Void... values) {
    if (listener != null)
      listener.onTaskStateChanged(state);
  }

  public interface StateChangeListener {
    void onTaskStateChanged(State newState);
  }
}
