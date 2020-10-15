package ru.didim99.tstu.core.math.modeling.processor;

import com.jjoe64.graphview.series.Series;
import java.util.ArrayList;
import ru.didim99.tstu.core.math.common.PointRN;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 08.05.20.
 */
public abstract class MultiSeriesProcessor extends Processor {

  protected ArrayList<ArrayList<PointRN>> seriesFamily;
  protected ArrayList<String> seriesNames;
  private int seriesIndex;

  protected MultiSeriesProcessor() {
    seriesFamily = new ArrayList<>();
    seriesNames = new ArrayList<>();
    seriesIndex = -1;
  }

  @Override
  public Series<PointRN> getSeries() {
    if (++seriesIndex < seriesFamily.size()) {
      return Utils.buildSeries(seriesFamily.get(seriesIndex),
        seriesNames.get(seriesIndex));
    } else return null;
  }

  @Override
  public String getDescription() {
    return describeSeries(seriesFamily.get(0));
  }
}
