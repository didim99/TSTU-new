package ru.didim99.tstu.core.math.analysis;

import android.content.Context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 01.12.18.
 */
public class RVProcessor {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_RVP";
  public  static final String DEFAULT_TITLE = "X / Y: ";
  private static final String LINE_REGEX = "\n";
  private static final String TITLE_SEP = ":";
  private static final String NAME_SEP = "/";
  private static final String NAME_REGEX = "[A-Za-z]";
  private static final String TITLE_REGEX =
    "^" + NAME_REGEX + "\\s*" + NAME_SEP + "\\s*" + NAME_REGEX + "$";
  private static final String TSEP_REGEX = "\\s*" + TITLE_SEP + "\\s*";
  private static final String NSEP_REGEX = "\\s*" + NAME_SEP + "\\s*";
  private static final String INTERVAL_SEP = "..";
  private static final String INTERVAL_REGEX = "\\.\\.";
  private static final String VALUE_REGEX = "\\s+";
  private static final double EPS = 10E-6;

  public static final class ErrorCode {
    public static final int EMPTY_INPUT = 1;
    public static final int INCORRECT_FORMAT = 2;
    public static final int TITLE_NOT_DEFINED = 3;
    public static final int TITLE_INCORRECT = 4;
    public static final int INCORRECT_X = 5;
    public static final int INCORRECT_Y = 6;
    public static final int INCORRECT_F = 7;
    public static final int SUM_OUT_OF_RANGE = 8;
  }

  // state
  private int errorCode;
  private int brokenX, brokenY;
  // workflow
  private String xName, yName;
  private Double[] xVals, yVals;
  private ArrayList<ArrayList<Double>> fVals;
  private Double[] xfVals, yfVals;
  private RVParams xParams, yParams;
  private double xyAvg, k, r;
  private double Axy, Bxy, Ayx, Byx;

  public RVProcessor() {}

  public void compute(String inputStr)
    throws ProcessException  {
    if (inputStr.isEmpty())
      setError(ErrorCode.EMPTY_INPUT);

    parseInput(inputStr);
    initValues();
    MyLog.d(LOG_TAG, "Computing...");

    double sum = 0;
    for (ArrayList<Double> series : fVals)
      for (Double f : series) sum += f;
    if (Math.abs(sum - 1) > EPS)
      setError(ErrorCode.SUM_OUT_OF_RANGE);

    for (int x = 0; x < fVals.size(); x++) {
      ArrayList<Double> series = fVals.get(x);
      for (int y = 0; y < series.size(); y++) {
        xfVals[x] += series.get(y);
        yfVals[y] += series.get(y);
      }
    }

    xParams = calcRVParams(xVals, xfVals);
    yParams = calcRVParams(yVals, yfVals);

    for (int x = 0; x < xVals.length; x++) {
      for (int y = 0; y < yVals.length; y++)
        xyAvg += xVals[x] * yVals[y] * fVals.get(x).get(y);
    }

    k = xyAvg - xParams.avg * yParams.avg;
    r = k / (xParams.sigma * yParams.sigma);
    Bxy = r * yParams.sigma / xParams.sigma;
    Axy = yParams.avg - Bxy * xParams.avg;
    Byx = r * xParams.sigma / yParams.sigma;
    Ayx = xParams.avg - Byx * yParams.avg;
    MyLog.d(LOG_TAG, "Computing completed");
  }

  private RVParams calcRVParams(Double[] vals, Double[] fVals) {
    RVParams res = new RVParams();
    int n = vals.length;
    for (int i = 0; i < n; i++)
      res.avg += vals[i] * fVals[i];
    for (int i = 0; i < n; i++)
      res.delta += Math.pow(vals[i] - res.avg, 2) * fVals[i];
    res.sigma = Math.sqrt(res.delta);
    return res;
  }

  private void initValues() {
    xfVals = new Double[xVals.length];
    yfVals = new Double[yVals.length];
    Arrays.fill(xfVals, 0d);
    Arrays.fill(yfVals, 0d);
    Axy = Bxy = Ayx = Byx = 0;
    xyAvg = k = r = 0;
  }

