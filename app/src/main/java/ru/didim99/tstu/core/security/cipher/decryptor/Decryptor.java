package ru.didim99.tstu.core.security.cipher.decryptor;

import android.content.Context;

/**
 * Created by didim99 on 20.09.20.
 */

public abstract class Decryptor {
  protected static final String ALPHABET_RUS = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ .,";
  protected static final String ALPHABET_ENG = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  protected void checkArgumentsCount(String[] args, int expected) {
    if (args == null)
      throw new IllegalArgumentException("no arguments passed, expected: " + expected);
    if (args.length < expected)
      throw new IllegalArgumentException("too few arguments passed, expected: " + expected);
    if (args.length > expected)
      throw new IllegalArgumentException("too many arguments passed, expected: " + expected);
  }

  protected void checkEmptyArguments(String[] args) {
    for (String arg : args) {
      if (arg == null || arg.isEmpty())
        throw new IllegalArgumentException("argument is empty");
    }
  }

  protected void checkArgumentLength(String data, int min, int max) {
    if (data.length() < min)
      throw new IllegalArgumentException("argument too short");
    if (data.length() > max)
      throw new IllegalArgumentException("argument too long");
  }

  protected void checkAlphabet(String data, String alphabet) {
    for (int i = 0; i < data.length(); i++) {
      if (alphabet.indexOf(data.charAt(i)) == -1)
        throw new IllegalArgumentException("Invalid character at pos: " + i);
    }
  }

  public abstract void configure(String[] params);
  public abstract String getDescription(Context context);
  public abstract String decrypt(String data);
  public abstract boolean isConfigured();
}
