package ru.didim99.tstu.core.math.common;

import java.util.Locale;
import java.util.Random;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 08.10.18.
 */
public class Matrix {
  private static final double EPSILON = 10E-5;

  private final double[][] data;
  private String name;

  public Matrix(String name, int rows, int columns) {
    this.data = new double[rows][columns];
    this.name = name;
  }

  public Matrix(String name, double[][] data) {
    this.data = data;
    this.name = name;
  }

  public Matrix(String name, Matrix other, boolean copyValues) {
    this(name, other.getRowCount(), other.getColumnCount());
    if (copyValues) {
      for (int i = 0; i < getRowCount(); i++) {
        for (int j = 0; j < getColumnCount(); j++) {
          set(i, j, other.get(i, j));
        }
      }
    }
  }

  public double get(int row, int column) {
    return data[row][column];
  }

  public void set(int row, int column, double value) {
    data[row][column] = value;
  }

  public void add(int row, int column, double value) {
    data[row][column] += value;
  }

  public double get(int index) {
    if (getRowCount() == 1)
      return get(0, index);
    else if (getColumnCount() == 1)
      return get(index, 0);
    else throw new IllegalStateException(
      "Can only be used with single-row or single-column matrix");
  }

  public void set(int index, double value) {
    if (getRowCount() == 1)
      set(0, index, value);
    else if (getColumnCount() == 1)
      set(index, 0, value);
    else throw new IllegalStateException(
      "Can only be used with single-row or single-column matrix");
  }

  public int getRowCount() {
    return data.length;
  }

