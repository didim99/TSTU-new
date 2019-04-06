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
  boolean syncScale;
  Texture texture;

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
}
