package ru.didim99.tstu.core.optimization.math;

import com.jjoe64.graphview.series.DataPointInterface;

/**
 * Created by didim99 on 16.03.20.
 */
public interface PointRN extends DataPointInterface {

  double get(int pos);
  void set(int pos, double val);

  @Override
  default double getX() { return get(0); }

  @Override
  default double getY() { return get(1); }
}
