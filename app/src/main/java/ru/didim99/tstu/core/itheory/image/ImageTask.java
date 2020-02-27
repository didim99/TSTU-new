package ru.didim99.tstu.core.itheory.image;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 14.02.20.
 */
public class ImageTask extends CallbackTask<ImageTask.Action, ImageTask.Action> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ImageTask";

  enum Mode { LOAD, SAVE, RANDOMIZE, TEST }
  private Queue<String> log;
  private Logger logger;

  ImageTask(Context context) {
    super(context);
  }

  void setLogger(Logger logger) {
    this.log = new LinkedList<>();
    this.logger = logger;
  }

  @Override
  protected Action doInBackgroundInternal(Action action) {
    try {
      switch (action.mode) {
        case LOAD: action.image = Image.load(action.path); break;
        case SAVE: action.image.save(action.path); break;
        case RANDOMIZE: action.image.randomize(); break;
        case TEST: coderTest(action.image); break;
      }
    } catch (IOException | Image.ParserException e) {
      MyLog.e(LOG_TAG, "Unable to load image: " + e);
      action.error = e.toString();
    }

    return action;
  }

  @Override
  protected void onProgressUpdate(Void... values) {
    if (logger != null) logger.logWrite(log.poll());
  }

  private void coderTest(Image image)
    throws IOException, Image.ParserException {
    File tmpFile = File.createTempFile("tmpImage", ".img");
    String filename = tmpFile.getAbsolutePath();
    logWrite(appContext.get().getString(
      R.string.iTheory_event_testingCopyCreated, filename));

    boolean compressed = image.isCompressed();
    image.setCompressed(false);
    image.save(filename);
    image.setCompressed(compressed);
    byte[] origData = Utils.readFileRaw(filename);
    logWrite(appContext.get().getString(
      R.string.iTheory_event_testingOrigWritten, origData.length));

    Image tmpImage = Image.load(filename);
    logWrite(appContext.get().getString(
      R.string.iTheory_event_testingOrigLoaded));
    tmpImage.setCompressed(true);
    tmpImage.save(filename);
    long compSize = new File(filename).length();
    logWrite(appContext.get().getString(
      R.string.iTheory_event_testingCompWritten, compSize));
    tmpImage.getBitmap().recycle();

    tmpImage = Image.load(filename);
    logWrite(appContext.get().getString(
      R.string.iTheory_event_testingCompLoaded));
    tmpImage.setCompressed(false);
    tmpImage.save(filename);
    byte[] copyData = Utils.readFileRaw(filename);
    logWrite(appContext.get().getString(
      R.string.iTheory_event_testingCopyWritten, copyData.length));
    tmpImage.getBitmap().recycle();
    if (tmpFile.delete()) {
      logWrite(appContext.get().getString(
        R.string.iTheory_event_testingCopyDeleted));
    }

    if (Arrays.equals(origData, copyData)) {
      double score = (origData.length - compSize) * 100.0 / origData.length;
      logWrite(appContext.get().getString(
        R.string.iTheory_event_testSuccessful, score));
    } else {
      logWrite(appContext.get().getString(
        R.string.iTheory_event_testFailed));
    }
  }

  private void logWrite(String msg) {
    this.log.add(msg);
    publishProgress();
  }

  static class Action {
    private Mode mode;
    private String path;
    private Image image;
    private String error;

    private Action(Mode mode) {
      this.mode = mode;
    }

    String getPath() {
      return path;
    }

    Image getImage() {
      return image;
    }

    String getError() {
      return error;
    }

    boolean isSuccess() {
      return error == null;
    }

    static class Builder {
      private Action action;

      Builder(Mode mode) {
        action = new Action(mode);
      }

      Builder path(String path) {
        action.path = path;
        return this;
      }

      Builder image(Image image) {
        action.image = image;
        return this;
      }

      Action build() {
        return action;
      }
    }
  }

  @FunctionalInterface
  interface Logger {
    void logWrite(String msg);
  }
}