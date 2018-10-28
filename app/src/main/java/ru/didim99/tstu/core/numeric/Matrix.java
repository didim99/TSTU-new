package ru.didim99.tstu.core.numeric;

import java.util.Locale;
import java.util.Random;

/**
 * Created by didim99 on 08.10.18.
 */
public class Matrix {
  private static final double EPSILON = 10E-5;

  private double[][] data;
  private String name;

  Matrix(String name, int rows, int columns) {
    this.data = new double[rows][columns];
    this.name = name;
  }

  Matrix(String name, double[][] data) {
    this.data = data;
    this.name = name;
  }

  Matrix(String name, Matrix other, boolean copyValues) {
    this(name, other.getRowCount(), other.getColumnCount());
    if (copyValues) {
      for (int i = 0; i < getRowCount(); i++) {
        for (int j = 0; j < getColumnCount(); j++) {
          set(i, j, other.get(i, j));
        }
      }
    }
  }

  double get(int row, int column) {
    return data[row][column];
  }

  void set(int row, int column, double value) {
    data[row][column] = value;
  }

  double get(int index) {
    if (getRowCount() == 1)
      return get(0, index);
    else if (getColumnCount() == 1)
      return get(index, 0);
    else throw new IllegalStateException(
      "Can only be used with single-row or single-column matrix");
  }

  void set(int index, double value) {
    if (getRowCount() == 1)
      set(0, index, value);
    else if (getColumnCount() == 1)
      set(index, 0, value);
    else throw new IllegalStateException(
      "Can only be used with single-row or single-column matrix");
  }

  int getRowCount() {
    return data.length;
  }

  int getColumnCount() {
    return data[0].length;
  }

  Matrix add(Matrix other) {
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

  Matrix sub(Matrix other) {
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

  Matrix multiply(Matrix other) {
    if (getColumnCount() != other.getRowCount()) {
      throw new IllegalArgumentException(
        "Impossible operation");
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

  double det() {
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

  boolean isZero() {
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

    for (int i = 0; i < getRowCount(); i++) {
      sb.append("\n");
      for (int j = 0; j < getColumnCount(); j++) {
        sb.append(String.format(Locale.US, "%8.3f", get(i, j)));
      }
    }

    return sb.toString();
  }

  static Matrix createRandom(String name, int rows, int columns,
                             int minValue, int maxValue) {
    Matrix matrix = new Matrix(name, rows, columns);
    Random random = new Random();

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        matrix.set(i, j, randInRange(random, minValue, maxValue));
      }
    }

    return matrix;
  }

  private static int randInRange(Random random, int minValue, int maxValue) {
    return maxValue - random.nextInt(maxValue - minValue + 1);
  }
}