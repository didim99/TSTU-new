package ru.didim99.tstu.core.graphics.utils;

import android.graphics.PointF;

/**
 * Created by didim99 on 10.03.19.
 */
public class Projection {
  private static final int DEFAULT_TYPE = Type.PARALLEL;
  private static final int DEFAULT_PPA = 63;
  private static final double DEFAULT_PPF = 0.5;
  private static final int DEFAULT_CPD = 1500;
  public static final int PPA_MIN = 0;
  public static final int PPA_MAX = 90;
  public static final int PPF_MIN = 25;
  public static final int PPF_MAX = 100;
  public static final int CPD_MIN = 100;
  public static final int CPD_MAX = 5000;
  public static final double PPF_FACTOR = 0.01;

  public static final class Type {
    public static final int PARALLEL  = 0;
    public static final int CENTRAL   = 1;
  }

  public static final class Config {
    private int type;
    private int ppAngle;
    private double ppFactor;
    private double sinP, cosP;
    private int cpDistance;

    private Config() {}
  }

  private Config config;
  private OnChangeListener listener;
  private Projector projector;

  public Projection(Config config, OnChangeListener listener) {
    if (config == null) {
      this.config = new Config();
      this.config.type = DEFAULT_TYPE;
      this.config.ppAngle = DEFAULT_PPA;
      this.config.ppFactor = DEFAULT_PPF;
      this.config.cpDistance = DEFAULT_CPD;
    } else this.config = config;
    applyType(this.config.type);
    this.listener = listener;
  }

  public Config getConfig() {
    return config;
  }

  public int getType() { return config.type; }

  public int getPPAngle() {
    return config.ppAngle;
  }

  public double getPPFactor() {
    return config.ppFactor;
  }

  public int getCPDistance() {
    return config.cpDistance;
  }

  public void setPPAngle(int angle) {
    config.ppAngle = angle;
    compute();
  }

  public void setPPFacror(double factor) {
    config.ppFactor = factor;
    compute();
  }

  public void setCPDistance(int distance) {
    config.cpDistance = distance;
    compute();
  }

  public void applyType(int type) {
    switch (this.config.type = type) {
      case Type.CENTRAL:  projector = this::projectCentral; break;
      case Type.PARALLEL: projector = this::projectParallel; break;
    }
    compute();
  }

  private void compute() {
    config.sinP = Math.sin(Math.toRadians(config.ppAngle)) * config.ppFactor;
    config.cosP = Math.cos(Math.toRadians(config.ppAngle)) * config.ppFactor;
    if (listener != null) listener.OnProjectionChanged();
  }

  PointF project(Vec4 vec) {
    return projector.project(vec);
  }

  private PointF projectParallel(Vec4 vec) {
    return new PointF(
      (float) (vec.x() - vec.z() * config.cosP),
      (float) (vec.y() - vec.z() * config.sinP));
  }

  private PointF projectCentral(Vec4 vec) {
    double f = 1
      + vec.z() / config.cpDistance
      /*+ vec.x() / config.cpDistance*/
      /*- vec.y() / config.cpDistance*/;
    return new PointF(
      (float) (vec.x() / f),
      (float) (vec.y() / f));
  }

  @FunctionalInterface
  public interface OnChangeListener {
    void OnProjectionChanged();
  }

  @FunctionalInterface
  private interface Projector {
    PointF project(Vec4 vec);
  }
}
