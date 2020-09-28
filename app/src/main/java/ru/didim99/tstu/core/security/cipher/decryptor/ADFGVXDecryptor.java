package ru.didim99.tstu.core.security.cipher.decryptor;

import android.content.Context;
import java.util.Arrays;
import ru.didim99.tstu.R;

/**
 * Created by didim99 on 20.09.20.
 */

public class ADFGVXDecryptor extends Decryptor {
  private static final String ALPHABET = ALPHABET_ENG;
  private static final String COMPONENT = "ADFGVX";
  private static final String DELIMITER = ":";

  private String table, keyword;
  private int[] swapTable;

  @Override
  public void configure(String[] params) {
    checkArgumentsCount(params, 2);
    checkEmptyArguments(params);
    String table = params[0].toUpperCase();
    checkArgumentLength(table, ALPHABET.length());
    checkAlphabet(table, ALPHABET);
    this.table = table;
    String keyword = params[1].toUpperCase();
    checkAlphabet(keyword, ALPHABET);
    this.keyword = keyword;
    calculateSwapTable();
  }

  @Override
  public String getSampleConfig() {
    return "<table:str> <keyword:str>";
  }

  @Override
  public String getDescription(Context context) {
    if (!isConfigured()) return null;
    StringBuilder sb = new StringBuilder();
    int size = COMPONENT.length();

    sb.append(context.getString(
      R.string.is_cipher_keyword, keyword)).append("\n");

    sb.append("\n");
    sb.append("   |").append(COMPONENT).append("\n");
    sb.append(" --|------").append("\n");
    for (int y = 0; y < size; y++) {
      sb.append(" ").append(COMPONENT.charAt(y)).append(" |");
      for (int x = 0; x < size; x++)
        sb.append(table.charAt(y * size + x));
      sb.append("\n");
    }

    return sb.toString();
  }

  @Override
  public String decrypt(String data) {
    int length = data.length();

    String[] params = data.split(DELIMITER);
    if (params.length > 1) {
      length = Integer.parseInt(params[0]) * 2;
      data = params[1];
    }

    if (data.length() % keyword.length() > 0)
      throw new IllegalArgumentException("input data length not match keyword length");
    data = data.toUpperCase();
    checkAlphabet(data, COMPONENT);

    int segmentSize = data.length() / keyword.length();
    char[][] buffer = new char[keyword.length()][segmentSize];
    for (int i = 0; i < keyword.length(); i++) {
      int offset = segmentSize * swapTable[i];
      buffer[i] = data.substring(offset, offset + segmentSize).toCharArray();
    }

    StringBuilder sb = new StringBuilder();
    for (int c = 0; c < segmentSize; c++) {
      for (int r = 0; r < keyword.length(); r++) {
        sb.append(buffer[r][c]);
      }
    }

    StringBuilder res = new StringBuilder();
    for (int pos = 0; pos < length; pos += 2) {
      int x = COMPONENT.indexOf(sb.charAt(pos));
      int y = COMPONENT.indexOf(sb.charAt(pos + 1));
      res.append(table.charAt(x * 6 + y));
    }

    return res.toString();
  }

  @Override
  public boolean isConfigured() {
    return this.table != null && !this.table.isEmpty()
      && this.keyword != null && !this.keyword.isEmpty();
  }

  private void calculateSwapTable() {
    swapTable = new int[keyword.length()];
    boolean[] empty = new boolean[keyword.length()];
    char[] buffer = keyword.toCharArray();
    char[] sorted = keyword.toCharArray();
    Arrays.fill(empty, true);
    Arrays.sort(sorted);

    for (int i = 0; i < buffer.length; i++) {
      for (int s = 0; s < sorted.length; s++) {
        if (buffer[i] == sorted[s] && empty[s]) {
          swapTable[i] = s;
          empty[s] = false;
          break;
        }
      }
    }
  }
}
