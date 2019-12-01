package ru.didim99.tstu.core.optimization.math;

/**
 * Created by didim99 on 01.12.19.
 */
@FunctionalInterface
public interface MultiFunction {
  double f(double t, Function... x);
}
