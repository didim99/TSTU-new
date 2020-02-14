package ru.didim99.tstu.core.itheory;

import android.content.Context;
import java.io.IOException;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 13.02.20.
 */
public class ImageProcessor {
  public enum State { INITIAL, LOADING, READY, PROCESSING, SAVING }

  private State state;
  private StateChangeListener listener;
  private Image image;

  public ImageProcessor() {
    applyState(State.INITIAL);
  }

  public Image getImage() {
    return image;
  }

  public void setStateChangeListener(StateChangeListener listener) {
    this.listener = listener;
    publishState();
  }

  public void loadImage(Context context, String path) {
    ImageLoader loader = new ImageLoader(context);
    loader.registerEventListener((e, d) -> {
      if (e.equals(CallbackTask.Event.START)) applyState(State.LOADING);
      else if (d != null) applyState(State.READY);
      else applyState(State.INITIAL);
    });
    loader.execute(path);
  }

  public void createImage(int width, int height) {
    image = new Image(width, height);
    applyState(State.READY);
  }

  public void clearImage() {
    image.clear();
    applyState(State.READY);
  }

  public void randomizeImage() {
    image.randomize();
    applyState(State.READY);
  }

  private void applyState(State state) {
    this.state = state;
    publishState();
  }

  private void publishState() {
    if (listener != null) {
      listener.onStateChanged(state);
      if (state.equals(State.READY))
        listener.onImageDataChanged(image);
    }
  }

  private static class ImageLoader extends CallbackTask<String, Image> {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ImageLoader";

    public ImageLoader(Context context) {
      super(context);
    }

    @Override
    protected Image doInBackgroundInternal(String path) {
      try {
        return Image.load(path);
      } catch (IOException | Image.ParserException e) {
        MyLog.e(LOG_TAG, "Unable to load image: " + e);
      }

      return null;
    }
  }

  public interface StateChangeListener {
    void onStateChanged(State state);
    void onImageDataChanged(Image image);
  }
}
