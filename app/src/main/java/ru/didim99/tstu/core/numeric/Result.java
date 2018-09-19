package ru.didim99.tstu.core.numeric;

import java.util.ArrayList;

/**
 * Created by didim99 on 16.09.18.
 */
public class Result {
  private double solution;
  private ArrayList<Double> solutionSeries;

  public double getSolution() {
    return solution;
  }

  public ArrayList<Double> getSolutionSeries() {
    return solutionSeries;
  }

  public void setSolution(double solution) {
    this.solution = solution;
  }

  public void setSolutionSeries(ArrayList<Double> solutionSeries) {
    this.solutionSeries = solutionSeries;
  }
}
