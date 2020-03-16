package ru.didim99.tstu.core.modeling;

import com.jjoe64.graphview.series.Series;
import ru.didim99.tstu.core.optimization.math.PointRN;

/**
 * Created by didim99 on 10.02.20.
 */
public class Result {
  private String description;
  private Series<PointRN> series;

  public String getDescription() {
    return description;
  }

  public Series<PointRN> getSeries() {
    return series;
  }

  void setDescription(String description) {
    this.description = description;
  }

  void setSeries(Series<PointRN> series) {
    this.series = series;
  }
}
