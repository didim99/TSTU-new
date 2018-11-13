package ru.didim99.tstu.core.math;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 11.11.18.
 */
public class MathStat {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MathStat";
  private static final String G_DELIMITER = "/";
  private static final String I_DELIMITER = "..";
  private static final String V_DELIMITER = "\\s+";
  private static final String I_REGEX = "\\.\\.";
  private static final String G_REGEX = "\\s*" + G_DELIMITER + "\\s*";

  private static final float RADIUS = 10f;
  private static final double GRAPH_OFFSET = 0.1;
  private static final int BG = R.color.graph0;
  private static final int[] COLORS = {
    R.color.graph1, R.color.graph2,
    R.color.graph3, R.color.graph4
  };

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

  public static final class GraphType {
    public static final int POLYGON_F = 1;
    public static final int POLYGON_W = 2;
    public static final int HYSTOGRAM = 3;
  }

  private boolean useGroups;
  private boolean useIntervals;
  private int groupCount, n;
  private double xAvg, delta, sigma;
  private ArrayList<Group> groups;
  private int brokenGroup, brokenValue;
  private int errorCode;

  public void compute(String xStr, String fStr) throws ProcessException {
    MyLog.d(LOG_TAG, "Initializing...");
    if (xStr.isEmpty())
      setError(ErrorCode.EMPTY_X);
    if (fStr.isEmpty())
      setError(ErrorCode.EMPTY_F);

    n = 0;
    xAvg = delta = sigma = 0;
    groups = new ArrayList<>();
    useGroups = xStr.contains(G_DELIMITER);
    useIntervals = xStr.contains(I_DELIMITER);
    parseValues(xStr, fStr);

    MyLog.d(LOG_TAG, "Computing statistics...");
    for (Group g : groups) {
      for (Value x : g.xList)
        g.n += x.f;
      n += g.n;

      if (useIntervals) {
        for (Value x : g.xList)
          x.point = (x.start + x.end) / 2;
      }
    }

    for (Group g : groups) {
      for (Value x : g.xList)
        x.w = (double) x.f / g.n;

      for (Value x : g.xList)
        g.xAvg += x.point * x.f;
      g.xAvg /= g.n;

      for (Value x : g.xList)
        g.delta += Math.pow(x.point - g.xAvg, 2) * x.f;
      g.delta /= g.n;
      g.sigma = Math.sqrt(g.delta);
    }

    if (useGroups) {
      for (Group g : groups)
        xAvg += g.xAvg * g.n / n;



    } else {
      xAvg = groups.get(0).xAvg;
      delta = groups.get(0).delta;
      sigma = groups.get(0).sigma;
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

      Value v;
      for (int value = 0; value < xVals.length; value++) {
        try {
          if (useIntervals) {
            iVals = xVals[value].split(I_REGEX);
            if (iVals.length != 2)
              setError(ErrorCode.INCORRECT_FORMAT_X, group, value);
            xStart = Double.parseDouble(iVals[0].trim());
            xEnd = Double.parseDouble(iVals[1].trim());
            v = new Value(xStart, xEnd);
          } else {
            x = Double.parseDouble(xVals[value].trim());
            v = new Value(x);
          }

          try {
            v.f = Integer.parseInt(fVals[value].trim());
            if (v.f <= 0)
              setError(ErrorCode.NEGATIVE_F, group, value);
          } catch (NumberFormatException e) {
            setError(ErrorCode.INCORRECT_FORMAT_F, group, value);
          }

          g.xList.add(v);
        } catch (NumberFormatException e) {
          setError(ErrorCode.INCORRECT_FORMAT_X, group, value);
        }
      }

      groups.add(g);
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

  public String getTextResult(Context ctx) {
    StringBuilder sb = new StringBuilder();

    if (useGroups) {
      int gNum = 0;
      sb.append(String.format(Locale.US, "Groups: %d\n", groupCount));
      for (Group g : groups) {
        sb.append(String.format(Locale.US, "\nGroup %d\n", gNum));
        sb.append(String.format(Locale.US, "  N%d = %d\n", gNum, g.n));
        sb.append(String.format(Locale.US, "  X%d = %.3f\n", gNum, g.xAvg));
        sb.append(String.format(Locale.US, "  D%d = %.3f\n", gNum, g.delta));
        sb.append(String.format(Locale.US, "  S%d = %.3f\n", gNum++, g.sigma));
      }
      sb.append("\n");
    }

    sb.append("General\n");
    sb.append(String.format(Locale.US, "  N = %d\n", n));
    sb.append(String.format(Locale.US, "  X = %.3f\n", xAvg));
    sb.append(String.format(Locale.US, "  D = %.3f\n", delta));
    sb.append(String.format(Locale.US, "  S = %.3f\n", sigma));

    return sb.toString();
  }

  public void drawGraph(GraphView view, int type) {
    ArrayList<DataPoint> mainP = new ArrayList<>();
    Resources res = view.getResources();

    switch (type) {
      case GraphType.POLYGON_F:
      case GraphType.POLYGON_W:
        LineGraphSeries<DataPoint> main = new LineGraphSeries<>();
        main.setColor(res.getColor(BG));
        view.addSeries(main);

        double f, yMax = -Double.MAX_VALUE, yMin = Double.MAX_VALUE;
        PointsGraphSeries<DataPoint> series;
        for (int i = 0; i < groupCount; i++) {
          series = new PointsGraphSeries<>();
          Group g = groups.get(i);
          Collections.sort(g.xList);

          for (Value x : g.xList) {
            f = type == GraphType.POLYGON_F ? x.f : x.w;
            if (f > yMax) yMax = f;
            if (f < yMin) yMin = f;

            DataPoint p = new DataPoint(x.point, f);
            series.appendData(p, false, g.size());
            mainP.add(p);
          }

          series.setColor(res.getColor(COLORS[i % COLORS.length]));
          series.setSize(RADIUS);
          view.addSeries(series);
        }

        Collections.sort(mainP, (p1, p2) ->
          Double.compare(p1.getX(), p2.getX()));
        for (DataPoint p : mainP)
          main.appendData(p, true, n);

        double xMin = mainP.get(0).getX();
        double xMax = mainP.get(mainP.size() - 1).getX();
        double xOffset = (xMax - xMin) * GRAPH_OFFSET;
        double yOffset = (yMax - yMin) * GRAPH_OFFSET;
        Viewport viewport = view.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(xMin - xOffset);
        viewport.setMaxX(xMax + xOffset);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(yMin - yOffset);
        viewport.setMaxY(yMax + yOffset);
        break;
      case GraphType.HYSTOGRAM:
        if (!useIntervals) return;
        BarGraphSeries<DataPoint> barGraph;

        double l;
        for (int i = 0; i < groupCount; i++) {
          Group g = groups.get(i);
          for (int j = 0; j < g.size(); j++) {
            barGraph = new BarGraphSeries<>();
            Value x = g.xList.get(j);

            l = x.end - x.start;
            DataPoint p = new DataPoint(x.point, x.f / l);
            barGraph.appendData(p, false, 10);

            barGraph.setDataWidth(l);
            barGraph.setColor(res.getColor(COLORS[i % COLORS.length]));
            view.addSeries(barGraph);
          }
        }
        break;
    }
  }

  private static class Value implements Comparable<Value> {
    private double point, start, end;
    private double w;
    private int f;

    Value(double point) {
      this.point = point;
    }

    Value(double start, double end) {
      this.start = start;
      this.end = end;
    }

    @Override
    public int compareTo(@NonNull Value o) {
      return Double.compare(point, o.point);
    }
  }

  private static class Group {
    private ArrayList<Value> xList;
    private double xAvg, delta, sigma;
    private int n;

    Group() {
      xList = new ArrayList<>();
      xAvg = delta = sigma = 0;
      n = 0;
    }

    int size() {
      return xList.size();
    }
  }

  public class ProcessException extends Exception {}
}
