package ru.didim99.tstu.core.math.optimization;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import ru.didim99.tstu.core.math.common.FunctionRN;
import ru.didim99.tstu.core.math.common.Limit;
import ru.didim99.tstu.core.math.common.PointD;
import ru.didim99.tstu.core.math.common.RectD;
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
  private static final int LINES_AREA = 200;
  private static final int LINES_ITER = 5;
  private static final int LINES_ITER_MAX = 100;
  private static final double EPS_F = 0.005;
  // Colors
  private static final int CLR_CLEAR  = 0x00000000;
  private static final int CLR_MIN    = 0xff202020;
  private static final int CLR_MAX    = 0xffa0a0a0;
  private static final int CLR_LINE   = 0xff3f51b5;
  private static final int CLR_LIMIT  = 0x206aa4f5;
  private static final int CLR_LIM_EQ = 0xff6aa4f5;

  private int width, height;
  private double xStep, yStep;
  private double[][] buffer;
  private Bitmap bmpBase, bmpOverlay;
  private RectD bounds;
  private int minPoints, maxPoints;

  IsolinePlotter() {
    this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

  private IsolinePlotter(int width, int height) {
    this.width = width;
    this.height = height;
    this.buffer = new double[width][height];
    this.bmpBase = Bitmap.createBitmap(
      width, height, Bitmap.Config.ARGB_8888);
    this.bmpOverlay = Bitmap.createBitmap(bmpBase);
    this.minPoints = width * height / LINES_AREA;
    this.maxPoints = width * height / LINES_AREA * 10;
  }

  void setBounds(RectD bounds) {
    this.xStep = (bounds.xMax - bounds.xMin) / width;
    this.yStep = (bounds.yMax - bounds.yMin) / height;
    this.bounds = bounds;
  }

  void plot(FunctionRN fun) {
    PointD p = new PointD(bounds.xMin, bounds.yMin);
    double f, fMin = Double.MAX_VALUE, fMax = -Double.MAX_VALUE;

    MyLog.d(LOG_TAG, "Calculating function values...");
    for (int xi = 0; xi < width; xi++, p.add(0, xStep), p.set(1, bounds.yMin)) {
      for (int yi = 0; yi < height; yi++, p.add(1, yStep)) {
        buffer[xi][yi] = f = fun.f(p);
        if (!Double.isInfinite(f)) {
          if (f < fMin) fMin = f;
          if (f > fMax) fMax = f;
        }
      }
    }

    MyLog.v(LOG_TAG, "Function min/max: " + fMin + " / " + fMax);
    MyLog.d(LOG_TAG, "Drawing base...");
    Canvas canvas = new Canvas(bmpBase);

    for (int xi = 0; xi < width; xi++) {
      for (int yi = 0; yi < height; yi++) {
        double factor = Utils.norm(buffer[xi][yi], fMin, fMax);
        int color = Utils.lerp(CLR_MIN, CLR_MAX, 1 - factor);
        bmpBase.setPixel(xi, height - (yi + 1), color);
      }
    }

    int linePoints = 0, retry = 0, totalRetry = 0;
    double fStep = (fMax - fMin) / LEVEL_COUNT;
    double eps = EPS_F;

    MyLog.d(LOG_TAG, "Drawing lines...");
    while (linePoints < minPoints || linePoints > maxPoints) {
      linePoints = drawLines(fMin, fStep, eps);
      MyLog.v(LOG_TAG, "Found line points: "
        + linePoints + " step: " + fStep + " eps: " + eps);

      if (totalRetry++ > LINES_ITER_MAX) {
        MyLog.e(LOG_TAG, "Unable to draw lines correctly");
        break;
      }

      retry++;
      if (linePoints > maxPoints) {
        eps /= 2;
      } else if (retry >= LINES_ITER) {
        fStep = (fMax - fMin) / LEVEL_COUNT;
        eps *= 2;
        retry = 0;
      } else
        fStep /= 2;
    }

    MyLog.d(LOG_TAG, "Merging layers...");
    canvas.drawBitmap(bmpOverlay, 0, 0, null);
  }

  void drawLimits(Limit... limits) {
    MyLog.d(LOG_TAG, "Drawing limits (" + limits.length + ")...");
    Canvas canvas = new Canvas(bmpBase);

    for (Limit limit : limits) {
      int color = limit.isEquality() ? CLR_LIM_EQ : CLR_LIMIT;
      PointD p = new PointD(bounds.xMin, bounds.yMin);
      bmpOverlay.eraseColor(CLR_CLEAR);

      for (int xi = 0; xi < width; xi++, p.add(0, xStep), p.set(1, bounds.yMin)) {
        for (int yi = 0; yi < height; yi++, p.add(1, yStep)) {
          if (limit.checkVisibility(p))
            bmpOverlay.setPixel(xi, height - (yi + 1), color);
        }
      }

      canvas.drawBitmap(bmpOverlay, 0, 0, null);
    }
  }

  private int drawLines(double fMin, double fStep, double eps) {
    int linePoints = 0;

    bmpOverlay.eraseColor(CLR_CLEAR);
    for (int xi = 0; xi < width; xi++) {
      for (int yi = 0; yi < height; yi++) {
        double f = buffer[xi][yi];
        double di = f - (fMin + fStep * Math.floor((f - fMin) / fStep));

        if (Math.abs(di) < eps) {
          bmpOverlay.setPixel(xi, height - (yi + 1), CLR_LINE);
          linePoints++;
        }
      }
    }

    return linePoints;
  }

  Bitmap getBitmap() {
    return bmpBase;
  }

  int getWidth() {
    return width;
  }

  int getHeight() {
    return height;
  }
}
