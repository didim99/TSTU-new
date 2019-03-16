package ru.didim99.tstu.core.graphics;

import android.graphics.Color;
import ru.didim99.tstu.ui.view.DrawerView;

/**
 * Created by didim99 on 13.02.19.
 */
public class LineRenderer extends AsyncRenderer {
  public static final int ANGLE_MIN = -90;
  public static final int ANGLE_MAX = 90;
  private static final int DEFAULT_WIDTH = 20;
  private static final int DEFAULT_HEIGHT = 20;
  private static final int colorNegative = Color.RED;
  private static final int colorPositive = Color.GREEN;

  private int realXZero, xMin, xMax, realYZero, yMin, yMax;
  private int dx, dy, di, xi, yi;
  private boolean reverse, swap;

  public LineRenderer(DrawerView target, Config config) {
    super(target, Config.Type.BITMAP, config,
      DEFAULT_WIDTH, DEFAULT_HEIGHT, false);
    realXZero = this.config.width / 2;
    realYZero = this.config.height / 2;
    xMax = realXZero - 1;
    yMax = realYZero - 1;
    xMin = -realXZero;
    yMin = -realYZero;
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
    xi = xMin;
  }

  @Override
  public void frame() {
    if (yi < yMax) drawPixel(xi, yi+1, config.colorBg);
    if (yi > yMin) drawPixel(xi, yi-1, config.colorBg);
    drawPixel(xi, yi, config.colorFg);

    if (di > 0) {
      di += 2 * (dy - dx);
      if (xi < xMax)
        drawPixel(xi+1, yi, colorNegative);
      yi++;
    } else {
      di += 2 * dy;
      if (xi < xMax && yi < yMax)
        drawPixel(xi+1, yi+1, colorNegative);
    }

    if (xi < xMax) {
      drawPixel(++xi, yi, colorPositive);
    } else {
      pause(true);
      animating = false;
    }
  }

  private void prepareDraw() {
    clear();
    reverse = false;
    swap = false;

    double a = config.angle;
    if (a < 0) { reverse = true; a = -a; }
    if (a > 45) { swap = true; a = 90 - a; }

    double t = Math.tan(Math.toRadians(a));
    int fromY = (int) (xMin * t);
    int toY = (int) (xMax * t);
    dx = xMax - xMin;
    dy = toY - fromY;
    di = 2 * dy - dx;
    yi = fromY;
  }

  private void draw() {
    prepareDraw();
    double g = (double) dy / dx;
    double t, yTmp = yi + g;

    for (int xi = xMin; xi <= xMax; xi++) {
      if (config.antiAlias) {
        yi = (int) yTmp;
        dy = yTmp > 0 ? 1 : -1;
        t = Math.abs(yTmp - yi);
        drawPixel(xi, yi, lerp(config.colorBg, config.colorFg, t));
        drawPixel(xi, yi + dy, lerp(config.colorBg, config.colorFg, 1 - t));
        yTmp += g;
      } else {
        drawPixel(xi, yi, config.colorFg);
        if (di > 0) {
          di += 2 * (dy - dx);
          yi++;
        } else
          di += 2 * dy;
      }
    }
  }

  private void drawPixel(int x, int y, int color) {
    if (reverse) y = -y;
    if (swap) { int t = x; x = y; y = t; }
    bitmap.setPixel(x(x), y(y), color);
  }

  private int x(int x) {
    return realXZero + x;
  }

  private int y(int y) {
    return config.height - (realYZero + y) - 1;
  }

  private int lerp(int bg, int fg, double alpha) {
    double gamma = 1 - alpha;
    int res = (int) ((bg & 0xff) * alpha + (fg & 0xff) * gamma);
    res |= (int) (((bg >> 8) & 0xff) * alpha + ((fg >> 8) & 0xff) * gamma) << 8;
    res |= (int) (((bg >> 16) & 0xff) * alpha + ((fg >> 16) & 0xff) * gamma) << 16;
    res |= 0xff000000;
    return res;
  }

  public void setAntiAlias(boolean antiAlias) {
    config.antiAlias = antiAlias;
    if (!animating) draw();
  }

  public void setAngle(int angle) {
    config.angle = angle;
    if (!animating) draw();
  }
}
