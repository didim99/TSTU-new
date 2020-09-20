package ru.didim99.tstu.core.security.cipher.decryptor;

/**
 * Created by didim99 on 20.09.20.
 */

public interface Decryptor {
  void configure(String[] params);
  String decrypt(String data);
}
