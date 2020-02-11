package ru.didim99.tstu.core.modeling;

import com.jjoe64.graphview.series.Series;
import ru.didim99.tstu.core.optimization.math.PointD;

/**
 * Created by didim99 on 10.02.20.
 */
public class Result {
  private String description;
  private Series<PointD> series;

  public String getDescription() {
    return description;
  }

  public Series<PointD> getSeries() {
    return series;
  }

  void setDescription(String description) {
    this.description = description;
  }

  void setSeries(Series<PointD> series) {
    this.series = series;
  }
}
