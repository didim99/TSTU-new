package ru.didim99.tstu.core.knowledge.rulebase;

import com.google.gson.annotations.SerializedName;

/**
 * Created by didim99 on 02.03.21.
 */

public class Condition {
  @SerializedName("type")
  private Type type;
  @SerializedName("operation")
  private Operation operation;
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

  public enum Operation {
    @SerializedName("and")
    AND,
    @SerializedName("or")
    OR
  }
}
