package ru.didim99.tstu.core.math;

import java.util.ArrayList;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 11.11.18.
 */
public class MathStat {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MathStat";
  private static final String G_DELIMITER = "/";
  private static final String I_DELIMITER = "\\.\\.";
  private static final String V_DELIMITER = "\\s+";
  private static final String G_REGEX = "\\s*" + G_DELIMITER + "\\s*";

  public static final class ErrorCode {
    public static final int EMPTY_X = 1;
    public static final int EMPTY_F = 2;
    public static final int EMPTY_GROUP = 3;
    public static final int GROUPS_NOT_EQUALS = 4;
    public static final int ELEMENTS_NOT_EQUALS = 5;
    public static final int INCORRECT_FORMAT_X = 6;
    public static final int INCORRECT_FORMAT_F = 7;
    public static final int NEGATIVE_F = 8;
  }

  private int groupCount, N;
  private boolean useGroups;
  private boolean useIntervals;
  private ArrayList<Group> groups;
  private int brokenGroup, brokenValue;
  private int errorCode;

  public void compute(String xStr, String fStr) throws ProcessException {
    MyLog.d(LOG_TAG, "Initializing...");
    if (xStr.isEmpty())
      setError(ErrorCode.EMPTY_X);
    if (fStr.isEmpty())
      setError(ErrorCode.EMPTY_F);

    N = 0;
    groups = new ArrayList<>();
    useGroups = xStr.contains(G_DELIMITER);
    useIntervals = xStr.contains(I_DELIMITER);
    parseValues(xStr, fStr);

    MyLog.d(LOG_TAG, "Computing statistics...");
    for (Group g : groups) {
      for (Integer f : g.fList)
        N += f;
    }

    

    MyLog.d(LOG_TAG, "Computing completed");
  }

  private void parseValues(String xStr, String fStr)
    throws ProcessException {
    MyLog.d(LOG_TAG, "Parsing values...");
    String[] xGroups = useGroups ? xStr.split(G_REGEX) : new String[] {xStr};
    String[] fGroups = useGroups ? fStr.split(G_REGEX) : new String[] {fStr};
    groupCount = useGroups ? xGroups.length : 1;

    if (xGroups.length != fGroups.length)
      setError(ErrorCode.GROUPS_NOT_EQUALS);

    int f;
    double xStart, xEnd, x;
    String[] xVals, iVals, fVals;
    for (int group = 0; group < groupCount; group++) {
      Group g = new Group();
      xVals = xGroups[group].trim().split(V_DELIMITER);
      fVals = fGroups[group].trim().split(V_DELIMITER);

      if (xVals.length == 0)
        setError(ErrorCode.EMPTY_GROUP, group);
      if (xVals.length != fVals.length)
        setError(ErrorCode.ELEMENTS_NOT_EQUALS, group);

      for (int value = 0; value < xVals.length; value++) {
        try {
          if (useIntervals) {
            iVals = xVals[group].split(I_DELIMITER);
            xStart = Double.parseDouble(iVals[0].trim());
            xEnd = Double.parseDouble(iVals[1].trim());
            g.xList.add(new Value(xStart, xEnd));
          } else {
            x = Double.parseDouble(xVals[value].trim());
            g.xList.add(new Value(x));
          }
        } catch (NumberFormatException e) {
          setError(ErrorCode.INCORRECT_FORMAT_X, group, value);
        }

        try {
          f = Integer.parseInt(fVals[value].trim());
          if (f <= 0)
            setError(ErrorCode.NEGATIVE_F, group, value);
          g.fList.add(f);
        } catch (NumberFormatException e) {
          setError(ErrorCode.INCORRECT_FORMAT_F, group, value);
        }
      }
    }
  }

  private void setError(int code)
    throws ProcessException {
    setError(code, 0, 0);
  }

  private void setError(int code, int group)
    throws ProcessException {
    setError(code, group, 0);
  }

  private void setError(int code, int group, int value)
    throws ProcessException {
    brokenGroup = group;
    brokenValue = value;
    errorCode = code;
    MyLog.w(LOG_TAG, "Error (code: " + code
      + " group: " + group + " value: " + value + ")");
    throw new ProcessException();
  }

  public int getErrorCode() {
    return errorCode;
  }

  public int getBrokenGroup() {
    return brokenGroup + 1;
  }

  public int getBrokenValue() {
    return brokenValue + 1;
  }

  private static class Value {
    private double point;
    private double start;
    private double end;

    Value(double point) {
      this.point = point;
    }

    Value(double start, double end) {
      this.start = start;
      this.end = end;
    }
  }

  private static class Group {
    private ArrayList<Value> xList;
    private ArrayList<Integer> fList;
    private ArrayList<Double> wList;

    Group() {
      xList = new ArrayList<>();
      fList = new ArrayList<>();
      wList = new ArrayList<>();
    }
  }

  public class ProcessException extends Exception {}
}
