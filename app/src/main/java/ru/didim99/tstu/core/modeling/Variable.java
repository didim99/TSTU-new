package ru.didim99.tstu.core.modeling;

/**
 * Created by didim99 on 24.02.20.
 */
public class Variable {
  private String name;
  private double defaultValue;
  private double minValue, maxValue;
  private double step;
  private double delta;

  Variable(String name, double defaultValue, double minValue,
           double maxValue, double step, double delta) {
    this.name = name;
    this.defaultValue = defaultValue;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.step = step;
    this.delta = delta;
  }

  public String getName() {
    return name;
  }

  public double getDefaultValue() {
    return defaultValue;
  }

  public double getMinValue() {
    return minValue;
  }

  public double getMaxValue() {
    return maxValue;
  }

  public double getStep() {
    return step;
  }

  public double getDelta() {
    return delta;
  }
}
