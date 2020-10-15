package ru.didim99.tstu.core.math.modeling.poxipol;

/**
 * Created by didim99 on 04.10.20.
 */

public enum PoxipolPointMapper {
  TAU, T, K1, K2, K3, K4, C1, C2, C3, C4, C5, C6;

  public static final PoxipolPointMapper[] values = PoxipolPointMapper.values();

  public static PoxipolPointMapper getByOrdinal(int ordinal) {
    if (ordinal < 0 || ordinal >= size())
      throw new IllegalArgumentException("Variable index out of range");
    return values[ordinal];
  }

  public static String[] names() {
    String[] names = new String[size()];
    for (int i = 0; i < size(); i++)
      names[i] = values[i].name();
    return names;
  }

  private static int size() {
    return values.length;
  }
}
