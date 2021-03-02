package ru.didim99.tstu.core.knowledge.rulebase;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by didim99 on 02.03.21.
 */

public class Rule {
  @SerializedName("conditions")
  private List<Condition> conditions;
  @SerializedName("target")
  private List<RuleTarget> target;
}