  public int getColumnCount() {
    return data[0].length;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isSquare() {
    return getRowCount() == getColumnCount();
  }

  public boolean sameAs(Matrix other) {
    return getRowCount() == other.getRowCount()
      && getColumnCount() == other.getColumnCount();
  }

  public Matrix add(Matrix other) {
    if (other.getColumnCount() != getColumnCount()) {
      throw new IllegalArgumentException(
        "Can only be used with same size matrix");
    }
    if (other.getRowCount() != getRowCount()) {
      throw new IllegalArgumentException(
        "Can only be used with same size matrix");
    }

    Matrix res = new Matrix(String.format("%s+%s", name, other.name),
      getRowCount(), getColumnCount());
    for (int i = 0; i < getRowCount(); i++) {
      for (int j = 0; j < getColumnCount(); j++) {
        res.set(i, j, get(i, j) + other.get(i, j));
      }
    }

    return res;
  }

  public Matrix sub(Matrix other) {
    if (other.getColumnCount() != getColumnCount()) {
      throw new IllegalArgumentException(
        "Can only be used with same size matrix");
    }
    if (other.getRowCount() != getRowCount()) {
      throw new IllegalArgumentException(
        "Can only be used with same size matrix");
    }

    Matrix res = new Matrix(String.format("%s-%s", name, other.name),
      getRowCount(), getColumnCount());
    for (int i = 0; i < getRowCount(); i++) {
      for (int j = 0; j < getColumnCount(); j++) {
        res.set(i, j, get(i, j) - other.get(i, j));
      }
    }

    return res;
  }

  public Matrix multiply(double factor) {
    Matrix res = new Matrix(String.format(
      Locale.US, "%.1f%s", factor, name),
      getRowCount(), getColumnCount());
    for (int i = 0; i < getRowCount(); i++) {
      for (int j = 0; j < getColumnCount(); j++) {
        res.set(i, j, get(i, j) * factor);
      }
    }

    return res;
  }

  public Matrix multiply(Matrix other) {
    if (getColumnCount() != other.getRowCount()) {
      throw new IllegalArgumentException(
        "Impossible operation (" + getColumnCount() + " != " + other.getRowCount() + ")");
    }
    
    Matrix res = new Matrix(String.format("%sx%s", name, other.name),
      getRowCount(), other.getColumnCount());
    for (int i = 0; i < getRowCount(); i++) {
      for (int j = 0; j < other.getColumnCount(); j++) {
        double tmp = 0;
        for (int k = 0; k < getColumnCount(); k++)
          tmp += get(i, k) * other.get(k, j);
        res.set(i, j, tmp);
      }
    }

    return res;
  }

  public PointD multiplyTransposed(PointD vector) {
    if (getRowCount() != vector.size()) {
      throw new IllegalArgumentException(
        "Impossible operation (" + getRowCount() + " != " + vector.size() + ")");
    }

    PointD result = new PointD(getColumnCount());
    for (int i = 0; i < getColumnCount(); i++) {
      for (int j = 0; j < getRowCount(); j++) {
        result.add(i, vector.get(j) * get(j, i));
      }
    }

    return result;
  }

  public double det() {
    if (getColumnCount() != getRowCount()) {
      throw new IllegalStateException(
        "Can only be used with square matrix");
    }

    if (getRowCount() == 1)
      return get(0, 0);

    if (getRowCount() == 2) {
      return get(0, 0) * get(1, 1)
        - get(1, 0) * get(0, 1);
    }

    return detInternal(data, 1);
  }

  private double detInternal(double[][] matrix, double element) {
    if (matrix.length > 1) {
      double result = 0;
      double[][] tmpMinor = new double[matrix.length - 1][matrix[0].length - 1];
      for (int c = 0; c < matrix[0].length; c++) {
        for (int i = 1; i < matrix.length; i++) {
          for (int j = 0; j < matrix[0].length; j++) {
            if (j < c)
              tmpMinor[i - 1][j] = matrix[i][j];
            else if (j > c)
              tmpMinor[i - 1][j - 1] = matrix[i][j];
          }
        }
        double paramForSub = Math.pow(-1, c + 2) * matrix[0][c] * element;
        result += detInternal(tmpMinor, paramForSub);
      }
      return result;
    } else
      return element * matrix[0][0];
  }

  public Matrix transpose() {
    Matrix res = new Matrix(String.format(
      Locale.US, "%s_t", name),
      getColumnCount(), getRowCount());
    for (int i = 0; i < getRowCount(); i++) {
      for (int j = 0; j < getColumnCount(); j++) {
        res.set(j, i, get(i, j));
      }
    }

    return res;
  }

  public double maxDeviation(boolean positive) {
    double deviation = 0;
    for (int i = 0; i < getRowCount(); i++) {
      for (int j = 0; j < getColumnCount(); j++) {
        double v = get(i, j);
        if (positive && v > deviation
          || !positive && v < deviation)
          deviation = v;
      }
    }

    return deviation;
  }

  public boolean isZero() {
    for (int i = 0; i < getRowCount(); i++) {
      for (int j = 0; j < getColumnCount(); j++) {
        if (Math.abs(get(i, j)) > EPSILON)
          return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append(String.format(Locale.US, "Matrix %s (%dx%d):",
      name, getRowCount(), getColumnCount()));

    int l1 = 0, l2 = 0;
    double max = -Double.MAX_VALUE, min = -max, d;
    for (int i = 0; i < getRowCount(); i++) {
      for (int j = 0; j < getColumnCount(); j++) {
        d = get(i, j);
        if (d > max) max = d;
        if (d < min) min = d;
      }
    }

    if (min < 0) l1++;
    if (max < 0) l2++;
    int imin = (int) Math.abs(min);
    int imax = (int) Math.abs(max);
    while (imin > 0) { imin /= 10; l1++; }
    while (imax > 0) { imax /= 10; l2++; }
    int l = Math.max(l1, l2) + 6;

    String fmt = String.format(Locale.US, "%%%d.3f", l);
    for (int i = 0; i < getRowCount(); i++) {
      sb.append("\n");
      for (int j = 0; j < getColumnCount(); j++) {
        sb.append(String.format(Locale.US, fmt, get(i, j)));
      }
    }

    return sb.toString();
  }

  public void randomize(Random random, double min, double max) {
    if (random == null) random = new Random();

    for (int i = 0; i < getRowCount(); i++) {
      for (int j = 0; j < getColumnCount(); j++) {
        set(i, j, Utils.randInRangeD(random, min, max));
      }
    }
  }

  public static Matrix createRandom(String name, int rows, int columns,
                                    int minValue, int maxValue) {
    Matrix matrix = new Matrix(name, rows, columns);
    Random random = new Random();

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        matrix.set(i, j, Utils.randInRange(random, minValue, maxValue));
      }
    }

    return matrix;
  }
}
