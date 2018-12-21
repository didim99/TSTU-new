package ru.didim99.tstu.core.math;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 14.12.18.
 */
public class CAProcessor {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_CA";
  private static final String V_REGEX = "\\s+";
  private static final int L = 2;

  // settings
  private static final String KEY_ALPHA = "CA.alpha";
  private static final String KEY_R_TYPE = "CA.regressionType";
  private static final String KEY_REV_FACTOR = "CA.revFactor";
  private static final float DEFVALUE_ALPHA = 0.05f;
  private static final int DEFVALUE_R_TYPE = 1;
  private static final float DEFVALUE_REV_FACTOR = 1.0f;
  // output
  private static final int CW = 7;
  private static final int L_CW = 2;
  private static final int PREC = 2;
  private static final int PREC_F = 1;
  private static final int H_CW = CW + PREC_F;
  private static final int H_PREC = PREC + PREC_F;
  private static final String S_HOLDER = "%" + CW + "s ";
  private static final String SW_HOLDER = "%" + H_CW + "s ";
  private static final String SS_HOLDER = "%" + L_CW + "s ";
  private static final String F_HOLDER = "%" + CW + "." + PREC + "f ";
  private static final String FW_HOLDER = "%" + H_CW + "." + H_PREC + "f ";
  private static final String T_HEADER = SS_HOLDER + S_HOLDER + S_HOLDER +
    S_HOLDER + S_HOLDER + S_HOLDER + S_HOLDER + SW_HOLDER + SW_HOLDER + "\n";
  private static final String T_ROW = SS_HOLDER + F_HOLDER + F_HOLDER +
    F_HOLDER + F_HOLDER + F_HOLDER + F_HOLDER + FW_HOLDER + FW_HOLDER + "\n";
  private static final String T_LAST_ROW = SS_HOLDER + F_HOLDER + F_HOLDER +
    F_HOLDER + F_HOLDER + F_HOLDER + S_HOLDER + FW_HOLDER + FW_HOLDER + "\n";

  public static final class ErrorCode {
    public static final int EMPTY_X = 1;
    public static final int EMPTY_F = 2;
    public static final int ELEMENTS_NOT_EQUALS = 5;
    public static final int INCORRECT_FORMAT_X = 6;
    public static final int INCORRECT_FORMAT_Y = 7;
  }

  public static final class RegressionType {
    public static final int LINEAR = 1;
    public static final int REVERSE = 2;
  }

  // state
  private int errorCode;
  private int brokenValue;
  private FunctionTableR2 student;
  // workflow
  private int n;
  private Config config;
  private boolean relevantR;
  private ArrayList<Double> xVals, yVals;
  private ArrayList<Double> x2Vals, y2Vals, xyVals;
  private ArrayList<Double> yxVals, qFVals, qRVals;
  private double xSum, ySum, xySum, x2Sum, y2Sum;
  private double xAvg, yAvg, xS, yS, r, bYX;
  private double tN, tCr, qFSum, qRSum, tRN, s;

  public CAProcessor(Context ctx, SharedPreferences settings)
    throws IOException {
    Storage storage = Storage.getInstance();
    if (!storage.initCompleted())
      storage.init(ctx);
    student = storage.getStudent();
    config = new Config(settings);
  }

  public void compute(String xStr, String yStr)
    throws ProcessException {
    initValues(xStr, yStr);
    basicAnalysis();
    MyLog.d(LOG_TAG, "Analysis completed");
  }

