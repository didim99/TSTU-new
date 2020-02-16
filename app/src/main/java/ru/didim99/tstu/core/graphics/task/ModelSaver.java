package ru.didim99.tstu.core.graphics.task;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.graphics.model.Model;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 09.03.19.
 */
public class ModelSaver extends CallbackTask<Model, String> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ModelLoader";
  private static final String MODELS_DIR = "models";

  public ModelSaver(Context context) {
    super(context);
  }

  @Override
  protected String doInBackgroundInternal(Model model) {
    Context ctx = appContext.get();

    try {
      String cache = Objects.requireNonNull(
        ctx.getExternalCacheDir()).getAbsolutePath();
      File dir = new File(cache, MODELS_DIR);
      if (!dir.exists() && !dir.mkdirs())
        throw new IOException("Can't create destination dir: " + dir.getPath());
      String path = model.save(dir.getAbsolutePath());
      return ctx.getString(R.string.graphics_modelSaved, path);
    } catch (NullPointerException e) {
      MyLog.e(LOG_TAG, "External cache directory unavailable");
      return ctx.getString(R.string.errGeneric_cacheUnavailable);
    } catch (IOException e) {
      MyLog.e(LOG_TAG, "Unable to save model: " + e);
      return ctx.getString(R.string.errGraphics_saveModel, e.toString());
    }
  }
}
