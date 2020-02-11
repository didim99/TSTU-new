package ru.didim99.tstu.core.modeling;

import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;
import java.util.ArrayList;
import java.util.Locale;

import ru.didim99.tstu.core.optimization.math.PointD;

/**
 * Created by didim99 on 11.02.20.
 */
public class StaticProcessor {
  // Static constants
  private static final double kT = 5000;  // W/m^2
  private static final double F = 10;     // m^2
  private static final double tR = 90;    // degC
  private static final double cT = 4187;  // J/kg
  private static final double r = 2.26E6; // J/kg*degC
  // Input parameters
  private static Variable[] vars = new Variable[] {
    new Variable("cIn", 0.08, 0.06, 0.10, 0.004),
    new Variable("mIn", 4.0, 4.0, 6.0, 0.2),
    new Variable("tIn", 130.0, 122.0, 140.0, 0.5)
  };

  private int variableId;
  private Variable var;
  private ArrayList<PointD> series;

  StaticProcessor(int variableId) {
    this.variableId = variableId;
    this.var = vars[variableId];
  }

  void precess() {
    series = new ArrayList<>();
    PointD p = new PointD(vars.length);
    for (int i = 0; i < p.size(); i++) {
      if (i == variableId) p.set(i, var.minValue);
      else p.set(i, vars[i].defaultValue);
    }

    while (p.get(variableId) < var.maxValue) {
      series.add(new PointD(p.get(variableId), cOut(p)));
      p.add(variableId, var.step);
    }
  }

  Series<PointD> getSeries() {
    LineGraphSeries<PointD> series = new LineGraphSeries<>();
    series.resetData(this.series.toArray(new PointD[0]));
    series.setTitle(var.name);
    return series;
  }

  String getDescription() {
    StringBuilder sb = new StringBuilder();
    for (PointD point : series)
      sb.append(String.format(Locale.US, "%7.4f %7.4f\n", point.getX(), point.getY()));
    return sb.toString();
  }

  public static String[] getVarList() {
    String[] list = new String[vars.length];
    for (int i = 0; i < list.length; i++)
      list[i] = vars[i].name;
    return list;
  }

  // Values definition: cIn, mIn, tIn
  private double cOut(PointD in) {
    return in.get(1) * in.get(0) / (in.get(1) + kT * F * (in.get(2) - tR) / (cT * tR - r));
  }

  static class Variable {
    private String name;
    private double defaultValue;
    private double minValue, maxValue;
    private double step;

    Variable(String name, double defaultValue, double minValue, double maxValue, double step) {
      this.name = name;
      this.defaultValue = defaultValue;
      this.minValue = minValue;
      this.maxValue = maxValue;
      this.step = step;
    }
  }
}
