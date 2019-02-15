package ru.didim99.tstu.core.graphics;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import java.lang.ref.WeakReference;
import ru.didim99.tstu.ui.DrawerView;

/**
 * Created by didim99 on 15.02.19.
 */
public class AsyncRenderer extends AsyncTask<Void, Void, Void> {
  private static final int FPS = 1;
  private static final int IDLE = 1000;
  private static final int FRAME = 1000 / FPS;
  private static final int DEFAULT_WIDTH = 20;
  private static final int DEFAULT_HEIGHT = 20;

  private WeakReference<DrawerView> target;
  private boolean running, paused;
  boolean animating;
  Config config;
  Bitmap bitmap;

  AsyncRenderer(DrawerView target, Config config) {
    this(target, config, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

  AsyncRenderer(DrawerView target, Config config, int width, int height) {
    if (config == null) {
      this.config = new Config();
      this.config.width = width;
      this.config.height = height;
      this.config.bgColor = Color.BLACK;
    } else this.config = config;
    this.target = new WeakReference<>(target);
    this.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    target.setSource(bitmap);
    clear();
  }

  @Override
  protected void onPreExecute() {
    this.animating = false;
    this.running = true;
    this.paused = true;
  }

  @Override
  protected Void doInBackground(Void... voids) {
    while (running) {
      try {
        if (paused) { Thread.sleep(IDLE); continue; }
        frame();
        publishProgress();
        Thread.sleep(FRAME);
      } catch (InterruptedException ignored) {}
    }
    return null;
  }

  @Override
  protected void onProgressUpdate(Void... values) {
    DrawerView v = target.get();
    if (v != null) v.invalidate();
  }

  public void pause(boolean paused) {
    this.paused = paused;
  }

  public void finish() {
    running = false;
    target.clear();
  }

  public void clear() {
    bitmap.eraseColor(config.bgColor);
  }

  public void animate() {
    animating = true;
    paused = false;
  }

  public Config getConfig() {
    return config;
  }

  public void setConfig(Config config) {
    this.config = config;
  }

  public void setBackground(int color) {
    config.bgColor = color;
  }

  void frame() {}
}
