package ru.didim99.tstu.core.knowledge.rulebase;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by didim99 on 02.03.21.
 */

public class Condition implements ValueHolder {
  public enum Status { TRUE, FALSE, UNKNOWN }

  @SerializedName("type")
  private Type type;
  @SerializedName("variable")
  private String variable;
  @SerializedName("value")
  private String value;

  public enum Type {
    @SerializedName("lt")
    LT,
    @SerializedName("le")
    LE,
    @SerializedName("eq")
    EQ,
    @SerializedName("ge")
    GE,
    @SerializedName("gt")
    GT
  }

  public Condition(ValueHolder source) {
    this.variable = source.getVariable();
    this.value = source.getValue();
    this.type = Type.EQ;
  }

  @Override
  public String getVariable() {
    return variable;
  }

  @Override
  public String getValue() {
    return value;
  }

  public Status check(List<Variable> facts) {
    for (Variable fact : facts) {
      if (variable.equals(fact.getName())) {
        if (type == Type.EQ) {
          return value.equals(fact.getValue()) ?
            Status.TRUE : Status.FALSE;
        }

        if (fact.getType() != Variable.Type.FLOAT)
          throw new IllegalArgumentException("Unsupported comparision "
            + type + " for type: " + fact.getType());
        return checkFloat(fact) ? Status.TRUE : Status.FALSE;
      }
    }

    return Status.UNKNOWN;
  }

  private boolean checkFloat(Variable fact) {
    float actual = Float.parseFloat(fact.getValue());
    float expected = Float.parseFloat(value);

    switch (type) {
      case LT:
        return actual < expected;
      case LE:
        return actual <= expected;
      case GE:
        return actual >= expected;
      case GT:
        return actual > expected;
    }

    return false;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('\'').append(variable).append("' ");

    switch (type) {
      case LT: sb.append('<'); break;
      case LE: sb.append("<="); break;
      case EQ: sb.append('='); break;
      case GE: sb.append(">="); break;
      case GT: sb.append('>'); break;
    }

    sb.append(' ').append(value);
    return sb.toString();
  }
}
