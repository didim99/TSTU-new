package ru.didim99.tstu.core.numeric;

import com.jjoe64.graphview.series.DataPoint;
import java.util.ArrayList;
import ru.didim99.tstu.core.optimization.math.PointRN;

/**
 * Created by didim99 on 16.09.18.
 */
public class Result {
  private Config config;
  private double solution;
  private ArrayList<Double> solutionSeries;
  private ArrayList<PointRN> graphData;
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

  public ArrayList<PointRN> getGraphData() {
    return graphData;
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

  void setGraphData(ArrayList<PointRN> graphData) {
    this.graphData = graphData;
  }

  //Transcendent solver specific
  private ArrayList<TranscendentSolver.StepEntry> solutionSteps;

  ArrayList<TranscendentSolver.StepEntry> getSolutionSteps() {
    return solutionSteps;
  }

  void setSolutionSteps(ArrayList<TranscendentSolver.StepEntry> solutionSteps) {
    this.solutionSteps = solutionSteps;
  }

  //Linear system solver specific
  private ArrayList<Matrix> matrixSeries;

  public ArrayList<Matrix> getMatrixSeries() {
    return matrixSeries;
  }

  void setMatrixSeries(ArrayList<Matrix> matrixSeries) {
    this.matrixSeries = matrixSeries;
  }

  //Interpolator specific
  private ArrayList<ArrayList<DataPoint>> graphDataSeries;
  private String polynom;

  public ArrayList<ArrayList<DataPoint>> getGraphDataSeries() {
    return graphDataSeries;
  }

  public void setGraphDataSeries(ArrayList<ArrayList<DataPoint>> graphDataSeries) {
    this.graphDataSeries = graphDataSeries;
  }

  public String getPolynom() {
    return polynom;
  }

  public void setPolynom(String polynom) {
    this.polynom = polynom;
  }
}
