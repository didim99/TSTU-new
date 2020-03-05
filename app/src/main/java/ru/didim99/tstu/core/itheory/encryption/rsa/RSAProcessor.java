package ru.didim99.tstu.core.itheory.encryption.rsa;

/**
 * Created by didim99 on 05.03.20.
 */
public class RSAProcessor {
  private RSAKey key;

  public RSAProcessor(RSAKey key) {
    this.key = key;
  }

  public byte[] encrypt(String message) {
    return new byte[0];
  }

  public String decrypt(byte[] data) {
    return null;
  }
}
