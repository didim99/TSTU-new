package ru.didim99.tstu.core.oop;

import ru.didim99.tstu.core.math.numeric.Matrix;

/**
 * Created by didim99 on 07.03.19.
 */
public class SquareMatrix extends Matrix {

  private SquareMatrix(String name, int size) {
    super(name, size, size);
  }

  private SquareMatrix(Matrix src) {
    super(src.getName(), src, true);
    if (!src.isSquare()) throw new
      IllegalArgumentException("Source matrix must be square");
  }

  public SquareMatrix transpose() {
    int size = getRowCount();
    SquareMatrix res = new SquareMatrix(
      String.format("%s", getName()), size);
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        res.set(i, j, get(j, i));
      }
    }

    return res;
  }

  public SquareMatrix multiply(SquareMatrix other) {
    if (getColumnCount() != other.getRowCount()) {
      throw new IllegalArgumentException(
        "Impossible operation");
    }

    SquareMatrix res = new SquareMatrix(String.format("%sx%s",
      getName(), other.getName()), getRowCount());
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

  public static SquareMatrix createRandom(String name, int size,
                                          int minValue, int maxValue) {
    return new SquareMatrix(Matrix.createRandom(
      name, size, size, minValue, maxValue));
  }
}
