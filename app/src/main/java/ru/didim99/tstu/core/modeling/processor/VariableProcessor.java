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
  private static final int MAX_SERIES_VISIBLE = 500;

  int variableId;
  Variable var;
  ArrayList<PointD> series;

  VariableProcessor(int variableId) {
    this.variableId = variableId;
    this.var = Functions.vars[variableId];
    this.series = new ArrayList<>();
  }

  public Series<PointD> getSeries() {
    LineGraphSeries<PointD> series = new LineGraphSeries<>();

    if (this.series.size() <= MAX_SERIES_VISIBLE)
      series.resetData(this.series.toArray(new PointD[0]));
    else {
      int step = this.series.size() / 500;
      for (int pos = 0; pos < this.series.size(); pos += step) {
        series.appendData(this.series.get(pos),
          false, 500);
      }
    }

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
