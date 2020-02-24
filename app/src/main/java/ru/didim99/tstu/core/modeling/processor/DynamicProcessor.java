package ru.didim99.tstu.core.modeling.processor;

/**
 * Created by didim99 on 24.02.20.
 */
public class DynamicProcessor extends VariableProcessor {
  private static final double T_START = 0; // sec
  private static final double T_END = 500; // sec
  private static final double T_STEP = 0.1; // sec

  public DynamicProcessor(int variableId) {
    super(variableId);
  }

  @Override
  public void process() {
    
  }
}
