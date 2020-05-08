package ru.didim99.tstu.core.modeling;

/**
 * Created by didim99 on 10.02.20.
 */
public class Config {
  public static final class TaskType {
    public static final int UNDEFINED             = 0;
    public static final int LUMPED_STATIC_CURVE   = 1;
    public static final int LUMPED_DYNAMIC_CURVE  = 2;
    public static final int DIST_STATIC_CURVES    = 3;
    public static final int RANDOM_PROCESSING     = 4;
  }

  private int taskType;
  private int variable;
  private boolean optimize;

  public Config(int taskType) {
    this.taskType = taskType;
  }

  public void setVariable(int variable) {
    this.variable = variable;
  }

  public void setOptimize(boolean optimize) {
    this.optimize = optimize;
  }

  int getTaskType() {
    return taskType;
  }

  int getVariable() {
    return variable;
  }

  boolean isOptimize() {
    return optimize;
  }
}