package ru.didim99.tstu.core.math.modeling;

import com.jjoe64.graphview.series.Series;
import java.util.ArrayList;
import ru.didim99.tstu.core.math.common.PointRN;

/**
 * Created by didim99 on 10.02.20.
 */
public class Result {
  private String description;
  private ArrayList<Series<PointRN>> seriesFamily;

  Result() {
    seriesFamily = new ArrayList<>();
  }

  public String getDescription() {
    return description;
  }

  public ArrayList<Series<PointRN>> getSeriesFamily() {
    return seriesFamily;
  }

  void setDescription(String description) {
    this.description = description;
  }

  void addSeries(Series<PointRN> series) {
    seriesFamily.add(series);
  }
}
