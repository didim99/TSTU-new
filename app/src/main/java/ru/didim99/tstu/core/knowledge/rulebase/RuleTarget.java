package ru.didim99.tstu.core.knowledge.rulebase;

import com.google.gson.annotations.SerializedName;

/**
 * Created by didim99 on 02.03.21.
 */

public class RuleTarget {
  @SerializedName("variable")
  private String variable;
  @SerializedName("value")
  private String value;
}
