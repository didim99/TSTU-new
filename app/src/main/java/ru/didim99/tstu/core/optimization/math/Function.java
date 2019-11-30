package ru.didim99.tstu.core.optimization.math;

/**
 * Single-argument mathematical function
 * Created by didim99 on 24.10.19.
 */
@FunctionalInterface
public interface Function {
  double f(double x);
}
