package ru.didim99.tstu.core.numeric;

import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 27.10.18.
 */
public class DiffSolver {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_diffSolver";

  private static double H0  = 0.5;
  private static double X0  = -1.0;
  private static double Y0  = 8.0;
  private static double B   = 2.0 * Math.PI - 1.0;

  private Result result;

  DiffSolver(Config config) {
    this.result = new Result(config);
  }

  Result solve() {
    return result;
  }

  private static double g(double x) {
    return 2 * (x - 2);
  }

  private static double phi(double x) {
    return Math.exp(Math.pow(-x, 2));
  }

  private static double psi(double x) {
    return 0.01;
  }
}
