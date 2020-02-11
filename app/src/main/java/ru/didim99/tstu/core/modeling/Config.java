package ru.didim99.tstu.core.modeling;

/**
 * Created by didim99 on 10.02.20.
 */
public class Config {
  public static final class TaskType {
    public static final int UNDEFINED     = 0;
    public static final int STATIC_CURVE  = 1;
    public static final int DYNAMIC_CURVE = 2;
  }

  private int taskType;
  private int variable;

  public Config(int taskType) {
    this.taskType = taskType;
  }

  public void setVariable(int variable) {
    this.variable = variable;
  }

  int getTaskType() {
    return taskType;
  }

  int getVariable() {
    return variable;
  }
}