package ru.didim99.tstu.core.math.modeling.poxipol;

import com.jjoe64.graphview.series.Series;
import ru.didim99.tstu.core.math.common.PointRN;

/**
 * Created by didim99 on 05.10.20.
 */

public class Result {
  private Series<PointRN> series;
  private String description;
  private long time;

  public Series<PointRN> getSeries() {
    return series;
  }

  public String getDescription() {
    return description;
  }

  public long getTime() {
    return time;
  }

  public void setSeries(Series<PointRN> series) {
    this.series = series;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setTime(long time) {
    this.time = time;
  }
}
