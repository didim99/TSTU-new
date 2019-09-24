package ru.didim99.tstu.core.optimization;

import android.content.Context;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.Timer;

/**
 * Created by didim99 on 05.09.19.
 */
public class ExtremaFinder {
  private static final int ITERATIONS   = 10000;
  private static final double EPSILON   = 0.01;
  public static final double DEF_START = -1.5;
  public static final double DEF_END   = 7.0;

  static final class Method {
    static final int BINARY     = 1;
    static final int GOLD_SEC   = 2;
    static final int FIBONACCI  = 3;
    static final int[] ALL = { BINARY, GOLD_SEC, FIBONACCI };
  }

  private Timer timer;
  private boolean verbose;
  private ArrayList<StepEntry> solutionSteps;
  private int fCalc, tFCalc;

  ExtremaFinder() {
    this.timer = new Timer();
  }

  Result solve(int method) {
    Result result = new Result();
    result.setMethod(method);
    PointD solution = null;
    Solver solver;

    switch (method) {
      case Method.BINARY:     solver = this::solveBinary; break;
      case Method.GOLD_SEC:   solver = this::solveGoldSec; break;
      case Method.FIBONACCI:  solver = this::solveFibonacci; break;
      default: throw new IllegalArgumentException("Unknown solve method");
    }

    verbose = false;
    timer.start();
    for (int i = 0; i < ITERATIONS; i++)
      solution = solver.solve(DEF_START, DEF_END);
    timer.stop();

    fCalc = 0;
    verbose = true;
    solutionSteps = new ArrayList<>();
    solver.solve(DEF_START, DEF_END);
    int tFCalcCount = 0;

    switch (method) {
      case Method.BINARY:     tFCalcCount = splitIterations(0.75) * 2; break;
      case Method.GOLD_SEC:   tFCalcCount = splitIterations(0.618) + 1; break;
      case Method.FIBONACCI:  tFCalcCount = tFCalc; break;
    }

    result.setSolution(solution);
    result.setSolutionTime(timer.getMicros() / ITERATIONS);
    result.setSolutionSteps(solutionSteps);
    result.setTFCalcCount(tFCalcCount);
    result.setFCalcCount(fCalc);
    return result;
  }

  private PointD solveBinary(double start, double end) {
    double minX = 0;

    while (end - start > EPSILON) {
      double delta = (end - start) / 4;
      double newStart = start + delta;
      double newEnd = end - delta;

      if (f(newStart) > f(newEnd))
        start = minX = newStart;
      else
        end = minX = newEnd;

      if (verbose) {
        solutionSteps.add(new StepEntry(start, end));
        fCalc += 2;
      }
    }

    return new PointD(minX, f(minX));
  }

  private PointD solveGoldSec(double start, double end) {
    double delta = (end - start) * 0.382;
    double newStart = start + delta;
    double newEnd = end - delta;
    double f1 = f(newStart);
    double f2 = f(newEnd);
    double minX = 0;

    if (verbose) fCalc += 2;

    while (end - start > EPSILON) {
      if (f1 > f2) {
        start = newStart;
        minX = newEnd;
      } else {
        end = newEnd;
        minX = newStart;
      }

      if (verbose) {
        solutionSteps.add(new StepEntry(start, end));
        fCalc++;
      }

      delta = (end - start) * 0.38;
      if (f1 > f2) {
        f1 = f2;
        newStart = newEnd;
        newEnd = end - delta;
        f2 = f(newEnd);
      } else {
        f2 = f1;
        newEnd = newStart;
        newStart = start + delta;
        f1 = f(newStart);
      }
    }

    return new PointD(minX, f(minX));
  }

