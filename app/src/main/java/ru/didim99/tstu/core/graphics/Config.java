package ru.didim99.tstu.core.graphics;

/**
 * Created by didim99 on 15.02.19.
 */
public class Config {
  // Global
  int width, height;
  int colorBg, colorFg;

  // LineRenderer
  boolean antiAlias;
  int angle;

  // HorizonRenderer
  double angleP, angleZ;
  int intAngleP, intAngleZ;
  double alpha, beta, gamma;

  public int getAngle() {
    return angle;
  }

  public int getAngleP() {
    return intAngleP;
  }

  public int getAngleZ() {
    return intAngleZ;
  }

  public double getAlpha() {
    return alpha;
  }

  public double getBeta() {
    return beta;
  }

  public double getGamma() {
    return gamma;
  }
}
