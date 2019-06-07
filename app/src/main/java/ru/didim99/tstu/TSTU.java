package ru.didim99.tstu;

import android.app.Application;
import android.widget.Toast;
import java.io.File;
import java.util.Objects;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.ResourceTask;
import ru.didim99.tstu.ui.dirpicker.DirPickerActivity;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;

/**
 * Root Application class
 * Created by didim99 on 16.09.18.
 */
public class TSTU extends Application {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_App";
  private static final String EXAMPLES_DIR = "examples";
  public static final String EXTRA_TYPE = BuildConfig.APPLICATION_ID + ".Type";

  @Override
  public void onCreate() {
    super.onCreate();
    InputValidator.getInstance().init(this);

    try {
      String cache = Objects.requireNonNull(
        getExternalCacheDir()).getAbsolutePath();
      File examples = new File(cache, EXAMPLES_DIR);
      if (!examples.exists()) {
        ResourceTask task = new ResourceTask(this);
        task.registerEventListener((e, s) -> {
          if (e == CallbackTask.Event.FINISH) {
            Toast.makeText(this, s ? R.string.resourcesUnpacked :
              R.string.errGeneric_resUnpackFailed, Toast.LENGTH_LONG).show();
            if (s) DirPickerActivity.setDefaultPath(
              this, examples.getAbsolutePath());
          }
        });
        task.execute(cache);
      }
    } catch (NullPointerException e) {
      MyLog.e(LOG_TAG, "External cache directory unavailable");
      Toast.makeText(this, R.string.errGeneric_cacheUnavailable,
        Toast.LENGTH_LONG).show();
    }
  }
}
