package ru.didim99.tstu.core.graphics;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.CallSuper;
import java.lang.ref.WeakReference;
import ru.didim99.tstu.ui.view.DrawerView;

/**
 * Created by didim99 on 15.02.19.
 */
public class AsyncRenderer extends AsyncTask<Void, Void, Void> {
  private static final int DEFAULT_BG = Color.WHITE;
  private static final int DEFAULT_FG = Color.BLACK;
  private static final int DEFAULT_FPS = 1;
  private static final int IDLE = 1000;

  public enum AnimationEvent { START, FINISH }

  private AnimationEventListener listener;
  private WeakReference<DrawerView> target;
  private volatile boolean running, paused;
  private volatile boolean realtimeMode;
  private volatile boolean animationFinish;
  private volatile boolean animating;
  private volatile int frameSleep;
  Config config;
  Bitmap bitmap;

  AsyncRenderer(DrawerView target, int type, Config config,
                int width, int height, boolean antiAlias) {
    if (config == null) {
      this.config = new Config();
      this.config.width = width;
      this.config.height = height;
      this.config.colorBg = DEFAULT_BG;
      this.config.colorFg = DEFAULT_FG;
    } else this.config = config;
    this.target = new WeakReference<>(target);

    if (type == Config.Type.BITMAP) {
      this.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      target.setSource(bitmap, antiAlias);
    }

    setFPS(DEFAULT_FPS);
    clear();
  }

  public void setEventListener(AnimationEventListener listener) {
    this.listener = listener;
  }

  public void start() {
    executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  void onSceneCreated(Scene scene) {
    target.get().setSource(scene);
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
        if (!paused) {
          if (animating) {
            animateInternal();
            animationFinish = true;
            onFrame();
          } else if (realtimeMode)
            update();
        } else Thread.sleep(IDLE);
      } catch (InterruptedException ignored) {}
    }
    return null;
  }

  @Override
  protected void onProgressUpdate(Void... values) {
    DrawerView v = target.get();
    if (v != null) v.invalidate();
    if (animationFinish) {
      if (listener != null)
        listener.onAnimationEvent(AnimationEvent.FINISH);
      animationFinish = false;
      animating = false;
      paused = true;
    }
  }

  void onFrame() throws InterruptedException {
    publishProgress();
    Thread.sleep(frameSleep);
  }

  public void pause(boolean paused) {
    if (animating || realtimeMode)
      this.paused = paused;
  }

  public void finish() {
    if (listener != null)
      listener.onAnimationEvent(AnimationEvent.FINISH);
    running = false;
    target.clear();
  }

  public void clear() {
    if (bitmap != null)
      bitmap.eraseColor(config.colorBg);
  }

  @CallSuper
  public void animate() {
    if (listener != null)
      listener.onAnimationEvent(AnimationEvent.START);
    animationFinish = false;
    animating = true;
    paused = false;
  }

  boolean isAnimating() {
    return animating;
  }

  public Config getConfig() {
    return config;
  }

  public void setConfig(Config config) {
    this.config = config;
  }

  void setFPS(int fps) {
    frameSleep = 1000 / fps;
  }

  void setRealtimeMode() {
    this.realtimeMode = true;
  }

  void animateInternal() throws InterruptedException {}
  void update() throws InterruptedException {}

  public interface AnimationEventListener {
    void onAnimationEvent(AnimationEvent event);
  }
}
