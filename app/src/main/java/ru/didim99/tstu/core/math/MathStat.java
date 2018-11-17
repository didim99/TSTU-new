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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 11.11.18.
 */
public class MathStat {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MathStat";

  private static final String G_DELIMITER_1 = "/";
  private static final String G_DELIMITER_2 = "#";
  private static final String I_DELIMITER = "..";
  private static final String V_REGEX = "\\s+";
  private static final String I_REGEX = "\\.\\.";
  private static final String G_REGEX = "\\s*[/#]\\s*";
  // view-specific
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

  static final class GraphType {
    static final int POLYGON_F = 1;
    static final int POLYGON_W = 2;
    static final int HISTOGRAM = 3;
  }

  // state
  private int errorCode;
  private int brokenGroup, brokenValue;
  // workflow
  private Config config;
  private boolean useGroups, useIntervals;
  private int groupCount, n;
  private double xAvg, delta, sigma, vf;
  private double deltaG, sigmaG;
  private ArrayList<Group> groups;
  // view
  private int graphType;
  private boolean splitGroups;

  public MathStat() {
    config = new Config();
    graphType = GraphType.POLYGON_F;
    splitGroups = false;
  }

  public void compute(String xStr, String fStr)
    throws ProcessException {
    initValues(xStr, fStr);
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
        x.w = (double) x.f / (useGroups ? n : g.n);

      for (Value x : g.xList)
        g.xAvg += x.point * x.f;
      g.xAvg /= g.n;

      for (Value x : g.xList)
        g.delta += Math.pow(x.point - g.xAvg, 2) * x.f;
      g.delta /= config.deltaCorrection ? g.n - 1 : g.n;
      g.sigma = Math.sqrt(g.delta);
      g.vf = g.sigma / g.xAvg;
    }

    if (useGroups) {
      for (Group g : groups)
        xAvg += g.xAvg * g.n / n;

      for (Group g : groups) {
        deltaG += Math.pow(g.xAvg - xAvg, 2) * g.n / n;
        sigmaG += g.delta * g.n / n;
      }

      for (Group g : groups) {
        for (Value x : g.xList) {
          delta += Math.pow(x.point - xAvg, 2) * x.f
            / (config.deltaCorrection ? n - 1 : n);
        }
      }

      sigma = Math.sqrt(delta);
      vf = sigma / xAvg;
    } else {
      Group g = groups.get(0);
      xAvg = g.xAvg;
      delta = g.delta;
      sigma = g.sigma;
      vf = g.vf;
    }

