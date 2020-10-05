package ru.didim99.tstu.core.modeling;

import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;
import java.util.ArrayList;
import ru.didim99.tstu.core.optimization.math.PointRN;

/**
 * Created by didim99 on 05.10.20.
 */

public class GraphUtils {
  private static final int MAX_SERIES_VISIBLE = 500;

  public static Series<PointRN> buildSeries(ArrayList<PointRN> data, String name) {
    LineGraphSeries<PointRN> series = new LineGraphSeries<>();

    if (data.size() <= MAX_SERIES_VISIBLE)
      series.resetData(data.toArray(new PointRN[0]));
    else {
      double step = data.size() / (double) MAX_SERIES_VISIBLE;
      for (int pos = 0; pos < MAX_SERIES_VISIBLE; pos++)
        series.appendData(data.get((int) Math.floor(pos * step)),
          false, MAX_SERIES_VISIBLE);
    }

    series.setTitle(name);
    return series;
  }
}
