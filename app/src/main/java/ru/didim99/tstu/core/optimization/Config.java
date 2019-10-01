package ru.didim99.tstu.core.optimization;

/**
 * Created by didim99 on 26.09.19.
 */
public class Config {
  public static final class TaskType {
    public static final int UNDEFINED   = 0;
    public static final int SINGLE_ARG  = 1;
    public static final int ZERO_ORDER  = 2;
  }

  private int taskType;

  public Config(int taskType) {
    this.taskType = taskType;
  }

  int getTaskType() {
    return taskType;
  }
}