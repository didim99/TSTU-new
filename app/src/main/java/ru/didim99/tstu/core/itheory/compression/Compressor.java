package ru.didim99.tstu.core.itheory.compression;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import ru.didim99.tstu.core.itheory.compression.utils.BaseCharTable;
import ru.didim99.tstu.utils.Timer;

/**
 * Created by didim99 on 28.02.20.
 */
public abstract class Compressor {
  static final String EXT_SEP = ".";
  public static final String EXT_UNC = ".txt";
  public static final String EXT_COMP = ".dat";

  static final class Type {
    static final int HUFFMAN    = 0;
    static final int ARITHMETIC = 1;
    static final int LZW        = 2;
  }

  String compressed, info;

  public abstract byte[] compress(String data) throws IOException;
  public abstract String decompress(byte[] data) throws IOException;

  public String getCompressed() {
    return compressed;
  }

  public String getInfo() {
    return info;
  }

  void clearBuffers() {
    compressed = null;
    info = null;
  }

  Map<Character, Integer> getFrequency(String data) {
    Map<Character, Integer> frequency = new HashMap<>();
    for (char c : data.toCharArray()) {
      Integer f = frequency.get(c);
      if (f == null) f = 0;
      frequency.put(c, f + 1);
    }

    return frequency;
  }

  public void test(String message) throws IOException {
    Timer timer = new Timer();
    timer.start();
    byte[] original = message.getBytes();
    byte[] compressed = compress(message);
    byte[] decompressed = decompress(compressed).getBytes();
    boolean passed = Arrays.equals(original, decompressed);
    timer.stop();

    info = String.format(Locale.US,
      "Message length: %d characters\n", message.length()) +
      String.format(Locale.US,
        "Original size: %d bytes\n", original.length) +
      String.format(Locale.US,
        "Compressed size: %d bytes\n", compressed.length) +
      String.format(Locale.US,
        "Compression factor: %.1f%%\n",
        percent(original.length, compressed.length)) +
      "Execution time: " + timer.getStr() + " sec\n" +
      "\nTest status: " + (passed ? "PASSED" : "FAILED");
  }

  static void describe(StringBuilder sb, String message, int compSize,
                       int compSizeTree, BaseCharTable table) {
    int origSize = message.getBytes(Charset.defaultCharset()).length;
    sb.append(String.format(Locale.US,
      "Message length: %d characters\n", message.length()));
    sb.append(String.format(Locale.US,
      "Original size: %d bytes\n", origSize));
    sb.append(String.format(Locale.US,
      "Metadata size: %d bytes\n", compSizeTree - compSize - 4));
    sb.append(String.format(Locale.US,
      "Compressed size (w/o metadata): %d bytes\n", compSize));
    sb.append(String.format(Locale.US,
      "Compressed size (w metadata): %d bytes\n", compSizeTree));
    sb.append(String.format(Locale.US,
      "Compression factor (w/o metadata): %.1f%%\n", percent(origSize, compSize)));
    sb.append(String.format(Locale.US,
      "Compression factor (w metadata): %.1f%%\n", percent(origSize, compSizeTree)));
    sb.append("\nCharacter code table\n");
    sb.append(table.describe());
  }

  static double percent(int total, int compressed) {
    return getRatio(total, compressed) * 100.0;
  }

  static double getRatio(int total, int compressed) {
    return (total - compressed) / (double) total;
  }
}