  private void basicAnalysis() {
    MyLog.d(LOG_TAG, "Analysing...");

    switch (config.regType) {
      case RegressionType.REVERSE:
        for (int i = 0; i < n; i++)
          xVals.set(i, config.revFactor / xVals.get(i));
        break;
    }

    for (int i = 0; i < n; i++)
      xyVals.add(xVals.get(i) * yVals.get(i));
    for (Double x : xVals)
      x2Vals.add(Math.pow(x, 2));
    for (Double y : yVals)
      y2Vals.add(Math.pow(y, 2));

    for (int i = 0; i < n; i++) {
      xSum += xVals.get(i);
      ySum += yVals.get(i);
      xySum += xyVals.get(i);
      x2Sum += x2Vals.get(i);
      y2Sum += y2Vals.get(i);
    }

    xS = Math.sqrt(n * x2Sum - Math.pow(xSum, 2));
    yS = Math.sqrt(n * y2Sum - Math.pow(ySum, 2));
    r = (n * xySum - xSum * ySum) / (xS * yS);

    tN = r * Math.sqrt(n - 2) / Math.sqrt(1 - Math.pow(r, 2));
    tCr = student.get(1 - config.alpha, n - 2);
    relevantR = !(Math.abs(tN) < tCr);

    xAvg = xSum / n;
    yAvg = ySum / n;
    bYX = r * yS / xS;

    for (Double x : xVals)
      yxVals.add(yAvg + bYX * (x - xAvg));
    for (int i = 0; i < n; i++) {
      qFVals.add(Math.pow(yxVals.get(i) - yAvg, 2));
      qRVals.add(Math.pow(yxVals.get(i) - yVals.get(i), 2));
      qFSum += qFVals.get(i);
      qRSum += qRVals.get(i);
    }

    tRN = (qFSum * (n - L)) / (qRSum * (L - 1));
    s = qRSum / (n - L);
  }

  private void initValues(String xStr, String yStr)
    throws ProcessException {
    MyLog.d(LOG_TAG, "Initializing...");
    if (xStr.isEmpty())
      setError(ErrorCode.EMPTY_X);
    if (yStr.isEmpty())
      setError(ErrorCode.EMPTY_F);

    String[] xStrVals = xStr.split(V_REGEX);
    String[] yStrVals = yStr.split(V_REGEX);

    if (xStrVals.length != yStrVals.length)
      setError(ErrorCode.ELEMENTS_NOT_EQUALS);

    xVals = new ArrayList<>();
    yVals = new ArrayList<>();
    xyVals = new ArrayList<>();
    x2Vals = new ArrayList<>();
    y2Vals = new ArrayList<>();
    yxVals = new ArrayList<>();
    qFVals = new ArrayList<>();
    qRVals = new ArrayList<>();
    xSum = ySum = xySum = x2Sum
      = y2Sum = qFSum = qRSum = 0;

    try {
      for (String s : xStrVals)
        xVals.add(Double.parseDouble(s.trim()));
    } catch (NumberFormatException e) {
      setError(ErrorCode.INCORRECT_FORMAT_X, xVals.size());
    }

    try {
      for (String s : yStrVals)
        yVals.add(Double.parseDouble(s.trim()));
    } catch (NumberFormatException e) {
      setError(ErrorCode.INCORRECT_FORMAT_Y, yVals.size());
    }

    if (xVals.size() != yVals.size())
      setError(ErrorCode.ELEMENTS_NOT_EQUALS);
    n = xVals.size();
  }

  private void setError(int code)
    throws ProcessException {
    setError(code, 0);
  }

  private void setError(int code, int value)
    throws ProcessException {
    errorCode = code;
    brokenValue = value;
    MyLog.w(LOG_TAG, "Error (code: " + code
      + " value: " + value + ")");
    throw new ProcessException();
  }

