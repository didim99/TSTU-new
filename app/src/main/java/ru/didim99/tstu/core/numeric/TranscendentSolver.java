package ru.didim99.tstu.core.numeric;

import java.util.ArrayList;

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
  private static final double DEF_END   = 10.0;

  public static final class Method {
    public static final int BINARY    = 1;
    public static final int ITERATION = 3;
  }

  private Config config;

  TranscendentSolver(Config config) {
    this.config = config;
  }

  Result solve() {
    Result result = new Result();
    switch (config.getSolveMethod()) {
      case Method.BINARY:
        if (config.isTConst())
          result.setSolution(solveBinary(T_DEFAULT, DEF_START, DEF_END));
        else
          result.setSolutionSeries(solveBinary(DEF_START, DEF_END));
        break;
      case Method.ITERATION:
        break;
    }
    return result;
  }

  private ArrayList<Double> solveBinary(double start, double end) {
    ArrayList<Double> solution = new ArrayList<>();
    double t = T_START;
    for (int i = 0; i < T_STEPS; i++) {
      solution.add(solveBinary(t, start, end));
      t += T_STEP;
    }
    return solution;
  }

  private double solveBinary(double t, double start, double end) {
    double c = (start + end) / 2;
    double yC = f(c, t);
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
}
