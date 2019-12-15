package ru.didim99.tstu.core.graphics;

import java.util.ArrayList;
import ru.didim99.tstu.core.graphics.utils.Model;
import ru.didim99.tstu.core.graphics.utils.Projection;
import ru.didim99.tstu.core.graphics.utils.Texture;
import ru.didim99.tstu.core.graphics.utils.Vec4;

/**
 * Created by didim99 on 15.02.19.
 */
public class Config {

  static final class Type {
    static final int BITMAP = 1;
    static final int SCENE = 2;
  }

  // Global
  int width, height;
  int colorBg, colorFg;

  // LineRenderer
  boolean antiAlias;
  int angle;

  public int getAngle() {
    return angle;
  }

  public boolean useAntiAlias() {
    return antiAlias;
  }

  // HorizonRenderer
  double angleP, angleZ;
  int intAngleP, intAngleZ;
  double alpha, beta, gamma;

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

  // ModelRenderer
  int modelType;
  ArrayList<Model> models;
  Projection.Config prConfig;
  boolean drawAxis, negativeAxis;
  Vec4 translate, scale, rotate;
  boolean syncScale, useVNormals;
  Texture texture;
  // Lighting
  boolean useLamp;
  Vec4 lampPos;
  double kd;

  public boolean isDrawAxis() {
    return drawAxis;
  }

  public boolean isNegativeAxis() {
    return negativeAxis;
  }

  public Vec4 getTranslate() {
    return translate;
  }

  public Vec4 getScale() {
    return scale;
  }

  public Vec4 getRotate() {
    return rotate;
  }

  public boolean isSyncScale() {
    return syncScale;
  }

  public boolean isUseVNormals() {
    return useVNormals;
  }

  public boolean isUseLamp() {
    return useLamp;
  }

  public double getKd() {
    return kd;
  }

  public boolean hasTexture() {
    return texture != null;
  }

  // ModelRenderer
  int maxLevel, branchCount;
  boolean centralBranch;
  double branchL, branchAngle;
  double branchLF, branchAF;

  public boolean useCentralBranch() {
    return centralBranch;
  }

  public int getMaxLevel() {
    return maxLevel;
  }

  public int getBranchCount() {
    return branchCount;
  }

  public double getBranchL() {
    return branchL;
  }

  public double getBranchAngle() {
    return branchAngle;
  }

  public double getBranchLF() {
    return branchLF;
  }

  public double getBranchAF() {
    return branchAF;
  }
}
