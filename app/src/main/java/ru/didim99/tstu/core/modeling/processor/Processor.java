package ru.didim99.tstu.core.modeling.processor;

import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.core.optimization.math.PointRN;

/**
 * Created by didim99 on 08.05.20.
 */
public abstract class Processor {
  private static final int MAX_SERIES_VISIBLE = 500;

  protected ArrayList<PointRN> series;

  protected Processor() {
    this.series = new ArrayList<>();
  }

  public abstract void process();
  public abstract Series<PointRN> getSeries();

  public String getDescription() {
    return describeSeries(series);
  }

  protected String describeSeries(ArrayList<PointRN> series) {
    StringBuilder sb = new StringBuilder();
    for (PointRN point : series)
      sb.append(String.format(Locale.US, "%7.4f %7.4f\n", point.getX(), point.getY()));
    return sb.toString();
  }

  protected Series<PointRN> buildSeries(ArrayList<PointRN> data, String name) {
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
}
