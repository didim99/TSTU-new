package ru.didim99.tstu.core.graphics;

import java.util.Arrays;
import ru.didim99.tstu.ui.graphics.view.DrawerView;

/**
 * Created by didim99 on 24.02.19.
 */
public class HorizonRenderer extends AsyncRenderer {
  // Minimum & maximum parameters values
  public static final int PROJ_MIN = -20;
  public static final int PROJ_MAX = 20;
  public static final int PITCH_MIN = -60;
  public static final int PITCH_MAX = 60;
  public static final int ALPHA_MIN = -100;
  public static final int ALPHA_MAX = 100;
  public static final int BETA_MIN = -30;
  public static final int BETA_MAX = 30;
  public static final int GAMMA_MIN = 0;
  public static final int GAMMA_MAX = 1000;
  public static final double A_FACTOR = 0.01;
  public static final double B_FACTOR = 0.1;
  public static final double G_FACTOR = 0.01;
  // Default parameters values
  private static final int DEFAULT_PROJ = 10;
  private static final int DEFAULT_PITCH = 20;
  private static final int DEFAULT_ALPHA = 20;
  private static final int DEFAULT_BETA = 15;
  private static final int DEFAULT_GAMMA = 175;
  // Internal constants
  private static final int COLOR_TOP = 0xff041048;
  private static final int COLOR_BOTTOM = 0xff483404;
  private static final int FPS = 50;
  private static final int VP_WIDTH = 700;
  private static final int VP_HEIGHT = 500;
  private static final int VP_F_RES = 500;
  private static final int X_RES = 400;
  private static final int Z_RES = 50;
  private static final double F_MIN = 0.0;
  private static final double F_MAX = 2 * Math.PI;
  private static final double X_STEP = (F_MAX - F_MIN) / X_RES;
  private static final double Z_STEP = (F_MAX - F_MIN) / Z_RES;
  private static final double VP_X_FACTOR = VP_F_RES / (F_MAX - F_MIN);
  private static final double VP_Y_FACTOR = VP_F_RES / (F_MAX - F_MIN);
  private static final double VP_Z_FACTOR = VP_F_RES / (F_MAX - F_MIN);
  private static final int ANIM_Z_FACTOR = 8;

  private int[] minHorizon;
  private int[] maxHorizon;
  private double xFactor, yFactor;
  private int offsetX, offsetY;
  private double zi;

  public HorizonRenderer(DrawerView target, Config config) {
    super(target, Config.Type.BITMAP, config,
      VP_WIDTH, VP_HEIGHT, true);
    minHorizon = new int[VP_WIDTH];
    maxHorizon = new int[VP_WIDTH];
    setFPS(FPS);

    if (config == null) {
      this.config.intAngleZ = DEFAULT_PROJ;
      this.config.intAngleP = DEFAULT_PITCH;
      this.config.alpha = DEFAULT_ALPHA * A_FACTOR;
      this.config.beta = DEFAULT_BETA * B_FACTOR;
      this.config.gamma = DEFAULT_GAMMA * G_FACTOR;
    }
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    if (!animating) draw();
  }

  @Override
  public void pause(boolean paused) {
    if (animating) super.pause(paused);
  }

  @Override
  public void animate() {
    super.animate();
    prepareDraw();
    zi = F_MIN;
  }

  @Override
  public void frame() {
    if (zi < F_MAX) {
      drawLayer(zi);
      zi += Z_STEP / ANIM_Z_FACTOR;
    } else {
      pause(true);
      animating = false;
    }
  }

  private void prepareDraw() {
    clear();
    Arrays.fill(maxHorizon, 0);
    Arrays.fill(minHorizon, VP_HEIGHT);
    config.angleP = Math.toRadians(config.intAngleP);
    config.angleZ = Math.toRadians(90 + config.intAngleZ);
    xFactor = Math.cos(config.angleZ) * VP_Z_FACTOR;
    yFactor = Math.sin(config.angleP) * VP_Z_FACTOR;
    double offsetZ = Math.cos(config.angleZ) * F_MAX * VP_Z_FACTOR;
    offsetX = (int) ((VP_WIDTH - VP_F_RES - offsetZ) / 2);
    double offsetP = Math.sin(config.angleP) * F_MAX * VP_Z_FACTOR;
    offsetY = (int) ((VP_HEIGHT - offsetP) / 2);
  }

  private void draw() {
    prepareDraw();
    double z = F_MIN;
    for (int i = 0; i < Z_RES; i++) {
      drawLayer(z);
      z += Z_STEP;
    }
  }

  private void drawLayer(double z) {
    int offsetX = (int) (z * xFactor);
    int offsetY = (int) (z * yFactor);

    double x = F_MIN;
    for (int i = 0; i < X_RES; i++) {
      setPixel(x(x) + offsetX, y(f(x, z)) + offsetY);
      x += X_STEP;
    }
  }

  private void setPixel(int x, int y) {
    if (x >= 0 && x < VP_WIDTH && y >= 0 && y < VP_HEIGHT) {
      boolean draw = false;
      int color = config.colorFg;
      if (y < minHorizon[x]) { minHorizon[x] = y; color = COLOR_BOTTOM; draw = true; }
      if (y > maxHorizon[x]) { maxHorizon[x] = y; color = COLOR_TOP; draw = true; }
      if (draw) bitmap.setPixel(x, VP_HEIGHT - (y + 1), color);
    }
  }

  private int x(double x) {
    return (int) (x * VP_X_FACTOR) + offsetX;
  }

  private int y(double y) {
    return (int) (y * VP_Y_FACTOR) + offsetY;
  }

  private double f(double x, double z) {
    double a = a(x, z);
    return config.alpha * Math.sin(x) * Math.cos(z)
      - config.beta * Math.cos(a * config.gamma) * Math.exp(-a);
  }

  private double a(double x, double z) {
    return Math.pow(x - Math.PI, 2) + Math.pow(z - Math.PI, 2);
  }

  public void setProjectionAngle(int angle) {
    config.intAngleZ = angle;
    if (!animating) draw();
  }

  public void setPitchAngle(int angle) {
    config.intAngleP = angle;
    if (!animating) draw();
  }

  public void setAlpha(double alpha) {
    config.alpha = alpha;
    if (!animating) draw();
  }

  public void setBeta(double beta) {
    config.beta = beta;
    if (!animating) draw();
  }

  public void setGamma(double gamma) {
    config.gamma = gamma;
    if (!animating) draw();
  }
}
