package ru.didim99.tstu.utils;

/**
 * Created by didim99 on 19.10.20.
 */
@FunctionalInterface
public interface Supplier<T> {
  T nextElement();
}
