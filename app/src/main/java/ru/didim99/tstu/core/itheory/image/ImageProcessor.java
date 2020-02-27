package ru.didim99.tstu.core.itheory.image;

import android.content.Context;
import java.io.File;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.CallbackTask;

/**
 * Created by didim99 on 13.02.20.
 */
public class ImageProcessor {
  public enum State { INITIAL, LOADING, READY, SAVING, PROCESSING, TESTING }

  private Context appContext;
  private EventListener listener;
  private State state;
  private Image image;

  public ImageProcessor(Context context) {
    this.appContext = context;
    applyState(State.INITIAL);
  }

  public Image getImage() {
    return image;
  }

  public void setStateChangeListener(EventListener listener) {
    this.listener = listener;
    publishState();
  }

  public void loadImage(String path) {
    logWrite(appContext.getString(
      R.string.iTheory_event_loadStart, path));
    ImageTask loader = new ImageTask(appContext);
    loader.registerEventListener(this::onImageTaskCompleted);
    loader.execute(new ImageTask.Action.Builder(
      ImageTask.Mode.LOAD).path(path).build());
    applyState(State.LOADING);
  }

  public void createImage(int width, int height) {
    image = new Image(width, height);
    applyState(State.READY);
    logWrite(appContext.getString(
      R.string.iTheory_event_created, width, height));
  }

  public void clearImage() {
    image.clear();
    applyState(State.READY);
    logWrite(appContext.getString(
      R.string.iTheory_event_cleared));
  }

  public void randomizeImage() {
    logWrite(appContext.getString(
      R.string.iTheory_event_generateStart));
    ImageTask loader = new ImageTask(appContext);
    loader.registerEventListener(this::onImageTaskCompleted);
    loader.execute(new ImageTask.Action.Builder(
      ImageTask.Mode.RANDOMIZE).image(image).build());
    applyState(State.PROCESSING);
  }

  public void saveImage(String path) {
    logWrite(appContext.getString(
      R.string.iTheory_event_saveStart, path));
    ImageTask saver = new ImageTask(appContext);
    saver.registerEventListener(this::onImageTaskCompleted);
    saver.execute(new ImageTask.Action.Builder(
      ImageTask.Mode.SAVE).image(image).path(path).build());
    applyState(State.SAVING);
  }

  public void testCoder() {
    logWrite(appContext.getString(
      R.string.iTheory_event_testingStart));
    ImageTask testTask = new ImageTask(appContext);
    testTask.registerEventListener(this::onImageTaskCompleted);
    testTask.setLogger(this::logWrite);
    testTask.execute(new ImageTask.Action.Builder(
      ImageTask.Mode.TEST).image(image).build());
    applyState(State.TESTING);
  }

  public void toggleImageState() {
    image.toggleCompressedState();
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

  private void onImageTaskCompleted(CallbackTask.Event event, ImageTask.Action action) {
    if (event.equals(CallbackTask.Event.FINISH)) {
      if (action.isSuccess()) {
        image = action.getImage();
        switch (state) {
          case LOADING:
            logWrite(appContext.getString(
              R.string.iTheory_event_loadEnd, image.getName(),
              new File(action.getPath()).length()));
            break;
          case SAVING:
            logWrite(appContext.getString(
              R.string.iTheory_event_saveEnd,
              new File(action.getPath()).length()));
            break;
        }
        applyState(State.READY);
      } else {
        if (listener != null)
          listener.onError(state, action.getError());
        applyState(State.INITIAL);
      }
    }
  }

  private void logWrite(String msg) {
    if (listener != null) listener.logWrite(msg);
  }

  public interface EventListener {
    void onStateChanged(State state);
    void onImageDataChanged(Image image);
    void onError(State state, String msg);
    void logWrite(String msg);
  }
}
