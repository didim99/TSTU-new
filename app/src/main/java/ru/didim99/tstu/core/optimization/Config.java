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

  public static final class Method {
    public static final int PAUL      = 0;
    public static final int SIMPLEX   = 1;
    public static final int FDESCENT  = 2;
    public static final int GRADIENT  = 3;
  }

  public static final class Function {
    public static final int PARABOLA  = 0;
    public static final int RESENBROK = 1;
  }

  private int taskType;
  private int method, function;

  public Config(int taskType) {
    this.taskType = taskType;
  }

  public void setMethod(int method) {
    this.method = method;
  }

  public void setFunction(int function) {
    this.function = function;
  }

  int getTaskType() {
    return taskType;
  }

  int getMethod() {
    return method;
  }

  int getFunction() {
    return function;
  }
}