package ru.didim99.tstu.core.security.cipher.decryptor;

import android.content.Context;

import java.util.Arrays;

import ru.didim99.tstu.R;

/**
 * Created by didim99 on 20.09.20.
 */

public class ADFGVXDecryptor extends Decryptor {
  private static final String ALPHABET = ALPHABET_ENG;

  private String table, keyword;

  @Override
  public void configure(String[] params) {
    checkArgumentsCount(params, 2);
    checkEmptyArguments(params);
    String table = params[0];
    checkArgumentLength(table, ALPHABET.length(), ALPHABET.length());
    checkAlphabet(table, ALPHABET);
    this.table = table;
    String keyword = params[1];
    checkAlphabet(keyword, ALPHABET);
    this.keyword = keyword;
  }

  @Override
  public String getDescription(Context context) {
    if (!isConfigured()) return null;
    StringBuilder sb = new StringBuilder();
    String name = "ADFGVX";
    int size = name.length();

    sb.append(context.getString(
      R.string.is_cipher_keyword, keyword)).append("\n");

    sb.append("\n");
    sb.append("   |").append(name).append("\n");
    sb.append(" --|------").append("\n");
    for (int y = 0; y < size; y++) {
      sb.append(" ").append(name.charAt(y)).append(" |");
      for (int x = 0; x < size; x++)
        sb.append(table.charAt(y * size + x));
      sb.append("\n");
    }

    return sb.toString();
  }

  @Override
  public String decrypt(String data) {
    if (data.length() % keyword.length() > 0)
      throw new IllegalArgumentException("input data length not match keyword length");

    int segmentSize = data.length() / keyword.length();
    StringBuilder buffer = new StringBuilder();

    char[] header = keyword.toCharArray();
    Arrays.sort(header);
    String tmpKeyword = keyword;

    String[] tmpTable = new String[keyword.length()];
    for (char c : tmpKeyword.toCharArray()) {
      int pos = tmpKeyword.indexOf(c);
    }

    return data;
  }

  @Override
  public boolean isConfigured() {
    return this.table != null && !this.table.isEmpty()
      && this.keyword != null && !this.keyword.isEmpty();
  }
}
