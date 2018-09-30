package ru.didim99.tstu.core.numeric;

import java.util.ArrayList;

/**
 * Created by didim99 on 16.09.18.
 */
public class Result {
  private Config config;
  private double solution;
  private ArrayList<Double> solutionSeries;
  private long solveTime;

  Result(Config config) {
    this.config = config;
  }

  public Config getConfig() {
    return config;
  }

  public double getSolution() {
    return solution;
  }

  public ArrayList<Double> getSolutionSeries() {
    return solutionSeries;
  }

  public long getSolveTime() {
    return solveTime;
  }

  void setSolution(double solution) {
    this.solution = solution;
  }

  void setSolutionSeries(ArrayList<Double> solutionSeries) {
    this.solutionSeries = solutionSeries;
  }

  void setSolveTime(long millis) {
    this.solveTime = millis;
  }

  //Transcendent solver specific
  private ArrayList<TranscendentSolver.StepEntry> solutionSteps;

  ArrayList<TranscendentSolver.StepEntry> getSolutionSteps() {
    return solutionSteps;
  }

  void setSolutionSteps(ArrayList<TranscendentSolver.StepEntry> solutionSteps) {
    this.solutionSteps = solutionSteps;
  }
}