  private void parseInput(String inputStr)
    throws ProcessException  {
    MyLog.d(LOG_TAG, "Parsing values...");
    String[] lines = inputStr.split(LINE_REGEX);
    if (lines.length < 2)
      setError(ErrorCode.INCORRECT_FORMAT);
    ArrayList<Double> xVals = new ArrayList<>();
    ArrayList<Double> yVals = new ArrayList<>();
    boolean useIntervalsX, useIntervalsY;
    fVals = new ArrayList<>();

    // parsing header
    String header = lines[0].trim();
    if (!header.contains(TITLE_SEP))
      setError(ErrorCode.TITLE_NOT_DEFINED);
    String[] headers = header.split(TSEP_REGEX);
    if (headers.length != 2)
      setError(ErrorCode.INCORRECT_FORMAT);
    String title = headers[0].trim();
    if (!title.matches(TITLE_REGEX))
      setError(ErrorCode.TITLE_INCORRECT);
    String[] names = title.split(NSEP_REGEX);
    xName = names[0].trim();
    yName = names[1].trim();

    // parsing y values
    String yStr = headers[1].trim();
    useIntervalsY = yStr.contains(INTERVAL_SEP);
    String[] yValsStr = yStr.split(VALUE_REGEX);

    try {
      for (String yVal : yValsStr)
        parseValue(yVal, useIntervalsY, yVals);
    } catch (NumberFormatException e) {
      setError(ErrorCode.INCORRECT_Y, yVals.size());
    }

    // parsing x values
    useIntervalsX =  lines[1].contains(INTERVAL_SEP);
    for (int i = 1; i < lines.length; i++) {
      if (lines[i].isEmpty())
        continue;
      String[] vals = lines[i].trim().split(VALUE_REGEX);
      if (vals.length != yVals.size() + 1)
        setError(ErrorCode.INCORRECT_FORMAT);
      ArrayList<Double> fLine = new ArrayList<>();
      fVals.add(fLine);

      try {
        parseValue(vals[0], useIntervalsX, xVals);
      } catch (NumberFormatException e) {
        setError(ErrorCode.INCORRECT_X, i);
      }

      for (int j = 1; j < vals.length; j++) {
        try {
          double v = Double.parseDouble(vals[j]);
          if (v < 0 || v > 1)
            throw new NumberFormatException("Value out of range");
          fLine.add(v);
        } catch (NumberFormatException e) {
          setError(ErrorCode.INCORRECT_F, i, j);
        }
      }
    }

    this.xVals = xVals.toArray(new Double[0]);
    this.yVals = yVals.toArray(new Double[0]);
  }

  private void parseValue(String src, boolean useIntervals,
                          ArrayList<Double> target)
    throws NumberFormatException {
    if (useIntervals) {
      String[] bounds = src.split(INTERVAL_REGEX);
      if (bounds.length != 2)
        throw new NumberFormatException();
      double start = Double.parseDouble(bounds[0]);
      double end = Double.parseDouble(bounds[1]);
      target.add((start + end) / 2);
    } else
      target.add(Double.parseDouble(src.trim()));
  }