    MyLog.d(LOG_TAG, "Computing completed");
  }

  private void initValues(String xStr, String fStr)
    throws ProcessException {
    MyLog.d(LOG_TAG, "Initializing...");
    if (xStr.isEmpty())
      setError(ErrorCode.EMPTY_X);
    if (fStr.isEmpty())
      setError(ErrorCode.EMPTY_F);

    n = 0;
    xAvg = delta = sigma = vf = 0;
    groups = new ArrayList<>();
    useGroups = xStr.contains(G_DELIMITER_1)
      || xStr.contains(G_DELIMITER_2);
    useIntervals = xStr.contains(I_DELIMITER);

    parseValues(xStr, fStr);
    checkXRepeats();
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
      xVals = xGroups[group].trim().split(V_REGEX);
      fVals = fGroups[group].trim().split(V_REGEX);

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

  private void checkXRepeats() {
    if (!useGroups) return;
    Set<Double> values = new HashSet<>();
    for (Group g : groups) {
      for (Value v : g.xList) {
        if (values.contains(v.point)) {
          splitGroups = true;
          return;
        } else
          values.add(v.point);
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

  public void switchGraphType() {
    switch (graphType) {
      case GraphType.POLYGON_F:
        graphType = GraphType.POLYGON_W;
        break;
      case GraphType.POLYGON_W:
        graphType = GraphType.POLYGON_F;
        break;
    }
  }

  public boolean hasResult() {
    return groups != null && !groups.isEmpty();
  }

  public Config getConfig() {
    return config;
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
      int gNum = 1;
      sb.append(ctx.getString(R.string.mathStat_res_groupCount,  groupCount));
      for (Group g : groups) {
        sb.append(ctx.getString(R.string.mathStat_res_group, gNum));
        sb.append(String.format(Locale.US, "  N%-2d = %d\n", gNum, g.n));
        sb.append(String.format(Locale.US, "  X%-2d = %.3f\n", gNum, g.xAvg));
        sb.append(String.format(Locale.US, "  D%-2d = %.3f\n", gNum, g.delta));
        sb.append(String.format(Locale.US, "  S%-2d = %.3f\n", gNum, g.sigma));
        sb.append(String.format(Locale.US, "  V%-2d = %.3f (%.2f%%)\n",
          gNum, g.vf, g.vf * 100));
        gNum++;
      }
      sb.append("\n");
    }

    sb.append(ctx.getString(R.string.mathStat_res_general));
    sb.append(String.format(Locale.US, "  N   = %d\n", n));
    sb.append(String.format(Locale.US, "  X   = %.3f\n", xAvg));
    sb.append(String.format(Locale.US, "  D   = %.3f\n", delta));
    sb.append(String.format(Locale.US, "  S   = %.3f\n", sigma));
    sb.append(String.format(Locale.US, "  V   = %.3f (%.2f%%)\n",
      vf, vf * 100));
    if (useGroups) {
      sb.append(String.format(Locale.US, "  d   = %.3f\n", deltaG));
      sb.append(String.format(Locale.US, "  s   = %.3f\n", sigmaG));
    }

    return sb.toString();
  }

  public void splitGroups(GraphView view) {
    if (!useGroups) return;
    splitGroups = !splitGroups;
    drawGraph(view);
  }

  public void drawGraph(GraphView view) {
    MyLog.d(LOG_TAG, "Drawing graph (" + graphType + ")...");
    Resources res = view.getResources();
    view.removeAllSeries();

    switch (graphType) {
      case GraphType.POLYGON_F:
      case GraphType.POLYGON_W:
        drawPolygon(view, res);
        break;
      case GraphType.HISTOGRAM:
        drawHistogram(view, res);
        break;
    }

    MyLog.d(LOG_TAG, "Graph drawn");
  }

  private void drawPolygon(GraphView view, Resources res) {
    boolean isRelative = graphType == GraphType.POLYGON_W;
    LineGraphSeries<DataPoint> main = new LineGraphSeries<>();
    ArrayList<DataPoint> mainP = new ArrayList<>();

    view.setTitle(res.getString(isRelative ?
      R.string.mathStat_gType_polygonW : R.string.mathStat_gType_polygonF));

    double f, yMax = -Double.MAX_VALUE, yMin = Double.MAX_VALUE;
    double xMax = -Double.MAX_VALUE, xMin = Double.MAX_VALUE;
    PointsGraphSeries<DataPoint> series;

    if (!splitGroups) {
      main.setColor(res.getColor(BG));
      view.addSeries(main);
    }

    for (int i = 0; i < groupCount; i++) {
      int gColor = res.getColor(COLORS[i % COLORS.length]);
      series = new PointsGraphSeries<>();
      Group g = groups.get(i);
      Collections.sort(g.xList);
      if (splitGroups) {
        main = new LineGraphSeries<>();
        main.setColor(gColor);
        view.addSeries(main);
      }

      for (Value x : g.xList) {
        f = isRelative ? x.w : x.f;
        if (x.point > xMax) xMax = x.point;
        if (x.point < xMin) xMin = x.point;
        if (f > yMax) yMax = f;
        if (f < yMin) yMin = f;

        DataPoint p = new DataPoint(x.point, f);
        series.appendData(p, false, g.n);
        if (splitGroups)
          main.appendData(p, true, g.n);
        else
          mainP.add(p);
      }

      series.setColor(gColor);
      series.setSize(RADIUS);
      view.addSeries(series);
    }

    if (!splitGroups) {
      Collections.sort(mainP, (p1, p2) ->
        Double.compare(p1.getX(), p2.getX()));
      for (DataPoint p : mainP)
        main.appendData(p, true, n);
    }

    double xOffset = (xMax - xMin) * GRAPH_OFFSET;
    double yOffset = (yMax - yMin) * GRAPH_OFFSET;
    Viewport viewport = view.getViewport();
    viewport.setXAxisBoundsManual(true);
    viewport.setMinX(xMin - xOffset);
    viewport.setMaxX(xMax + xOffset);
    viewport.setYAxisBoundsManual(true);
    viewport.setMinY(yMin - yOffset);
    viewport.setMaxY(yMax + yOffset);
  }

  private void drawHistogram(GraphView view, Resources res) {
    if (!useIntervals) return;
    BarGraphSeries<DataPoint> barGraph;
    view.setTitle(res.getString(
      R.string.mathStat_gType_histogram));

    double l;
    for (int i = 0; i < groupCount; i++) {
      Group g = groups.get(i);
      for (Value x : g.xList) {
        l = x.end - x.start;
        barGraph = new BarGraphSeries<>();
        DataPoint p = new DataPoint(x.point, x.f / l);
        barGraph.appendData(p, false, 10);

        barGraph.setDataWidth(l);
        barGraph.setColor(res.getColor(COLORS[i % COLORS.length]));
        view.addSeries(barGraph);
      }
    }
  }

  public static class Config {
    private boolean deltaCorrection;

    private Config() {
      this.deltaCorrection = false;
    }

    public Config(boolean deltaCorrection) {
      this.deltaCorrection = deltaCorrection;
    }

    public boolean isDeltaCorrection() {
      return deltaCorrection;
    }

    public void setDeltaCorrection(boolean deltaCorrection) {
      this.deltaCorrection = deltaCorrection;
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
    private double xAvg, delta, sigma, vf;
    private int n;

    Group() {
      xList = new ArrayList<>();
      xAvg = delta = sigma = vf = 0;
      n = 0;
    }
  }

  public class ProcessException extends Exception {}
}
