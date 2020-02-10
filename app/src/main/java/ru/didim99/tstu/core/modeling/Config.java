package ru.didim99.tstu.core.modeling;

/**
 * Created by didim99 on 10.02.20.
 */
public class Config {
  public static final class TaskType {
    public static final int STATIC_CURVE  = 0;
    public static final int DYNAMIC_CURVE = 1;
  }

  private int taskType;

  public Config(int taskType) {
    this.taskType = taskType;
  }

  int getTaskType() {
    return taskType;
  }
}