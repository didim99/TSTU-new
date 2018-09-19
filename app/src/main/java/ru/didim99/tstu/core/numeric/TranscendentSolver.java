package ru.didim99.tstu.core.numeric;

import android.content.Context;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.R;

/**
 * Created by didim99 on 15.09.18.
 */
public class TranscendentSolver {
  private static final double EPSILON   = 0.001;
  private static final double T_START   = 1.0;
  private static final double T_END     = 2.0;
  private static final int    T_STEPS   = 20;
  private static final double T_STEP    = (T_END - T_START) / T_STEPS;
  private static final double T_DEFAULT = (T_START + T_END) / 2;
  private static final double DEF_START = 1.0;
  private static final double DEF_END   = 3.0;

  public static final class Method {
    public static final int BINARY    = 1;
    public static final int ITERATION = 3;
  }

  private Config config;
  private ArrayList<StepEntry> solutionSteps;

  TranscendentSolver(Config config) {
    this.config = config;
    if (config.isTConst())
      solutionSteps = new ArrayList<>();
  }

  Result solve() {
    Result result = new Result(config);

    if (!config.isRangeDefined())
      config.setRange(DEF_START, DEF_END);

    switch (config.getSolveMethod()) {
      case Method.BINARY:
        if (config.isTConst()) {
          result.setSolution(solveBinary(T_DEFAULT, config.getXStart(), config.getXEnd()));
          result.setSolutionSteps(solutionSteps);
        } else
          result.setSolutionSeries(solveBinary(config.getXStart(), config.getXEnd()));
        break;
      case Method.ITERATION:
        break;
    }
    return result;
  }

  private ArrayList<Double> solveBinary(double start, double end) {
    ArrayList<Double> solution = new ArrayList<>();
    double t = T_START;
    for (int i = 0; i <= T_STEPS; i++) {
      solution.add(solveBinary(t, start, end));
      t += T_STEP;
    }
    return solution;
  }

  private double solveBinary(double t, double start, double end) {
    double c = (start + end) / 2;
    double yC = f(c, t);

    if (solutionSteps != null)
      solutionSteps.add(new StepEntry(start, end, yC));

    if (Math.abs(yC) < EPSILON)
      return c;
    if (f(start, t) * yC < 0)
      return solveBinary(t, start, c);
    else
      return solveBinary(t, c, end);
  }

  private static double f(double x, double t) {
    return Math.pow(Math.tan(x), 2) - t * x;
  }

  public static String getTextResult(Context context, Result result) {
    StringBuilder sb = new StringBuilder();
    Config config = result.getConfig();

    switch (config.getSolveMethod()) {
      case Method.BINARY:
        sb.append(context.getString(R.string.numeric_transcendentSolver_typeBinary));
        break;
    }

    sb.append("\n\n");
    if (config.isTConst()) {
      sb.append(context.getString(R.string.numeric_tIfConst, T_DEFAULT)).append("\n");
      sb.append(context.getString(R.string.numeric_solution, result.getSolution()));
      sb.append("\n\n").append(context.getString(R.string.numeric_solutionSteps));
      sb.append("\nx_start  x_end    y_c     \n");
      for (StepEntry entry : result.getSolutionSteps())
        sb.append(String.format(Locale.US, "%7.4f  %7.4f  %8.4f\n",
          entry.xStart, entry.xEnd, entry.yC));
    } else {
      sb.append(context.getString(R.string.numeric_solutionSeries));
      sb.append("\nt     x      \n");
      double t = T_START;
      for (Double solution : result.getSolutionSeries()) {
        sb.append(String.format(Locale.US, "%5.2f %7.4f\n", t, solution));
        t += T_STEP;
      }
    }

    return sb.toString();
  }

  static class StepEntry {
    private double xStart, xEnd, yC;

    private StepEntry(double xStart, double xEnd, double yC) {
      this.xStart = xStart;
      this.xEnd = xEnd;
      this.yC = yC;
    }
  }
}
