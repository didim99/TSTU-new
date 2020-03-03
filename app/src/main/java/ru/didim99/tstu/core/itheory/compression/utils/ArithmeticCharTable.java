package ru.didim99.tstu.core.itheory.compression.utils;

import android.support.annotation.NonNull;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import ru.didim99.tstu.core.itheory.compression.ArithmeticCompressor;

/**
 * Created by didim99 on 01.03.20.
 */
public class ArithmeticCharTable extends BaseCharTable<ArithmeticCharTable.Entry> {
  private static final String TABLE_HEADER = "  Min     Max";

  private long maxFrequency;

  public ArithmeticCharTable(Map<Character, Integer> frequencyMap) {
    super();
    buildTable(frequencyMap);
  }

  public ArithmeticCharTable(DataInput source) throws IOException {
    this(readFreqMap(source));
  }

  @Override
  String getHeader() {
    return super.getHeader() + TABLE_HEADER;
  }

  public long getMaxFrequency() {
    return maxFrequency;
  }

  public long getMinValue(char c) {
    return table.get(c).minValue;
  }

  public long getMaxValue(char c) {
    return table.get(c).maxValue;
  }

  public Character getCharacter(long code) {
    for (Entry entry : entryList) {
      if (entry.checkNumber(code))
        return entry.character;
    }

    return null;
  }

  private void buildTable(Map<Character, Integer> frequencyMap) {
    entryList = new ArrayList<>();
    for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
      Entry newEntry = new Entry(entry.getKey(), entry.getValue());
      table.put(entry.getKey(), newEntry);
      entryList.add(newEntry);
    }

    long prevMax = 0;
    Collections.sort(entryList);
    for (Entry entry : entryList)
      prevMax = entry.computeRange(prevMax);
    if (prevMax >= ArithmeticCompressor.MAX_FREQ) {
      prevMax = 0;
      for (Entry entry : entryList) {
        entry.frequency = (entry.frequency + 1) / 2;
        prevMax = entry.computeRange(prevMax);
      }
    }

    maxFrequency = prevMax;
  }

  static class Entry extends BaseCharTable.Entry
    implements Comparable<Entry> {
    private long minValue;
    private long maxValue;

    private Entry(char character, int frequency) {
      super(character, frequency);
    }

    private long computeRange(long prevMax) {
      maxValue = prevMax + frequency;
      minValue = prevMax;
      return maxValue;
    }

    private boolean checkNumber(long number) {
      return number >= minValue && number < maxValue;
    }

    @Override
    public int compareTo(@NonNull Entry o) {
      int c = Integer.compare(o.frequency, frequency);
      if (c == 0) c = Character.compare(character, o.character);
      return c;
    }

    @Override
    public String toString() {
      return String.format(Locale.US, "%s  %-6d  %-6d",
        super.toString(), minValue, maxValue);
    }
  }
}
