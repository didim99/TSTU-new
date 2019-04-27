package ru.didim99.tstu.ui.os.scheduler;

import android.content.res.Resources;
import android.support.annotation.ColorRes;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * {@link GraphView} wrapper for realtime data logging
 * with fixed time interval and log size
 * Created by didim99 on 23.04.19.
 */
class GraphLogWrapper {
  private Resources res;
  private LineGraphSeries<DataPoint> series;
  private Viewport viewport;
  private int bufferSize;
  private int dataSize;

  GraphLogWrapper(GraphView view, int logSize) {
    bufferSize = logSize;
    res = view.getResources();
    series = new LineGraphSeries<>();
    series.setDrawDataPoints(false);
    series.setDrawBackground(true);
    viewport = view.getViewport();
    viewport.setXAxisBoundsManual(true);
    viewport.setYAxisBoundsManual(true);
    view.addSeries(series);
    clear();
  }

  void setBoundsY(int min, int max) {
    viewport.setMaxY(max);
    viewport.setMinY(min);
  }

  void setColors(@ColorRes int fg, @ColorRes int bg) {
    series.setBackgroundColor(res.getColor(bg));
    series.setColor(res.getColor(fg));
  }

  void append(int value) {
    int pos = dataSize + bufferSize;
    DataPoint p = new DataPoint(pos, value);
    series.appendData(p, false, bufferSize + 1);
    viewport.setMinX(dataSize++);
    viewport.setMaxX(pos);
  }

  void clear() {
    series.resetData(new DataPoint[0]);
    dataSize = 0;
    append(0);
  }
}
