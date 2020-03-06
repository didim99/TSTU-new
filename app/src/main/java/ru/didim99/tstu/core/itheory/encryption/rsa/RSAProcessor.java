package ru.didim99.tstu.core.itheory.encryption.rsa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import ru.didim99.tstu.core.itheory.encryption.math.FastMath;

/**
 * Created by didim99 on 05.03.20.
 */
public class RSAProcessor {
  private RSAKey key;

  public RSAProcessor(RSAKey key) {
    this.key = key;
  }

  public byte[] encrypt(String message) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DataOutputStream stream = new DataOutputStream(buffer);

    stream.writeInt(message.length());
    long e = key.getE().longValue();
    long n = key.getN().longValue();
    for (char character : message.toCharArray())
      stream.writeLong(FastMath.powm(character, e, n));

    return buffer.toByteArray();
  }

  public String decrypt(byte[] data) throws IOException {
    ByteArrayInputStream inBuffer = new ByteArrayInputStream(data);
    DataInputStream stream = new DataInputStream(inBuffer);
    StringBuilder sb = new StringBuilder();

    int length = stream.readInt();
    long d = key.getD().longValue();
    long n = key.getN().longValue();
    while (length-- > 0)
      sb.append((char) FastMath.powm(stream.readLong(), d, n));

    return sb.toString();
  }
}
