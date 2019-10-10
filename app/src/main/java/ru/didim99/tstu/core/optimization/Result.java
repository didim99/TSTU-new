package ru.didim99.tstu.core.optimization;

import android.graphics.Bitmap;
import java.util.ArrayList;

/**
 * Created by didim99 on 05.09.19.
 */
public class Result {
  private int method;
  private long solutionTime;
  private PointD solution;
  private ArrayList<ExtremaFinder.StepEntry> solutionSteps;
  private int fCalcCount, tFCalcCount;

  int getMethod() {
    return method;
  }

  long getSolutionTime() {
    return solutionTime;
  }

  PointD getSolution() {
    return solution;
  }

  ArrayList<ExtremaFinder.StepEntry> getSolutionSteps() {
    return solutionSteps;
  }

  int getFCalcCount() {
    return fCalcCount;
  }

  int getTFCalcCount() {
    return tFCalcCount;
  }

  void setMethod(int method) {
    this.method = method;
  }

  void setSolutionTime(long solutionTime) {
    this.solutionTime = solutionTime;
  }

  void setSolution(PointD solution) {
    this.solution = solution;
  }

  void setSolutionSteps(ArrayList<ExtremaFinder.StepEntry> solutionSteps) {
    this.solutionSteps = solutionSteps;
  }

  void setFCalcCount(int fCalcCount) {
    this.fCalcCount = fCalcCount;
  }

  void setTFCalcCount(int tFCalcCount) {
    this.tFCalcCount = tFCalcCount;
  }

  private Bitmap bitmap;

  public Bitmap getBitmap() {
    return bitmap;
  }

  public void setBitmap(Bitmap bitmap) {
    this.bitmap = bitmap;
  }
}
