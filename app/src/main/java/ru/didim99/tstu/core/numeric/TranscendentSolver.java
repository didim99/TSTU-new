package ru.didim99.tstu.core.numeric;

import android.content.Context;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.Timer;

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
  private static final double DEF_START = 2.0;
  private static final double DEF_END   = 4.0;

  static final class Method {
    static final int BINARY     = 1;
    static final int FALSE_POS  = 2;
    static final int TANGENT    = 3;
    static final int[] ALL = { BINARY, FALSE_POS, TANGENT};
  }

  private Config config;
  private ArrayList<StepEntry> solutionSteps;
  private Timer timer;

  TranscendentSolver(Config config) {
    this.config = config;
    if (config.isTConst())
      solutionSteps = new ArrayList<>();
    this.timer = new Timer();
  }

  Result solve() {
    Result result = new Result(config);
    Solver solver;

    if (!config.isRangeDefined())
      config.setRange(DEF_START, DEF_END);

    switch (config.getSolveMethod()) {
      case Method.BINARY:     solver = this::solveBinary; break;
      case Method.FALSE_POS:  solver = this::solveFalsePos; break;
      case Method.TANGENT:    solver = this::solveTangent; break;
      default: throw new IllegalArgumentException("Unknown solve method");
    }

    if (config.isTConst()) {
      result.setSolution(solver.solve(T_DEFAULT, config.getXStart(), config.getXEnd()));
      result.setSolutionSteps(solutionSteps);
    } else {
      result.setSolutionSeries(solveSeries(solver, config.getXStart(), config.getXEnd()));
      result.setSolveTime(timer.getMicros());
    }

    return result;
  }

  private ArrayList<Double> solveSeries(Solver solver, double start, double end) {
    ArrayList<Double> solution = new ArrayList<>();
    double t = T_START;
    timer.start();
    for (int i = 0; i <= T_STEPS; i++) {
      solution.add(solver.solve(t, start, end));
      t += T_STEP;
    }
    timer.stop();
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

  private double solveFalsePos(double t, double x, double xNext) {
    double y = f(x, t);
    double xTest = x - y * (xNext - x)/(f(xNext, t) - y);
    double yTest = f(xTest, t);

    if (solutionSteps != null)
      solutionSteps.add(new StepEntry(xNext, xTest, yTest));

    if (Math.abs(yTest) < EPSILON)
      return xTest;
    if (y * yTest < 0)
      return solveFalsePos(t, x, xTest);
    else
      return solveFalsePos(t, xTest, xNext);
  }

  private double solveTangent(double t, double start, double end) {
    double x, xNext, y;

    double d2f = (df(start + EPSILON, t) - df(start - EPSILON, t)) / (2 * EPSILON);
    if (f(start, t) * d2f > 0)
      x = start;
    else
      x = end;

    do {
      xNext = x - f(x, t) / df(x, t);
      y = f(xNext, t);

      if (solutionSteps != null)
        solutionSteps.add(new StepEntry(x, xNext, y));

      x = xNext;
    } while (Math.abs(y) > EPSILON);
    return xNext;
  }

  private double f(double x, double t) {
    return Math.pow(Math.tan(x), 2) - t * x;
  }

  private double df(double x, double t) {
    return 2 * Math.sin(x) / Math.pow(Math.cos(x), 3) - t;
  }

  public static String getTextResult(Context context, Result result) {
    StringBuilder sb = new StringBuilder();
    Config config = result.getConfig();
    String iterHeader = "";

    switch (config.getSolveMethod()) {
      case Method.BINARY:
        sb.append(context.getString(R.string.numeric_transcendentSolver_typeBinary));
        iterHeader = "x_start  x_end    y_c     ";
        break;
      case Method.FALSE_POS:
        sb.append(context.getString(R.string.numeric_transcendentSolver_typeFalsePos));
        iterHeader = "x_next   x_test   y";
        break;
      case Method.TANGENT:
        sb.append(context.getString(R.string.numeric_transcendentSolver_typeTangent));
        iterHeader = "x        x_next   y";
        break;
    }

    sb.append("\n\n");
    if (config.isTConst()) {
      sb.append(context.getString(R.string.numeric_tIfConst, T_DEFAULT)).append("\n");
      sb.append(context.getString(R.string.numeric_solution, result.getSolution()));
      sb.append("\n\n").append(context.getString(R.string.numeric_solutionSteps));
      sb.append("\n").append(iterHeader).append("\n");
      for (StepEntry entry : result.getSolutionSteps()) {
        sb.append(String.format(Locale.US, "%7.4f  %7.4f  %8.4f\n",
          entry.xStart, entry.xEnd, entry.y));
      }
    } else {
      sb.append(context.getString(R.string.numeric_solveTime, result.getSolveTime()))
        .append("\n").append(context.getString(R.string.numeric_solutionSeries))
        .append("\nt     x*     \n");
      double t = T_START;
      for (Double solution : result.getSolutionSeries()) {
        sb.append(String.format(Locale.US, "%5.2f %7.4f\n", t, solution));
        t += T_STEP;
      }
    }

    return sb.toString();
  }

  static class StepEntry {
    private double xStart, xEnd, y;

    private StepEntry(double xStart, double xEnd, double y) {
      this.xStart = xStart;
      this.xEnd = xEnd;
      this.y = y;
    }
  }

  @FunctionalInterface
  private interface Solver {
    double solve(double t, double start, double end);
  }
}
