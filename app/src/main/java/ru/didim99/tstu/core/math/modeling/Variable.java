package ru.didim99.tstu.core.math.modeling;

/**
 * Created by didim99 on 24.02.20.
 */
public class Variable {
  private final String name;
  private final double defaultValue;
  private final double minValue, maxValue;
  private final double step;
  private final double delta;

  Variable(String name, double minValue, double maxValue, double step) {
    this(name, 0, minValue, maxValue, step, 0);
  }

  Variable(String name, double defaultValue,
           double minValue, double maxValue, double step) {
    this(name, defaultValue, minValue, maxValue, step, 0);
  }

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

  public boolean hasDelta() {
    return maxValue > minValue;
  }
}
