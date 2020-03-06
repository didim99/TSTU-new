package ru.didim99.tstu.core.itheory.encryption.math;

import java.util.Random;

/**
 * Fast integer mathematical operations for cryptography
 * Created by didim99 on 05.03.20.
 */
public class FastMath {
  private static final int FERMAT_ITERATIONS = 100;
  private static final Random random = new Random();

  /**
   * Checks {@code number} for primality using Fermat's test
   */
  public static boolean isPrime(long n) {
    if (n == 2) return true;
    for (int i = 0; i < FERMAT_ITERATIONS; i++) {
      long a = (random.nextInt() % (n - 2)) + 2;
      if (gcd(a, n) != 1) return false;
      if (powm(a, n - 1, n) != 1) return false;
    }

    return true;
  }

  /**
   * Calculate multiplicative inverse number for {@code a} by module {@code m}
   * using extended Euclidean algorithm
   */
  public static long inversem(long a, long m) {
    if (a == 0) return 1;
    long x1 = 0, x2 = 1;
    long y1 = 1, y2 = 0;
    long q, r, x, y;
    long m0 = m;

    while (a > 0) {
      q = m / a;
      r = m - a*q;
      x = x2 - q * x1;
      y = y2 - q * y1;
      m = a;
      a = r;
      x2 = x1;
      y2 = y1;
      x1 = x;
      y1 = y;
    }

    if (y2 < 0) y2 += m0;
    return y2;
  }

  /**
   * Calculate a greatest common divisor of {@code a} and {@code b}
   * using Euclidean algorithm
   */
  private static long gcd(long a, long b) {
    if (b == 0) return a;
    return gcd(b, a % b);
  }

  /**
   * Fast exponentiation modulo
   * @return {@code a^b mod m}
   */
  public static long powm(long a, long b, long m) {
    if (b == 0) return 1;
    if (b % 2 == 0) {
      long t = powm(a, b / 2, m);
      return mulm(t, t, m) % m;
    } else
      return mulm(powm(a, b - 1, m), a, m) % m;
  }

  /**
   * Fast multiplication modulo
   * @return {@code a*b mod m}
   */
  private static long mulm(long a, long b, long m) {
    if (b == 1) return a;
    if (b % 2 == 0) {
      long t = mulm(a, b / 2, m);
      return (2 * t) % m;
    } else
      return (mulm(a, b-1, m) + a) % m;
  }
}
