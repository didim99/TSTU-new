package ru.didim99.tstu.core.optimization;

/**
 * Created by didim99 on 26.09.19.
 */
public class Config {
  public static final class TaskType {
    public static final int UNDEFINED   = 0;
    public static final int SINGLE_ARG  = 1;
    public static final int MULTI_ARG   = 2;
  }

  public static final class Method {
    public static final int PAUL      = 0;
    public static final int SIMPLEX   = 1;
    public static final int FDESCENT  = 2;
    public static final int GRADIENT  = 3;
  }

  public static final class FunctionType {
    public static final int PARABOLA  = 0;
    public static final int PARABOLA2 = 1;
    public static final int RESENBROK = 2;
  }

  public static final class FineType {
    public static final int INTERNAL  = 0;
    public static final int EXTERNAL  = 1;
  }

  private int taskType;
  private int method, function;
  private boolean useLimits;
  private int limitMethod;

  public Config(int taskType) {
    this.taskType = taskType;
  }

  public void setMethod(int method) {
    this.method = method;
  }

  public void setFunction(int function) {
    this.function = function;
  }

  public void setUseLimits(boolean useLimits) {
    this.useLimits = useLimits;
  }

  public void setLimitMethod(int limitMethod) {
    this.limitMethod = limitMethod;
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

  boolean isUseLimits() {
    return useLimits;
  }

  int getLimitMethod() {
    return limitMethod;
  }
}