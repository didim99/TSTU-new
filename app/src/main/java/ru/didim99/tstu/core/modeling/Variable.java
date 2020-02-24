package ru.didim99.tstu.core.modeling;

/**
 * Created by didim99 on 24.02.20.
 */
class Variable {
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

  String getName() {
    return name;
  }

  double getDefaultValue() {
    return defaultValue;
  }

  double getMinValue() {
    return minValue;
  }

  double getMaxValue() {
    return maxValue;
  }

  double getStep() {
    return step;
  }

  double getDelta() {
    return delta;
  }
}