  public Config getConfig() {
    return config;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public int getBrokenValue() {
    return brokenValue;
  }

  public String getTextResult(Context ctx) {
    StringBuilder sb = new StringBuilder();
    boolean rr = config.isRegressionReverse();
    String XStr = rr ? "Z" : "X";
    String xStr = rr ? "z" : "x";

    sb.append(String.format(Locale.US, T_HEADER,
      "", XStr, "Y", XStr + "Y", XStr + "^2",
      "Y^2", "Y" + xStr, "Qf", "Qr"));
    for (int i = 0; i < n; i++) {
      sb.append(String.format(Locale.US, T_ROW, "",
        xVals.get(i), yVals.get(i), xyVals.get(i), x2Vals.get(i),
        y2Vals.get(i), yxVals.get(i), qFVals.get(i), qRVals.get(i)));
    }
    sb.append("\n");
    sb.append(String.format(Locale.US, T_LAST_ROW,
      "Σ", xSum, ySum, xySum, x2Sum, y2Sum, "", qFSum, qRSum));

    sb.append("\n");
    sb.append(String.format(Locale.US, "    n: %d\n", n));
    sb.append(String.format(Locale.US, "    α: %.3f\n", config.alpha));
    if (rr) sb.append(String.format(Locale.US, "   rf: %.3f\n", config.revFactor));
    sb.append("\n");
    sb.append(String.format(Locale.US, "   S%s: %.3f\n", xStr, xS));
    sb.append(String.format(Locale.US, "   Sy: %.3f\n", yS));
    sb.append(String.format(Locale.US, "  R%sy: %.3f\n", xStr, r));
    sb.append(String.format(Locale.US, "   Tn: %.3f\n", tN));
    sb.append(String.format(Locale.US, "  Tcr: %.3f", tCr));
    sb.append(" => ").append(ctx.getString(relevantR ?
      R.string.ca_relevantR : R.string.ca_irrelevantR, xStr));
    sb.append("\n");

    sb.append("\n").append(ctx.getString(R.string.rv_regressionEquation)).append(":\n\n");
    sb.append(String.format(Locale.US, "  By%s: %.3f\n", xStr, bYX));
    sb.append(String.format(Locale.US, " %savg: %.3f\n", XStr, xAvg));
    sb.append(String.format(Locale.US, " Yavg: %.3f\n", yAvg));
    sb.append("\n");
    sb.append(String.format(Locale.US,
      "  Y%s - %.3f = %.3f * (%s - %.3f)\n",
      xStr, yAvg, bYX, xStr, xAvg));
    double a = yAvg - bYX * xAvg;
    sb.append(String.format(Locale.US, "  Y%s = %.3f * %s %s %.3f\n",
      xStr, bYX, xStr, (a < 0 ? "-" : "+"), Math.abs(a)));
    if (rr) sb.append(String.format(Locale.US, "  Yx = %.3f / x %s %.3f\n",
      bYX * 6, (a < 0 ? "-" : "+"), Math.abs(a)));
    sb.append("\n");
    sb.append(String.format(Locale.US, "  Trn: %.3f\n", tRN));
    sb.append(String.format(Locale.US, "   S0: %.3f\n", s));
    return sb.toString();
  }

  public static class Config {
    private float alpha, revFactor;
    private int regType;

    private Config(SharedPreferences settings) {
      alpha = settings.getFloat(KEY_ALPHA, DEFVALUE_ALPHA);
      regType = settings.getInt(KEY_R_TYPE, DEFVALUE_R_TYPE);
      revFactor = settings.getFloat(KEY_REV_FACTOR, DEFVALUE_REV_FACTOR);
    }

    public void update(float alpha, float revFactor) {
      this.revFactor = revFactor;
      this.alpha = alpha;
    }

    public double getAlpha() {
      return alpha;
    }

    public int getRegType() {
      return regType;
    }

    public float getRevFactor() {
      return revFactor;
    }

    public void setRegType(int regType) {
      this.regType = regType;
    }

    public void updateSettings(SharedPreferences settings) {
      settings.edit()
        .putInt(KEY_R_TYPE, regType)
        .putFloat(KEY_ALPHA, alpha)
        .putFloat(KEY_REV_FACTOR, revFactor)
        .apply();
    }

    public boolean isRegressionReverse() {
      return regType == RegressionType.REVERSE;
    }
  }
}
