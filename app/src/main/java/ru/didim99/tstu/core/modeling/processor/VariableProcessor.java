package ru.didim99.tstu.core.modeling.processor;

import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.core.modeling.Functions;
import ru.didim99.tstu.core.modeling.Variable;
import ru.didim99.tstu.core.optimization.math.PointD;

/**
 * Created by didim99 on 24.02.20.
 */
public abstract class VariableProcessor {

  int variableId;
  Variable var;
  ArrayList<PointD> series;

  VariableProcessor(int variableId) {
    this.variableId = variableId;
    this.var = Functions.vars[variableId];
  }

  public Series<PointD> getSeries() {
    LineGraphSeries<PointD> series = new LineGraphSeries<>();
    series.resetData(this.series.toArray(new PointD[0]));
    series.setTitle(var.getName());
    return series;
  }

  public String getDescription() {
    StringBuilder sb = new StringBuilder();
    for (PointD point : series)
      sb.append(String.format(Locale.US, "%7.4f %7.4f\n", point.getX(), point.getY()));
    return sb.toString();
  }

  public abstract void process();
}
