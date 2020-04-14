package ru.didim99.tstu.core.modeling.processor;

import com.jjoe64.graphview.series.Series;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.core.modeling.Functions;
import ru.didim99.tstu.core.modeling.Variable;
import ru.didim99.tstu.core.optimization.math.PointRN;

/**
 * Distributed system static mode processor
 *
 * Created by didim99 on 13.04.20.
 */
public class DSProcessor extends VariableProcessor {

  private int secondVarId;
  private ArrayList<ArrayList<PointRN>> seriesFamily;
  private ArrayList<String> seriesNames;
  private int seriesIndex;

  public DSProcessor(int variableId) {
    super(variableId);
    seriesFamily = new ArrayList<>();
    seriesNames = new ArrayList<>();
    seriesIndex = -1;

    for (int i = 1; i < Functions.distVars.length; i++) {
      if (Functions.distVars[i].hasDelta()) {
        var = Functions.distVars[i];
        secondVarId = i;
        break;
      }
    }
  }

  @Override
  public void process() {
    DPoint start = new DPoint(variableId);
    for (int i = 1; i < Functions.distVars.length; i++) {
      Variable v = Functions.distVars[i];
      start.set(i, i == secondVarId ? v.getMinValue()
        : v.getDefaultValue());
    }

    double currentValue = start.get(secondVarId);
    double maxOffset = var.getMaxValue() + var.getStep() / 2;
    while (currentValue < maxOffset) {
      seriesNames.add(String.format(Locale.US,
        "%s = %.2f", var.getName(), currentValue));
      seriesFamily.add(computeSingleVar(new DPoint(start), DPoint.L));
      start.set(secondVarId, currentValue += var.getStep());
    }
  }

  private ArrayList<PointRN> computeSingleVar(DPoint start, int varId) {
    ArrayList<PointRN> series = new ArrayList<>();
    Variable var = Functions.distVars[varId];

    start.set(varId, var.getMinValue());
    start.k1 = Functions.rSpeed(Functions.A1, Functions.E1, start.t);
    start.k2 = Functions.rSpeed(Functions.A2, Functions.E2, start.t);
    start.cOut1 = start.cIn;
    start.cOut2 = 0;

    series.add(new DPoint(start));
    DPoint next = new DPoint(start);
    while (next.get(varId) <= var.getMaxValue()) {
      next.set(varId, next.get(varId) + var.getStep());
      next.cOut1 = start.cOut1 + Functions.dC1.f(start) * var.getStep();
      next.cOut2 = start.cOut2 + Functions.dC2.f(start) * var.getStep();
      series.add(new DPoint(next));
      start.set(next);
    }

    return series;
  }

  @Override
  public Series<PointRN> getSeries() {
    if (++seriesIndex < seriesFamily.size()) {
      return buildSeries(seriesFamily.get(seriesIndex),
        seriesNames.get(seriesIndex));
    } else return null;
  }

  @Override
  public String getDescription() {
    return describeSeries(seriesFamily.get(0));
  }

  public static class DPoint implements PointRN {
    public static final int L       = 0;
    public static final int M       = 1;
    public static final int C_IN    = 2;
    public static final int T       = 3;
    public static final int K1      = 4;
    public static final int C_OUT1  = 5;
    public static final int K2      = 6;
    public static final int C_OUT2  = 7;

    private final static int[] Y_INDEX = { C_OUT1, C_OUT2 };

    private final int yIndex;
    private double l, m, cIn, t;
    private double k1, cOut1, k2, cOut2;

    private DPoint(int yIndex) {
      this.yIndex = Y_INDEX[yIndex];
    }

    private DPoint(DPoint src) {
      this.yIndex = src.yIndex;
      set(src);
    }

    @Override
    public void set(int pos, double val) {
      switch (pos) {
        case L: this.l = val; break;
        case M: this.m = val; break;
        case C_IN: this.cIn = val; break;
        case T: this.t = val; break;
      }
    }

    @Override
    public double get(int pos) {
      switch (pos) {
        case L: return l;
        case M: return m;
        case C_IN: return cIn;
        case T: return t;
        case K1: return k1;
        case C_OUT1: return cOut1;
        case K2: return k2;
        case C_OUT2: return cOut2;
        default: return 0;
      }
    }

    @Override
    public double getX() {
      return l;
    }

    @Override
    public double getY() {
      return get(yIndex);
    }

    private void set(DPoint src) {
      this.l = src.l;
      this.m = src.m;
      this.cIn = src.cIn;
      this.t = src.t;
      this.k1 = src.k1;
      this.cOut1 = src.cOut1;
      this.k2 = src.k2;
      this.cOut2 = src.cOut2;
    }
  }
}