  public String getTextResult(Context ctx, boolean screenLarge) {
    StringBuilder sb = new StringBuilder();
    int maxlenX = getMaxLen(xVals);
    int maxlenY = getMaxLen(yVals);

    if (screenLarge) {
      printTable(sb, xName, maxlenX, xVals, xfVals);
      sb.append(String.format(Locale.US,
        "M[%s]: %.3f D[%s]: %.3f σ[%s]: %.3f\n\n",
        xName, xParams.avg, xName, xParams.delta, xName, xParams.sigma));
      printTable(sb, yName, maxlenY, yVals, yfVals);
      sb.append(String.format(Locale.US,
        "M[%s]: %.3f D[%s]: %.3f σ[%s]: %.3f\n",
        yName, yParams.avg, yName, yParams.delta, yName, yParams.sigma));
    } else {
      printColumn(sb, xName, maxlenX, xVals, xfVals);
      sb.append("\n");
      sb.append(String.format(Locale.US, "  M[%s] = %.3f\n", xName, xParams.avg));
      sb.append(String.format(Locale.US, "  D[%s] = %.3f\n", xName, xParams.delta));
      sb.append(String.format(Locale.US, "  σ[%s] = %.3f\n", xName, xParams.sigma));
      sb.append("\n");
      printColumn(sb, yName, maxlenY, yVals, yfVals);
      sb.append("\n");
      sb.append(String.format(Locale.US, "  M[%s] = %.3f\n", yName, yParams.avg));
      sb.append(String.format(Locale.US, "  D[%s] = %.3f\n", yName, yParams.delta));
      sb.append(String.format(Locale.US, "  σ[%s] = %.3f\n", yName, yParams.sigma));
    }

    sb.append("\n").append(ctx.getString(R.string.mathStat_res_general));
    if (screenLarge) {
      sb.append(String.format(Locale.US,
        "  M[XY]: %.3f, K: %.3f, R: %.3f\n", xyAvg, k, r));
    } else {
      sb.append(String.format(Locale.US, "  M[XY] = %.3f\n", xyAvg));
      sb.append(String.format(Locale.US, "  K     = %.3f\n", k));
      sb.append(String.format(Locale.US, "  R     = %.3f\n", r));
    }

    sb.append("\n").append(ctx.getString(
      R.string.rv_regressionEquation)).append("\n");
    sb.append(String.format(Locale.US, "  %s = %.4f * %s %s %.4f\n",
      yName, Bxy, xName, (Axy < 0 ? "-" : "+"), Math.abs(Axy)));
    sb.append(String.format(Locale.US, "  %s = %.4f * %s %s %.4f",
      xName, Byx, yName, (Ayx < 0 ? "-" : "+"), Math.abs(Ayx)));
    return sb.toString();
  }

  private void printTable(StringBuilder sb, String name, int maxLen,
                          Double[] first, Double[] second) {
    String format = "  %" + (maxLen + 4) + ".3f";
    sb.append(String.format(Locale.US, "%3s:", name));
    for (Double d : first)
      sb.append(String.format(Locale.US, format, d));
    sb.append("\n  p:");
    for (Double d : second)
      sb.append(String.format(Locale.US, format, d));
    sb.append("\n");
  }

  private void printColumn(StringBuilder sb, String name, int maxLen,
                           Double[] first, Double[] second) {
    sb.append(name).append(":\n");
    String format = "  %" + maxLen + ".3f: %.3f\n";
    for (int i = 0; i < first.length; i++)
      sb.append(String.format(Locale.US, format, first[i], second[i]));
  }

  private int getMaxLen(Double[] vals) {
    double min = vals[0], max = vals[0];
    for (double v : vals) {
      if (v > max) max = v;
      if (v < min) min = v;
    }

    if (min * max < 0) {
      int minLog = (int) Math.floor(Math.log10(
        Math.abs(min))) + (min > 0 ? 1 : 2);
      int maxLog = (int) Math.floor(Math.log10(
        Math.abs(max))) + (max > 0 ? 1 : 2);
      return Math.max(minLog, maxLog);
    } else {
      double val = max > 0 ? max : min;
      return (int) Math.floor(Math.log10(
        Math.abs(val))) + (val > 0 ? 1 : 2);
    }
  }

  public int getErrorCode() {
    return errorCode;
  }

  public int getBrokenX() {
    return brokenX;
  }

  public int getBrokenY() {
    return brokenY;
  }

  private void setError(int code)
    throws ProcessException {
    setError(code, 0, 0);
  }

  private void setError(int code, int pos)
    throws ProcessException {
    setError(code, pos, 0);
  }

  private void setError(int code, int posX, int posY)
    throws ProcessException {
    errorCode = code;
    brokenX = posX;
    brokenY = posY;
    MyLog.w(LOG_TAG, "Error (code: " + code
      + " x: " + posX + " y: " + posY + ")");
    throw new ProcessException();
  }

  private static class RVParams {
    private double avg, delta, sigma;

    private RVParams() {
      avg = delta = sigma = 0;
    }

    @Override
    public String toString() {
      return String.format(Locale.US,
        "avg: %.4f, delta: %.4f, sigma: %.4f", avg, delta, sigma);
    }
  }
}
