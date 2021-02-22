package ru.didim99.tstu.core.math.numeric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.core.math.common.Matrix;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 08.10.18.
 */
public class LinearSystemSolver {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_LinSysSolver";

  private static final int DEFAULT_SIZE = 5;
  private static final int DEFAULT_MIN  = -10;
  private static final int DEFAULT_MAX  = 10;

  private Config config;
  private Result result;

  LinearSystemSolver(Config config) {
    this.config = config;
    if (config.getSize() == 0)
      config.setSize(DEFAULT_SIZE);
    this.result = new Result(config);
  }

  Result solve() {
    Matrix A, B;

    if (config.isTConst()) {
      do {
        A = Matrix.createRandom("A",
          config.getSize(), config.getSize(), DEFAULT_MIN, DEFAULT_MAX);
      } while (A.get(0, 0) == 0 || A.det() == 0);
      B = Matrix.createRandom("B",
        config.getSize(), 1, DEFAULT_MIN, DEFAULT_MAX);
    } else {
      try {
        ArrayList<Matrix> system = buildSystem(Utils.readFile(config.getFileName()));
        A = system.get(0);
        B = system.get(1);
        config.setSize(A.getRowCount());
      } catch (IOException e) {
        MyLog.e(LOG_TAG, "Can't open input file: " + e);
        return result;
      }
    }

    MyLog.d(LOG_TAG, "Solving system:\n" + A + "\n" + B);

    Matrix L = new Matrix("L", A, false);
    Matrix U = new Matrix("U", A, false);
    Matrix Y = new Matrix("Y", B, false);
    Matrix X = new Matrix("X", B, false);
    int n = config.getSize() - 1;
    double tmp;

    for (int i = 0; i <= n; i++) {
      L.set(i, 0, A.get(i, 0));
      U.set(0, i, A.get(0, i) / L.get(0, 0));

      for (int j = 1; j <= i; j++) {
        tmp = 0;
        for (int k = 0; k < j; k++)
          tmp += L.get(i, k) * U.get(k, j);
        L.set(i, j, A.get(i, j) - tmp);

        tmp = 0;
        for (int k = 0; k < j; k++)
          tmp += L.get(j, k) * U.get(k, i);
        U.set(j, i, (A.get(j, i) - tmp) / L.get(j, j));
      }

      if (i == 0) {
        Y.set(i, B.get(0) / L.get(0, 0));
      } else {
        tmp = 0;
        for (int k = 0; k < i; k++)
          tmp += L.get(i, k) * Y.get(k);
        Y.set(i, (B.get(i) - tmp) / L.get(i, i));
      }
    }

    X.set(n, Y.get(n));
    for (int i = n - 1; i >= 0; i--) {
      tmp = 0;
      for (int k = i + 1; k <= n; k++)
        tmp += U.get(i, k) * X.get(k);
      X.set(i, Y.get(i) - tmp);
    }

    Matrix test = A.multiply(X).sub(B);
    if (test.isZero())
      MyLog.d(LOG_TAG, "Solved successful: " + X);
    else
      MyLog.w(LOG_TAG, "Solving failed: " + test);

    ArrayList<Matrix> matrices = new ArrayList<>();
    result.setMatrixSeries(matrices);
    matrices.add(A);
    matrices.add(B);
    matrices.add(L);
    matrices.add(U);
    matrices.add(Y);
    matrices.add(X);

    return result;
  }

  private ArrayList<Matrix> buildSystem(ArrayList<String> text) {
    int rows = text.size(), columns = text.size();
    ArrayList<Matrix> system = new ArrayList<>(3);
    Matrix A = new Matrix("A", rows, columns);
    Matrix B = new Matrix("B", rows, 1);

    for (int i = 0; i < rows; i++) {
      String[] digits = text.get(i).split("\\s+");

      int j = 0;
      for (String digit : digits) {
        if (digit.isEmpty())
          continue;
        if (j == columns) {
          B.set(i, Double.parseDouble(digit));
          break;
        }
        A.set(i, j++, Double.parseDouble(digit));
      }
    }

    system.add(A);
    system.add(B);
    return system;
  }

  public static String buildSystem(Result result) {
    Matrix A = result.getMatrixSeries().get(0);
    Matrix B = result.getMatrixSeries().get(1);

    double x;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < A.getRowCount(); i++) {
      for (int j = 0; j < A.getColumnCount(); j++) {
        x = A.get(i, j);
        if (j > 0 && x >= 0)
          sb.append("+");
        if (Double.compare(Math.round(x), x) == 0)
          sb.append(Math.round(x));
        else
          sb.append(x);
        sb.append("*x").append(j+1);
      }

      x = B.get(i);
      sb.append("=");
      if (Double.compare(Math.round(x), x) == 0)
        sb.append(Math.round(x));
      else
        sb.append(x);
      sb.append("\n");
    }

    return sb.toString();
  }

  public static String buildSystemMatrix(Result result) {
    Matrix A = result.getMatrixSeries().get(0);
    Matrix B = result.getMatrixSeries().get(1);

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < A.getRowCount(); i++) {
      for (int j = 0; j < A.getColumnCount(); j++) {
        sb.append(String.format(Locale.US, "%8.3f", A.get(i, j)));
      }
      sb.append(String.format(Locale.US, "%8.3f", B.get(i)));
      sb.append("\n");
    }

    return sb.toString();
  }
}
