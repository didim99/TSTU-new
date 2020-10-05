package ru.didim99.tstu.utils;

import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;
import java.lang.reflect.Array;
import java.util.ArrayList;
import ru.didim99.tstu.core.optimization.math.PointRN;

/**
 * Created by didim99 on 05.10.20.
 */

public class GraphUtils {
  private static final int MAX_SERIES_VISIBLE = 500;

  @SuppressWarnings("unchecked")
  public static <E extends PointRN> Series<E> buildSeries(ArrayList<E> data, String name) {
    LineGraphSeries<E> series = new LineGraphSeries<>();

    if (data.size() <= MAX_SERIES_VISIBLE) {
      E[] ref = (E[]) Array.newInstance(data.get(0).getClass(), 0);
      series.resetData(data.toArray(ref));
    } else {
      double step = data.size() / (double) MAX_SERIES_VISIBLE;
      for (int pos = 0; pos < MAX_SERIES_VISIBLE; pos++)
        series.appendData(data.get((int) Math.floor(pos * step)),
          false, MAX_SERIES_VISIBLE);
    }

    series.setTitle(name);
    return series;
  }
}
