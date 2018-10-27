package ru.didim99.tstu.core.numeric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 25.10.18.
 */
public class Interpolator {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_interpolator";
  private static final String DELIMITER = "\\s+";

  private Config config;
  private Result result;
  private double[] xSrc, ySrc, xRes, yRes;
  private double[][] delta;
  private int n, m;

  Interpolator(Config config) {
    this.config = config;
    this.result = new Result(config);
  }

  Result interpolate() {
    try {
      initValues(config.getFileName());
      MyLog.d(LOG_TAG, String.format(Locale.US,
        "Parsed initial values: n=%d, m=%d", n, m));
    } catch (IOException e) {
      MyLog.e(LOG_TAG, "Can't open input file: " + e);
      return result;
    }

    calcDelta();



    return result;
  }

  private void calcDelta() {
    delta = new double[n][n];

    for (int i = 0; i < n-1; i++)
      delta[i][0] = (ySrc[i+1] - ySrc[i]) / (xSrc[i+1] - xSrc[i]);

    for (int j = 1; j < n; j++) {
      for (int i = 0; i < n - j; i++) {
        delta[i][j] = delta(i, j);
      }
    }
  }

  private double delta(int i, int k) {
    return (delta[i+1][k-1] - delta[i][k-1]) / (xSrc[i+k] - xSrc[i]);
  }

  private void initValues(String fileName) throws IOException {
    ArrayList<String> data = Utils.readFile(fileName);
    if (data.size() < 3)
      throw new IOException("Incorrect input file format");
    xSrc = Utils.stringArrayToDoubleArray(data.get(0).split(DELIMITER));
    ySrc = Utils.stringArrayToDoubleArray(data.get(1).split(DELIMITER));
    xRes = Utils.stringArrayToDoubleArray(data.get(2).split(DELIMITER));
    if (xSrc == null || ySrc == null || xRes == null)
      throw new IOException("Incorrect input file format");
    n = xSrc.length;
    m = xRes.length;
    yRes = new double[n];
  }
}
