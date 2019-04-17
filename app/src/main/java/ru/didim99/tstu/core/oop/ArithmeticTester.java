package ru.didim99.tstu.core.oop;

import java.util.Locale;
import java.util.Random;

/**
 * Created by didim99 on 17.04.19.
 */
public class ArithmeticTester {
  private final static double X_START = 0.0;
  private final static double X_END = 4.0;
  private final static double X_STEP = 0.2;
  private final static int RND_MIN = -20;
  private final static int RND_MAX = 20;

  public static String test(int size) {
    StringBuilder sb = new StringBuilder();
    Random random = new Random();
    double[] a = new double[size];
    double[] c = new double[size];
    int[] b = new int[size];
    double x = X_START;

    sb.append(String.format(Locale.ROOT,
      "%3s | %8s | %4s | %8s\n", "I", "A", "B", "C"));
    for (int i = 0; x <= X_END; i++, x += X_STEP) {
      sb.append(String.format(Locale.ROOT, "%3d | ", i));

      try {
        a[i] = f(x);
        b[i] = RND_MIN + random.nextInt(RND_MAX - RND_MIN);
        c[i] = g(a[i], b[i]);

        sb.append(String.format(Locale.ROOT,
          "%8.3f | %4d | %8.3f\n", a[i], b[i], c[i]));
      } catch (ArrayIndexOutOfBoundsException e) {
        sb.append(e).append("\n");
        break;
      } catch (ArithmeticException e) {
        sb.append(e).append("\n");
      }
    }

    return sb.toString();
  }

  private static double f(double x) {
    if (x <= 1.0) throw new ArithmeticException("A must be > 0");
    return Math.log(x * x - 1);
  }

  private static double g(double a, double b) {
    if (b == 0.0) throw new ArithmeticException("B must be != 0");
    return a - 1 / b;
  }
}
