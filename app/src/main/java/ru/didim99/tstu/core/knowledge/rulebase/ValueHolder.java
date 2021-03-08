package ru.didim99.tstu.core.knowledge.rulebase;

/**
 * Created by didim99 on 08.03.21.
 */

interface ValueHolder {
  String getVariable();
  String getValue();

  default boolean equals(ValueHolder other) {
    return getVariable().equals(other.getVariable())
      && getValue().equals(other.getValue());
  }
}
