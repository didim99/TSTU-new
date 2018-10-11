package ru.didim99.tstu.core.numeric;

/**
 * Created by didim99 on 16.09.18.
 */
public class Config {

  public static final class TaskType {
    public static final int UNDEFINED     = 0;
    public static final int TRANSCENDENT  = 1;
    public static final int LINEAR_SYSTEM = 2;
  }

  private int taskType;
  private int solveMethod;
  private boolean tConst;
  private Double xStart, xEnd;
  private String fileName;
  private int size;

  int getTaskType() { return taskType; }
  int getSolveMethod() { return solveMethod; }
  boolean isTConst() { return tConst; }
  Double getXStart() { return xStart; }
  Double getXEnd() { return xEnd; }
  String getFileName() { return fileName; }
  int getSize() { return size; }

  boolean isRangeDefined() {
    return xStart != null && xEnd != null;
  }

  void setRange(double xStart, double xEnd) {
    this.xStart = xStart;
    this.xEnd = xEnd;
  }

  void setSize(int size) {
    this.size = size;
  }

  public static class Builder {
    private Config config;

    public Builder() {
      config = new Config();
    }

    public Builder(Config other) {
      config = new Config();
      config.taskType = other.taskType;
      config.solveMethod = other.solveMethod;
      config.tConst = other.tConst;
      config.xStart = other.xStart;
      config.xEnd = other.xEnd;
      config.fileName = other.fileName;
      config.size = other.size;
    }

    public Builder taskType(int taskType) {
      config.taskType = taskType;
      return this;
    }

    public Builder solveMethod(int solveMethod) {
      config.solveMethod = solveMethod;
      return this;
    }

    public Builder tConst(boolean tConst) {
      config.tConst = tConst;
      return this;
    }

    public Builder range(double xStart, double xEnd) {
      config.xStart = xStart;
      config.xEnd = xEnd;
      return this;
    }

    public Builder fileName(String fileName) {
      config.fileName = fileName;
      return this;
    }

    public Config build() {
      return config;
    }
  }
}
