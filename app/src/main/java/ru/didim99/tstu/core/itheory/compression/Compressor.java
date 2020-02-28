package ru.didim99.tstu.core.itheory.compression;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by didim99 on 28.02.20.
 */
public abstract class Compressor {
  public static final String EXT_SEP = ".";
  public static final String EXT_UNC = ".txt";
  public static final String EXT_COMP = ".dat";

  static final class Type {
    static final int HUFFMAN = 0;
    static final int ARITHMETIC = 1;
  }

  String compressed, info;

  public String getCompressed() {
    return compressed;
  }

  public String getInfo() {
    return info;
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

  public abstract byte[] compress(String data);
  public abstract String decompress(byte[] data);
}
