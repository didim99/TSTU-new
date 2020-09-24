package ru.didim99.tstu.core.security.cipher.decryptor;

import android.content.Context;

/**
 * Created by didim99 on 20.09.20.
 */

public abstract class Decryptor {
  protected static final String ALPHABET_RUS = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ .,";

  protected void checkArgumentsCount(String[] args, int expected) {
    if (args == null)
      throw new IllegalArgumentException("no arguments passed");
    if (args.length < expected)
      throw new IllegalArgumentException("too few arguments passed");
    if (args.length > expected)
      throw new IllegalArgumentException("too many arguments passed");
  }

  protected void checkEmptyArguments(String[] args) {
    for (String arg : args) {
      if (arg == null || arg.isEmpty())
        throw new IllegalArgumentException("argument is empty");
    }
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
