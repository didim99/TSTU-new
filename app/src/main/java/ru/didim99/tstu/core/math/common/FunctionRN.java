package ru.didim99.tstu.core.math.common;

/**
 * Basic multi-argument mathematical function
 * Created by didim99 on 02.10.19.
 */
@FunctionalInterface
public interface FunctionRN extends GenericFunctionRN<PointRN> {
  @Override double f(PointRN p);
}
