package ru.didim99.tstu.core.modeling;

import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.core.optimization.math.FunctionRN;
import ru.didim99.tstu.core.optimization.math.PointD;

/**
 * Created by didim99 on 11.02.20.
 */
class StaticProcessor {

  private int variableId;
  private Variable var;
  private ArrayList<PointD> series;
  private FunctionRN cOut;

  StaticProcessor(int variableId) {
    this.variableId = variableId;
    this.var = Functions.vars[variableId];
    this.cOut = Functions.cOutStatic;
  }

  void precess() {
    series = new ArrayList<>();
    PointD p = new PointD(Functions.vars.length);
    for (int i = 0; i < p.size(); i++) {
      if (i == variableId) p.set(i, var.getMinValue());
      else p.set(i, Functions.vars[i].getDefaultValue());
    }

    while (p.get(variableId) < var.getMaxValue()) {
      series.add(new PointD(p.get(variableId), cOut.f(p)));
      p.add(variableId, var.getStep());
    }
  }

  Series<PointD> getSeries() {
    LineGraphSeries<PointD> series = new LineGraphSeries<>();
    series.resetData(this.series.toArray(new PointD[0]));
    series.setTitle(var.getName());
    return series;
  }

  String getDescription() {
    StringBuilder sb = new StringBuilder();
    for (PointD point : series)
      sb.append(String.format(Locale.US, "%7.4f %7.4f\n", point.getX(), point.getY()));
    return sb.toString();
  }
}
