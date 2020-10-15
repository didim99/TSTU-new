package ru.didim99.tstu.core.math.modeling.processor;

import com.jjoe64.graphview.series.Series;
import ru.didim99.tstu.core.math.common.PointRN;
import ru.didim99.tstu.core.math.modeling.Functions;
import ru.didim99.tstu.core.math.modeling.Variable;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 24.02.20.
 */
abstract class VariableProcessor extends Processor {

  int variableId;
  Variable var;

  VariableProcessor(int variableId) {
    this.variableId = variableId;
    this.var = Functions.lumpedVars[variableId];
  }

  @Override
  public Series<PointRN> getSeries() {
    return Utils.buildSeries(this.series, var.getName());
  }
}
