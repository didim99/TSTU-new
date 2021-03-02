package ru.didim99.tstu.core.knowledge.rulebase;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

/**
 * Created by didim99 on 02.03.21.
 */

public class RuleBaseConfig {
  @SerializedName("variables")
  private List<Variable> variables;
  @SerializedName("enumValues")
  private Map<String, List<String>> enumValues;
  @SerializedName("rules")
  private List<Rule> rules;

  public List<Variable> getVariables() {
    return variables;
  }

  public List<Rule> getRules() {
    return rules;
  }

  public List<String> getEnumValues(String varName) {
    return enumValues.get(varName);
  }
}
