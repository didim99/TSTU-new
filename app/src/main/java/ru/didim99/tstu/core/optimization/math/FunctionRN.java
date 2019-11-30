package ru.didim99.tstu.core.optimization.math;

/**
 * Multi-argument mathematical function
 * Created by didim99 on 02.10.19.
 */
@FunctionalInterface
public interface FunctionRN {
  double f(PointD p);
}
