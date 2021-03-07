package ru.didim99.tstu.core.knowledge.rulebase;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by didim99 on 02.03.21.
 */

public class Rule {
  public enum Status { TRUE, FALSE, PARTIALLY, UNKNOWN }

  @SerializedName("conditions")
  private List<Condition> conditions;
  @SerializedName("target")
  private List<RuleTarget> target;

  private final List<String> unknown;
  private Status status;

  public Rule() {
    unknown = new ArrayList<>();
  }

  public List<Condition> getConditions() {
    return conditions;
  }

  public List<RuleTarget> getTarget() {
    return target;
  }

  public Status getStatus() {
    return status;
  }

  public List<String> getUnknown() {
    return unknown;
  }

  public boolean isAlreadyChecked() {
    return status == Status.FALSE || status == Status.TRUE;
  }

  public Status check(List<Variable> facts) {
    boolean allUnknown = true;
    boolean allKnown = true;
    boolean allTrue = true;
    reset();

    for (Condition c : conditions) {
      switch (c.check(facts)) {
        case UNKNOWN:
          unknown.add(c.getVariable());
          allKnown = false;
          break;
        case FALSE:
          allUnknown = false;
          allTrue = false;
          break;
        case TRUE:
          allUnknown = false;
          break;
      }
    }

    if (allUnknown)
      return setStatus(Status.UNKNOWN);
    if (!allTrue)
      return setStatus(Status.FALSE);
    if (!allKnown)
      return setStatus(Status.PARTIALLY);
    else return setStatus(Status.TRUE);
  }

  public void reset() {
    unknown.clear();
    status = null;
  }

  private Status setStatus(Status status) {
    this.status = status;
    return status;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("IF ");
    for (Condition c : conditions)
      sb.append(c).append(' ');
    sb.append("THEN ");
    for (RuleTarget t : target)
      sb.append(t).append(' ');
    return sb.toString().trim();
  }
}