  private PointD solveFibonacci(double start, double end) {
    double minX = 0;

    int n = 1;
    double delta = (end - start) / EPSILON;
    while (delta > fibonacci(n)) n++;
    n++;

    if (verbose) tFCalc = n + 1;

    int k = 1;
    while (k < n) {
      delta = (end - start) * fibonacci(n - k) / fibonacci(n - k++ + 1);
      double newEnd = start + delta;
      double newStart = end - delta;

      if (f(newStart) < f(newEnd)) {
        end = newEnd;
        minX = newStart;
      } else {
        start = newStart;
        minX = newEnd;
      }

      if (verbose) {
        solutionSteps.add(new StepEntry(start, end));
        fCalc += 2;
      }
    }

    return new PointD(minX, f(minX));
  }

  private PointD solveFibonacci2(double start, double end) {
    int n = 1;
    double delta = (end - start) / EPSILON;
    while (delta > fibonacci(n)) n++;
    n++;

    delta = (end - start) / fibonacci(n);
    double newStart = start + delta * fibonacci(n - 2);
    double newEnd = start + delta * fibonacci(n - 1);
    double f1 = f(newStart);
    double f2 = f(newEnd);

    if (verbose) {
      tFCalc = n + 1;
      fCalc = 2;
    }

    while (n-- > 1) {
      if (f1 > f2) {
        start = newStart;
        newStart = newEnd;
        newEnd = end - (newStart - start);
        f1 = f2;
        f2 = f(newEnd);
      } else {
        end = newEnd;
        newEnd = newStart;
        newStart = start + (end - newEnd);
        f2 = f1;
        f1 = f(newStart);
      }

      if (verbose) {
        solutionSteps.add(new StepEntry(start, end));
        fCalc++;
      }
    }

    double minX = (start + end) / 2;
    return new PointD(minX, f(minX));
  }

  private static int splitIterations(double delta) {
    return (int) Math.ceil(-(Math.log((DEF_END - DEF_START)
      / EPSILON) / Math.log(delta)));
  }

  private static double f(double x) {
    return Math.pow(x, 3) + 1.5 * Math.pow(x, 2) - 6 * x - 12;
  }

  private static int fibonacci(int n) {
    int[] f = new int[] {0, 1, 0};
    for (int i = 2; i <= n; i++)
      f[i % 3] = f[(i + 1) % 3] + f[(i + 2) % 3];
    return f[n % 3];
  }

  public static String getTextResult(Context context, Result result) {
    StringBuilder sb = new StringBuilder();
    PointD s = result.getSolution();

    switch (result.getMethod()) {
      case Method.BINARY:
        sb.append(context.getString(R.string.opt_localExtrema_typeBinary));
        break;
      case Method.GOLD_SEC:
        sb.append(context.getString(R.string.opt_localExtrema_typeGoldSec));
        break;
      case Method.FIBONACCI:
        sb.append(context.getString(R.string.opt_localExtrema_typeFibonacci));
        break;
    }

    sb.append("\n\n");
    sb.append(context.getString(
      R.string.opt_solution, s.getX(), s.getY()));
    sb.append("\n").append(context.getString(
      R.string.numeric_solveTime, result.getSolutionTime()));
    sb.append("\n").append(context.getString(
      R.string.opt_iterations, result.getSolutionSteps().size()));
    sb.append("\n").append(context.getString(
      R.string.opt_fCalcCount, result.getFCalcCount(), result.getTFCalcCount()));
    sb.append("\n\n").append(context.getString(
      R.string.numeric_solutionSteps));
    sb.append("\nx_start   x_end\n");
    for (StepEntry entry : result.getSolutionSteps()) {
      sb.append(String.format(Locale.US, "%7.4f   %7.4f\n",
        entry.xStart, entry.xEnd));
    }

    return sb.toString();
  }

  static class StepEntry {
    private double xStart, xEnd;

    private StepEntry(double xStart, double xEnd) {
      this.xStart = xStart;
      this.xEnd = xEnd;
    }
  }

  @FunctionalInterface
  private interface Solver {
    PointD solve(double start, double end);
  }
}