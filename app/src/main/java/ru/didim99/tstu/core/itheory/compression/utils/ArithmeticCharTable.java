package ru.didim99.tstu.core.itheory.compression.utils;

import android.support.annotation.NonNull;
import java.io.DataInput;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Created by didim99 on 01.03.20.
 */
public class ArithmeticCharTable extends BaseCharTable<ArithmeticCharTable.Entry> {
  public static final BigDecimal DEFAULT_MIN = BigDecimal.valueOf(0.0);
  public static final BigDecimal DEFAULT_MAX = BigDecimal.valueOf(1.0);
  private static final String TABLE_HEADER = "  Min     Max";

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

  public BigDecimal getMinValue(char c) {
    return table.get(c).minValue;
  }

  public BigDecimal getMaxValue(char c) {
    return table.get(c).maxValue;
  }

  public BigDecimal getInterval(char c) {
    return table.get(c).getInterval();
  }

  public Character getCharacter(BigDecimal code) {
    for (Entry entry : entryList) {
      if (entry.checkNumber(code))
        return entry.character;
    }

    return null;
  }

  private void buildTable(Map<Character, Integer> frequencyMap) {
    double prevMax = DEFAULT_MIN.doubleValue();
    int weightSum = 0;

    entryList = new ArrayList<>();
    for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
      Entry newEntry = new Entry(entry.getKey(), entry.getValue());
      table.put(entry.getKey(), newEntry);
      weightSum += entry.getValue();
      entryList.add(newEntry);
    }

    Collections.sort(entryList);
    for (Entry entry : entryList)
      prevMax = entry.computeRange(weightSum, prevMax);
  }

  static class Entry extends BaseCharTable.Entry
    implements Comparable<Entry> {
    private BigDecimal minValue;
    private BigDecimal maxValue;

    private Entry(char character, int frequency) {
      super(character, frequency);
    }

    private double computeRange(int sum, double prevMax) {
      double nextMax = prevMax + ((double) frequency) / sum;
      this.minValue = BigDecimal.valueOf(prevMax);
      this.maxValue = BigDecimal.valueOf(nextMax);
      return nextMax;
    }

    private boolean checkNumber(BigDecimal number) {
      return number.compareTo(minValue) > 0
        && number.compareTo(maxValue) < 0;
    }

    private BigDecimal getInterval() {
      return maxValue.subtract(minValue);
    }

    @Override
    public int compareTo(@NonNull Entry o) {
      return Character.compare(character, o.character);
    }

    @Override
    public String toString() {
      return String.format(Locale.US, "%s  %6.4f  %6.4f",
        super.toString(), minValue, maxValue);
    }
  }
}
