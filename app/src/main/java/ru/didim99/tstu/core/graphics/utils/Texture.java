package ru.didim99.tstu.core.graphics.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.IOException;

/**
 * Created by didim99 on 05.04.19.
 */
public class Texture {
  private Bitmap data;
  private boolean useFiltering;
  private int width, height;
  private double xfMax, yfMax;
  private int xMax, yMax;

  private Texture(Bitmap data) {
    this.data = data;
    this.width = data.getWidth();
    this.height = data.getHeight();
    this.xMax = width - 1;
    this.yMax = height - 1;
    this.xfMax = xMax - 1;
    this.yfMax = yMax - 1;
  }

  public void recycle() {
    data.recycle();
  }

  public void enableFiltering(boolean filter) {
    useFiltering = filter;
  }

  public int getPixel(double u, double v) {
    if (useFiltering) return getPixelFiltered(u, v);
    int x = bound((int) (u * width), xMax);
    int y = bound((int) (v * height), yMax);
    return data.getPixel(x, y);
  }

  private int getPixelFiltered(double u, double v) {
    u = bound(u * width, xfMax);
    v = bound(v * height, yfMax);
    int x = (int) Math.floor(u); u = x + 1 - u;
    int y = (int) Math.floor(v); v = y + 1 - v;
    int c1 = lerp(data.getPixel(x, y), data.getPixel(x+1, y), u);
    int c2 = lerp(data.getPixel(x, y+1), data.getPixel(x+1, y+1), u);
    return lerp(c1, c2, v);
  }

  private static int lerp(int bg, int fg, double alpha) {
    double gamma = 1 - alpha;
    int res = (int) ((bg & 0xff) * alpha + (fg & 0xff) * gamma);
    res |= (int) (((bg >> 8) & 0xff) * alpha + ((fg >> 8) & 0xff) * gamma) << 8;
    res |= (int) (((bg >> 16) & 0xff) * alpha + ((fg >> 16) & 0xff) * gamma) << 16;
    res |= 0xff000000;
    return res;
  }

  private static int bound(int i, int max) {
    return i < 0 ? 0 : (i > max ? max : i);
  }

  private static double bound(double d, double max) {
    return d < 0 ? 0 : (d > max ? max : d);
  }

  public static Texture load(String path) throws IOException {
    Bitmap src = BitmapFactory.decodeFile(path);
    if (src == null) throw new IOException("Unable to decode file");
    return new Texture(src);
  }
}
