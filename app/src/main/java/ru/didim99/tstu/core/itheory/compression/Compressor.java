package ru.didim99.tstu.core.itheory.compression;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by didim99 on 28.02.20.
 */
public abstract class Compressor {

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
