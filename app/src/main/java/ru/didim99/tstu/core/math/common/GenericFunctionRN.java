package ru.didim99.tstu.core.math.common;

/**
 * Generic multi-argument mathematical function
 * Created by didim99 on 02.10.19.
 */
@FunctionalInterface
public interface GenericFunctionRN<T extends PointRN> {
  double f(T p);
}
