package ru.didim99.tstu.core.security.cipher.decryptor;

import android.content.Context;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 20.09.20.
 */

public class RotaryGridDecryptor extends Decryptor {

  private int width, height;
  private boolean[][] grid;
  private int[] sequence;

  @Override
  public void configure(String[] params) {
    checkArgumentsCount(params, 4);
    checkEmptyArguments(params);
    width = getIntegerArgument(params, 0);
    height = getIntegerArgument(params, 1);
    String gridStr = params[2];
    checkArgumentLength(gridStr, width * height);
    checkAlphabet(gridStr, "01");
    grid = parseGridString(gridStr);
    String sequenceStr = params[3];
    checkArgumentLength(sequenceStr, 4);
    checkAlphabet(sequenceStr, "1234");
    sequence = parseSequenceString(sequenceStr);
  }

  @Override
  public String getDescription(Context context) {
    if (!isConfigured()) return null;
    StringBuilder sb = new StringBuilder();

    String sequenceStr = Utils.joinStr(", ",
      Utils.intArrayToStringArray(sequence));
    sb.append(context.getString(
      R.string.is_cipher_sequence, sequenceStr)).append("\n");

    sb.append("\n");
    for (int y = 0; y < height; y++) {
      sb.append("\n ");
      for (int x = 0; x < width; x++)
        sb.append(grid[x][y] ? "0" : "1");
    }

    return sb.toString();
  }

  @Override
  public String decrypt(String data) {
    return data;
  }

  @Override
  public boolean isConfigured() {
    return width > 0 && height > 0
      && grid != null && sequence != null;
  }

  private boolean[][] parseGridString(String gridStr) {
    boolean[][] grid = new boolean[height][width];
    char[] buffer = gridStr.toCharArray();
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++)
        grid[x][y] = buffer[y * width + x] == '1';
    }

    return grid;
  }

  private int[] parseSequenceString(String sequenceStr) {
    int[] sequence = new int[sequenceStr.length()];
    for (int i = 0; i < sequence.length; i++)
      sequence[i] = Integer.parseInt(sequenceStr.substring(i, i+1));
    return sequence;
  }
}
