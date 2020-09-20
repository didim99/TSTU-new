package ru.didim99.tstu.core.security.cipher.decryptor;

/**
 * Created by didim99 on 20.09.20.
 */

public class RSADecryptor implements Decryptor {

  @Override
  public void configure(String[] params) {

  }

  @Override
  public String decrypt(String data) {
    return data;
  }
}
