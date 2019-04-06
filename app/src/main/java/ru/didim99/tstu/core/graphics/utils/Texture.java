package ru.didim99.tstu.core.graphics.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.IOException;

/**
 * Created by didim99 on 05.04.19.
 */
public class Texture {
  private Bitmap data;
  private int width, height;
  private int xMax, yMax;

  private Texture(Bitmap data) {
    this.data = data;
    this.width = data.getWidth();
    this.height = data.getHeight();
    this.xMax = width - 1;
    this.yMax = height - 1;
  }

  public int getPixel(double u, double v) {
    int x = bound((int) (u * width), xMax);
    int y = bound((int) (v * height), yMax);
    return data.getPixel(x, y);
  }

  public static Texture load(String path) throws IOException {
    Bitmap src = BitmapFactory.decodeFile(path);
    if (src == null) throw new IOException("Unable to decode file");
    return new Texture(src);
  }

  private static int bound(int i, int max) {
    return i < 0 ? 0 : (i > max ? max : i);
  }
}
