package ru.didim99.tstu.core.modeling.processor;

import ru.didim99.tstu.core.modeling.Functions;
import ru.didim99.tstu.core.optimization.math.FunctionRN;
import ru.didim99.tstu.core.optimization.math.PointD;

/**
 * Created by didim99 on 11.02.20.
 */
public class StaticProcessor extends VariableProcessor {

  private FunctionRN cOut;

  public StaticProcessor(int variableId) {
    super(variableId);
    this.cOut = Functions.cOutStatic;
  }

  @Override
  public void process() {
    PointD p = new PointD(Functions.vars.length);
    for (int i = 0; i < p.size(); i++) {
      if (i == variableId) p.set(i, var.getMinValue());
      else p.set(i, Functions.vars[i].getDefaultValue());
    }

    while (p.get(variableId) < var.getMaxValue()) {
      series.add(new PointD(p.get(variableId), cOut.f(p)));
      p.add(variableId, var.getStep());
    }
  }
}
