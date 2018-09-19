package ru.didim99.tstu.core.numeric;

/**
 * Created by didim99 on 16.09.18.
 */
public class Config {

  public static final class TaskType {
    public static final int UNDEFINED = 0;
    public static final int TRANSCENDENT = 1;
  }

  private int taskType;
  private int solveMethod;
  private boolean tConst;
  private Double xStart, xEnd;

  public boolean isRangeDefined() {
    return xStart != null && xEnd != null;
  }

  public int getTaskType() { return taskType; }
  public int getSolveMethod() { return solveMethod; }
  public boolean isTConst() { return tConst; }
  public Double getXStart() { return xStart; }
  public Double getXEnd() { return xEnd; }

  public void setRange(double xStart, double xEnd) {
    this.xStart = xStart;
    this.xEnd = xEnd;
  }

  public static class Builder {
    private Config config;

    public Builder() {
      config = new Config();
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

    public Config build() {
      return config;
    }
  }
}
