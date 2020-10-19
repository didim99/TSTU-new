package ru.didim99.tstu.core.math.modeling.processor;

import com.jjoe64.graphview.series.Series;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import ru.didim99.tstu.core.math.common.PointRN;

/**
 * Created by didim99 on 08.05.20.
 */

public abstract class Processor {

  protected List<PointRN> series;

  protected Processor() {
    this.series = new ArrayList<>();
  }

  public abstract void process();
  public abstract Series<PointRN> getSeries();

  public String getDescription() {
    return describeSeries(series);
  }

  protected String describeSeries(List<PointRN> series) {
    StringBuilder sb = new StringBuilder();
    for (PointRN point : series)
      sb.append(String.format(Locale.US, "%7.4f %7.4f\n", point.getX(), point.getY()));
    return sb.toString();
  }
}
