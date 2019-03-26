package ru.didim99.tstu.core.graphics;

import android.content.Context;
import android.widget.Toast;
import java.io.IOException;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.graphics.utils.EdgeModel;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 09.03.19.
 */
public class ModelLoader extends CallbackTask<String, EdgeModel> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ModelLoader";

  public ModelLoader(Context context) {
    super(context);
  }

  @Override
  protected EdgeModel doInBackgroundInternal(String path) {
    Context ctx = appContext.get();
    String err;

    try {
      return EdgeModel.loadOBJ(path);
    } catch (IOException e) {
      MyLog.e(LOG_TAG, "Unable to load model: " + e);
      err = ctx.getString(R.string.errGraphics_ioError);
    } catch (EdgeModel.ParserException e) {
      MyLog.e(LOG_TAG, "Unable to load model: " + e);
      err = ctx.getString(R.string.errGraphics_invalidFormat);
    }

    Toast.makeText(ctx, ctx.getString(
      R.string.errGraphics_unableLoad, err),
      Toast.LENGTH_LONG).show();
    return null;
  }
}
