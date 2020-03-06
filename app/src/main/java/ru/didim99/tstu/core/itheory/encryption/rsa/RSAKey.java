package ru.didim99.tstu.core.itheory.encryption.rsa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import ru.didim99.tstu.core.itheory.encryption.math.FastMath;
import ru.didim99.tstu.core.itheory.encryption.math.SieveEratosthenes;

/**
 * Created by didim99 on 05.03.20.
 */
public class RSAKey {
  private static final byte[] HEADER = {0x52, 0x53, 0x41};
  private static final long[] PUBLIC_EXP = {17, 257, 65537};
  private static final int MIN_LEN = 4;
  private static final int MAX_LEN = 16;
  private static final int LEN_STEP = 4;

  private BigInteger e, d, n;

  public RSAKey(BigInteger e, BigInteger d, BigInteger n) {
    this.e = e;
    this.d = d;
    this.n = n;
  }

  public RSAKey(byte[] rawData) throws IOException {
    deserialize(rawData);
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

  private void deserialize(byte[] data) throws IOException {
    ByteArrayInputStream buffer = new ByteArrayInputStream(data);
    DataInputStream stream = new DataInputStream(buffer);

    byte[] header = new byte[HEADER.length];
    if (buffer.read(header) < HEADER.length)
      throw new IOException("Unexpected end of file");
    if (!Arrays.equals(header, HEADER))
      throw new IOException("Invalid file header");

    e = readComponent(stream);
    d = readComponent(stream);
    n = readComponent(stream);
  }

  public byte[] serialize() throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DataOutputStream stream = new DataOutputStream(buffer);
    buffer.write(HEADER);
    writeComponent(stream, e);
    writeComponent(stream, d);
    writeComponent(stream, n);
    return buffer.toByteArray();
  }

  private BigInteger readComponent(DataInput input)
    throws IOException {
    int length = input.readInt();
    byte[] buffer = new byte[length];
    input.readFully(buffer);
    return new BigInteger(buffer);
  }

  private void writeComponent(DataOutput output, BigInteger component)
    throws IOException {
    byte[] data = component.toByteArray();
    output.writeInt(data.length);
    output.write(data);
  }

  public static RSAKey generate(int length) {
    if (length == MAX_LEN) length--;
    long minValue = 1L << (length - 2);
    long maxValue = 1L << length;

    Random random = new Random();
    SieveEratosthenes sieve = new SieveEratosthenes();
    sieve.fillPrimesUpTo(maxValue);
    int size = sieve.getSize();
    long p = 0, q = 0;

    while (p < minValue)
      p = sieve.get(random.nextInt(size));
    while (q < minValue)
      q = sieve.get(random.nextInt(size));

    long n = p * q, e = 1;
    long phi = (p - 1) * (q - 1);
    for (long exp : PUBLIC_EXP) {
      if (exp < phi) e = exp;
    }

    long d = FastMath.inversem(e, phi);
    return new RSAKey(
      BigInteger.valueOf(e),
      BigInteger.valueOf(d),
      BigInteger.valueOf(n));
  }

  public static String[] getPossibleLength() {
    ArrayList<String> list = new ArrayList<>();
    for (int len = MIN_LEN; len <= MAX_LEN; len += LEN_STEP)
      list.add(String.valueOf(len));
    return list.toArray(new String[0]);
  }
}
