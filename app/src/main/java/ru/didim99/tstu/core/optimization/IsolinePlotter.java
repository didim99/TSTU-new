package ru.didim99.tstu.core.optimization;

import android.graphics.Bitmap;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 29.09.19.
 */
class IsolinePlotter {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ILP";
  private static final int DEFAULT_WIDTH = 2000;
  private static final int DEFAULT_HEIGHT = 2000;
  private static final int LEVEL_COUNT = 20;
  private static final double EPS_F = 0.005;
  // Colors
  private static final int CLR_CLEAR  = 0x00000000;
  private static final int CLR_MIN    = 0xff202020;
  private static final int CLR_MAX    = 0xffa0a0a0;
  private static final int CLR_LINE   = 0xff3f51b5;

  private int width, height;
  private double xStep, yStep;
  private double[][] buffer;
  private Bitmap bitmap;
  private RectD bounds;

  IsolinePlotter() {
    this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

  IsolinePlotter(int width, int height) {
    this.buffer = new double[width][height];
    this.bitmap = Bitmap.createBitmap(
      width, height, Bitmap.Config.ARGB_8888);
    this.width = width;
    this.height = height;
  }

  void setBounds(RectD bounds) {
    this.xStep = (bounds.xMax - bounds.xMin) / width;
    this.yStep = (bounds.yMax - bounds.yMin) / height;
    this.bounds = bounds;
  }

  void plot(FunctionR2 fun) {
    double x = bounds.xMin, y = bounds.yMin, f;
    double fMin = Double.MAX_VALUE, fMax = -Double.MAX_VALUE;

    for (int xi = 0; xi < width; xi++, x += xStep, y = bounds.yMin) {
      for (int yi = 0; yi < height; yi++, y += yStep) {
        buffer[xi][yi] = f = fun.f(x, y);
        if (f < fMin) fMin = f;
        if (f > fMax) fMax = f;
      }
    }

    double fStep = (fMax - fMin) / LEVEL_COUNT;

    bitmap.eraseColor(CLR_CLEAR);
    for (int xi = 0; xi < width; xi++) {
      for (int yi = 0; yi < height; yi++) {
        f = buffer[xi][yi];
        double di = f - (fMin + fStep * Math.floor((f - fMin) / fStep));

        if (Math.abs(di) < EPS_F) {
          bitmap.setPixel(xi, height - (yi + 1), CLR_LINE);
        } else {
          double factor = Utils.norm(f, fMin, fMax);
          int color = Utils.lerp(CLR_MIN, CLR_MAX, 1 - factor);
          bitmap.setPixel(xi, height - (yi + 1), color);
        }
      }
    }
  }

  Bitmap getBitmap() {
    return bitmap;
  }
}
