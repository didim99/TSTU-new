package ru.didim99.tstu.core.graphics;

import android.graphics.Color;

/**
 * Created by didim99 on 15.02.19.
 */
public class Config {
  // Global
  int width, height;
  int bgColor;

  // LineRenderer
  boolean antiAlias = false;
  int color = Color.WHITE;
  int angle = 0;

  public int getAngle() {
    return angle;
  }
}
