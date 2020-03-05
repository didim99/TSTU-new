package ru.didim99.tstu.core.itheory.encryption.rsa;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Created by didim99 on 05.03.20.
 */
public class RSAKey {
  private static final int MIN_LEN = 8;
  private static final int MAX_LEN = 64;
  private static final int LEN_STEP = 8;

  private BigInteger e, d, n;

  public RSAKey(BigInteger e, BigInteger d, BigInteger n) {
    this.e = e;
    this.d = d;
    this.n = n;
  }

  public RSAKey(byte[] rawData) {

  }

  public BigInteger getE() {
    return e;
  }

  public BigInteger getD() {
    return d;
  }

  public BigInteger getN() {
    return n;
  }

  public byte[] serialize() {
    return new byte[0];
  }

  public static RSAKey generate(int length) {
    return null;
  }

  public static String[] getPossibleLength() {
    ArrayList<String> list = new ArrayList<>();
    for (int len = MIN_LEN; len <= MAX_LEN; len += LEN_STEP)
      list.add(String.valueOf(len));
    return list.toArray(new String[0]);
  }
}
