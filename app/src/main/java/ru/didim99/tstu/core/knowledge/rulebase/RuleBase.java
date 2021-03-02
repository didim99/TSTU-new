package ru.didim99.tstu.core.knowledge.rulebase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by didim99 on 01.03.21.
 */

public class RuleBase {
  public static final String FILE_MASK = ".json";

  private List<Variable> variables;
  private List<Rule> rules;

  private List<Variable> facts;
  private Variable targetVariable;

  public RuleBase(RuleBaseConfig config) {
    this.facts = new ArrayList<>();
    this.variables = config.getVariables();
    this.rules = config.getRules();

    for (Variable var : variables) {
      if (var.getType() == Variable.Type.ENUM)
        var.setValues(config.getEnumValues(var.getName()));
    }
  }


}
