package ru.didim99.tstu.ui.graphics;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.graphics.Config;
import ru.didim99.tstu.core.graphics.ModelLoader;
import ru.didim99.tstu.core.graphics.ModelRenderer;
import ru.didim99.tstu.core.graphics.TextureLoader;
import ru.didim99.tstu.core.graphics.utils.Model;
import ru.didim99.tstu.ui.dirpicker.DirPickerActivity;
import ru.didim99.tstu.ui.view.DrawerView;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 02.04.19.
 */
public class TexturesActivity extends AnimationActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_TexAct";
  private static final int REQUEST_GET_MODEL = 1;
  private static final int REQUEST_GET_TEXTURE = 2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "TexturesActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_textures);

    MyLog.d(LOG_TAG, "View components init...");
    DrawerView targetView = findViewById(R.id.view);
    findViewById(R.id.btnLoadModel).setOnClickListener(
      v -> openFile(REQUEST_GET_MODEL));
    findViewById(R.id.btnLoadTexture).setOnClickListener(
      v -> openFile(REQUEST_GET_TEXTURE));
    MyLog.d(LOG_TAG, "View components init competed");

    Config config = (Config) getLastCustomNonConfigurationInstance();
    renderer = new ModelRenderer(Model.Type.FACE, targetView, config);
    renderer.start();

    ((ModelRenderer) renderer).setScale(2.5);
    //((ModelRenderer) renderer).setRotateY(30);
    MyLog.d(LOG_TAG, "TexturesActivity started");
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent i) {
    if (resultCode == RESULT_OK) {
      Uri data = i.getData();
      if (data != null) {
        String path = data.getPath();
        if (path != null) {
          MyLog.d(LOG_TAG, "Loading item: " + path);

          if (requestCode == REQUEST_GET_MODEL) {
            ModelLoader loader = new ModelLoader(this);
            loader.registerEventListener((event, model) -> {
              if (event != CallbackTask.Event.FINISH) return;
              if (model == null) {
                Toast.makeText(this, R.string.errGraphics_unableLoad,
                  Toast.LENGTH_LONG).show();
                return;
              }

              if (model.getType() == Model.Type.FACE)
                ((ModelRenderer) renderer).onModelLoaded(model);
              else Toast.makeText(this, R.string.errGraphics_incorrectType,
                Toast.LENGTH_LONG).show();
            });
            loader.execute(path);
          } else if (requestCode == REQUEST_GET_TEXTURE) {
            TextureLoader loader = new TextureLoader(this);
            loader.registerEventListener((event, texture) ->
              ((ModelRenderer) renderer).onTextureLoaded(texture));
            loader.execute(path);
          }

        } else {
          Toast.makeText(this, R.string.errGraphics_incorrectType,
            Toast.LENGTH_LONG).show();
        }
      }
    }

    else if (resultCode == RESULT_CANCELED)
      MyLog.d(LOG_TAG, "Choosing path aborted");
  }

  private void openFile(int requestCode) {
    MyLog.d(LOG_TAG, "Choose file from DirPicker...");
    Intent intent = new Intent(this, DirPickerActivity.class);
    intent.putExtra(DirPickerActivity.KEY_MODE, DirPickerActivity.Mode.FILE);
    startActivityForResult(intent, requestCode);
  }
}
