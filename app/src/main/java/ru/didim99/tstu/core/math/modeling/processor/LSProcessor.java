package ru.didim99.tstu.core.math.modeling.processor;

import ru.didim99.tstu.core.math.common.FunctionRN;
import ru.didim99.tstu.core.math.common.PointD;
import ru.didim99.tstu.core.math.modeling.Functions;

/**
 * Lumped system static mode processor
 *
 * Created by didim99 on 11.02.20.
 */
public class LSProcessor extends VariableProcessor {

  private FunctionRN cOut;

  public LSProcessor(int variableId) {
    super(variableId);
    this.cOut = Functions.cOutStatic;
  }

  @Override
  public void process() {
    PointD p = new PointD(Functions.lumpedVars.length);
    for (int i = 0; i < p.size(); i++) {
      if (i == variableId) p.set(i, var.getMinValue());
      else p.set(i, Functions.lumpedVars[i].getDefaultValue());
    }

    while (p.get(variableId) < var.getMaxValue()) {
      series.add(new PointD(p.get(variableId), cOut.f(p)));
      p.add(variableId, var.getStep());
    }
  }
}
