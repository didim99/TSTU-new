package ru.didim99.tstu.core.security.cipher.decryptor;

import android.content.Context;
import java.util.Arrays;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 20.09.20.
 */

public class RotaryGridDecryptor extends Decryptor {
  private static final String DELIMITER = ":";

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
  public String getSampleConfig() {
    return "<width:int> <height:int> <grid:str> <positions:str>";
  }

  @Override
  public String getDescription(Context context) {
    if (!isConfigured()) return null;
    StringBuilder sb = new StringBuilder();

    String sequenceStr = Utils.joinStr(", ",
      Utils.intArrayToStringArray(sequence));
    sb.append(context.getString(
      R.string.is_cipher_sequence, sequenceStr)).append("\n");
    sb.append("\n").append(serializeGrid(grid));

    return sb.toString();
  }

  @Override
  public String decrypt(String data) {
    int length = data.length();

    String[] params = data.split(DELIMITER);
    if (params.length > 1) {
      length = Integer.parseInt(params[0]);
      data = params[1];
    }

    StringBuilder sb = new StringBuilder();
    for (int position : sequence) {
      boolean[][] tmgGrid = rotateGrid(position);
      sb.append(fillGrid(tmgGrid, data));
    }

    return sb.substring(0, length);
  }

  private CharSequence fillGrid(boolean[][] grid, String src) {
    StringBuilder sb = new StringBuilder();

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (grid[y][x]) sb.append(src.charAt(y * width + x));
      }
    }

    return sb;
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
        grid[y][x] = buffer[y * width + x] == '1';
    }

    return grid;
  }

  private int[] parseSequenceString(String sequenceStr) {
    int[] sequence = new int[sequenceStr.length()];
    for (int i = 0; i < sequence.length; i++)
      sequence[i] = Integer.parseInt(sequenceStr.substring(i, i+1));
    return sequence;
  }

  private boolean[][] rotateGrid(int position) {
    boolean[][] res = deepCopy(grid);

    switch (position) {
      case 1:
        break;
      case 2:
        flipHorizontal(res);
        flipVertical(res);
        break;
      case 3:
        flipHorizontal(res);
        break;
      case 4:
        flipVertical(res);
        break;
      default:
        throw new IllegalArgumentException("Unknown position: " + position);
    }

    return res;
  }

  private void flipHorizontal(boolean[][] grid) {
    int width = grid[0].length;
    for (int y = 0; y < grid.length; y++) {
      for (int x = 0; x < width / 2; x++) {
        int swapX = width - x - 1;
        boolean tmp = grid[y][x];
        grid[y][x] = grid[y][swapX];
        grid[y][swapX] = tmp;
      }
    }
  }

  private void flipVertical(boolean[][] grid) {
    int height = grid.length;
    for (int y = 0; y < height / 2; y++) {
      for (int x = 0; x < grid[0].length; x++) {
        int swapY = height - y - 1;
        boolean tmp = grid[y][x];
        grid[y][x] = grid[swapY][x];
        grid[swapY][x] = tmp;
      }
    }
  }

  private boolean[][] deepCopy(boolean[][] src) {
    if (src == null)
      return null;
    final boolean[][] result = new boolean[src.length][];
    for (int i = 0; i < src.length; i++)
      result[i] = Arrays.copyOf(src[i], src[i].length);
    return result;
  }

  private String serializeGrid(boolean[][] grid) {
    StringBuilder sb = new StringBuilder();
    for (int y = 0; y < height; y++) {
      sb.append(" ");
      for (int x = 0; x < width; x++)
        sb.append(grid[y][x] ? "1" : "0");
      if (y < height - 1)
        sb.append("\n");
    }
    return sb.toString();
  }
}
