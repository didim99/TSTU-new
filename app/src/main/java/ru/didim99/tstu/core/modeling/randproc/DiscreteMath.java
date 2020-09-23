package ru.didim99.tstu.core.modeling.randproc;

/**
 * Created by didim99 on 08.05.20.
 */
public class DiscreteMath {
  public static double mean(Double... sequence) {
    double expected = 0;
    for (double v : sequence) expected += v;
    return expected / sequence.length;
  }

  public static double spread(Double... sequence) {
    double mean = mean(sequence), spread = 0;
    for (double v : sequence)
      spread += Math.pow(v - mean, 2);
    return spread / sequence.length;
  }

  public static double correlate(int s, Double... sequence) {
    int size = sequence.length - s;
    double mean = mean(sequence);
    double res = 0;

    for (int i = 0; i < size; i++)
      res += (sequence[i] - mean) * (sequence[i+s] - mean);
    return res / size;
  }
}
