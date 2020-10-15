package ru.didim99.tstu.core.math.optimization;

import android.graphics.Bitmap;
import java.util.ArrayList;
import ru.didim99.tstu.core.math.common.PointD;

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
  private String description;

  public Bitmap getBitmap() {
    return bitmap;
  }

  public String getDescription() {
    return description;
  }

  void setBitmap(Bitmap bitmap) {
    this.bitmap = bitmap;
  }

  void setDescription(String description) {
    this.description = description;
  }

  private ArrayList<PointD> solutionSeries;
  private ArrayList<PointD> reference;
  private ArrayList<PointD> delta;

  public ArrayList<PointD> getSolutionSeries() {
    return solutionSeries;
  }

  public ArrayList<PointD> getReference() {
    return reference;
  }

  public ArrayList<PointD> getDelta() {
    return delta;
  }

  void setSolutionSeries(ArrayList<PointD> solutionSeries) {
    this.solutionSeries = solutionSeries;
  }

  void setReference(ArrayList<PointD> reference) {
    this.reference = reference;
  }

  void setDelta(ArrayList<PointD> delta) {
    this.delta = delta;
  }
}
