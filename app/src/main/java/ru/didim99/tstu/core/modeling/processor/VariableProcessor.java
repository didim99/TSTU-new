package ru.didim99.tstu.core.modeling.processor;

import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.core.modeling.Functions;
import ru.didim99.tstu.core.modeling.Variable;
import ru.didim99.tstu.core.optimization.math.PointRN;

/**
 * Created by didim99 on 24.02.20.
 */
public abstract class VariableProcessor {
  private static final int MAX_SERIES_VISIBLE = 500;

  int variableId;
  Variable var;
  ArrayList<PointRN> series;

  VariableProcessor(int variableId) {
    this.variableId = variableId;
    this.var = Functions.lumpedVars[variableId];
    this.series = new ArrayList<>();
  }

  public Series<PointRN> getSeries() {
    return buildSeries(this.series, var.getName());
  }

  public String getDescription() {
    return describeSeries(series);
  }

  String describeSeries(ArrayList<PointRN> series) {
    StringBuilder sb = new StringBuilder();
    for (PointRN point : series)
      sb.append(String.format(Locale.US, "%7.4f %7.4f\n", point.getX(), point.getY()));
    return sb.toString();
  }

  Series<PointRN> buildSeries(ArrayList<PointRN> data, String name) {
    LineGraphSeries<PointRN> series = new LineGraphSeries<>();

    if (data.size() <= MAX_SERIES_VISIBLE)
      series.resetData(data.toArray(new PointRN[0]));
    else {
      double step = data.size() / 500d;
      for (int pos = 0; pos < 500; pos++)
        series.appendData(data.get((int) Math.floor(pos * step)),
          false, 500);
    }

    series.setTitle(name);
    return series;
  }

  public abstract void process();
}
