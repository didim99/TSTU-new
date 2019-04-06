package ru.didim99.tstu.ui.graphics;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Toast;
import java.io.IOException;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.graphics.Config;
import ru.didim99.tstu.core.graphics.ModelLoader;
import ru.didim99.tstu.core.graphics.TextureLoader;
import ru.didim99.tstu.core.graphics.TextureRenderer;
import ru.didim99.tstu.ui.dirpicker.DirPickerActivity;
import ru.didim99.tstu.ui.view.DrawerView;
import ru.didim99.tstu.ui.view.RangeBar;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 02.04.19.
 */
public class TexturesActivity extends AnimationActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_TexAct";
  private static final int REQUEST_GET_TEXTURE = 1;

  private CheckBox cbTextureFiltering;
  private RangeBar rbTranslateX, rbTranslateY;
  private RangeBar rbScale, rbRotate;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "TexturesActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_textures);

    MyLog.d(LOG_TAG, "View components init...");
    DrawerView targetView = findViewById(R.id.view);
    cbTextureFiltering = findViewById(R.id.cbTextureFiltering);
    rbTranslateX = findViewById(R.id.rbTranslateX);
    rbTranslateY = findViewById(R.id.rbTranslateY);
    rbScale = findViewById(R.id.rbScale);
    rbRotate = findViewById(R.id.rbRotate);

    findViewById(R.id.btnLoadTexture).setOnClickListener(v -> openFile());
    findViewById(R.id.btnReset).setOnClickListener(v -> clearTransform());
    findViewById(R.id.btnClear).setOnClickListener(v -> {
      ((TextureRenderer) renderer).clearTexture();
      cbTextureFiltering.setEnabled(false);
    });
    MyLog.d(LOG_TAG, "View components init competed");

    Config config = (Config) getLastCustomNonConfigurationInstance();
    renderer = new TextureRenderer(targetView, config);
    renderer.start();

    config = renderer.getConfig();
    // Translation configuration
    rbTranslateX.setFactor(TextureRenderer.TRN_FACTOR);
    rbTranslateX.setBounds(TextureRenderer.TRN_MIN, TextureRenderer.TRN_MAX);
    rbTranslateX.setValue(config.getTranslate().x());
    rbTranslateX.setOnScaledValueChangedListener(value ->
      ((TextureRenderer) renderer).setTranslateX(value));
    rbTranslateY.setFactor(TextureRenderer.TRN_FACTOR);
    rbTranslateY.setBounds(TextureRenderer.TRN_MIN, TextureRenderer.TRN_MAX);
    rbTranslateY.setValue(config.getTranslate().y());
    rbTranslateY.setOnScaledValueChangedListener(value ->
      ((TextureRenderer) renderer).setTranslateY(value));
    // Scale configuration
    rbScale.setFactor(TextureRenderer.SCL_FACTOR);
    rbScale.setBounds(TextureRenderer.SCL_MIN, TextureRenderer.SCL_MAX);
    rbScale.setValue(config.getScale().x());
    rbScale.setOnScaledValueChangedListener(value ->
      ((TextureRenderer) renderer).setScale(value));
    // Rotation configuration
    rbRotate.setBounds(TextureRenderer.ROT_MIN, TextureRenderer.ROT_MAX);
    rbRotate.setValue((int) config.getRotate().z());
    rbRotate.setOnValueChangedListener(value ->
      ((TextureRenderer) renderer).setRotate(value));
    // Texture filtering configuration
    cbTextureFiltering.setEnabled(config.hasTexture());
    cbTextureFiltering.setChecked(config.useAntiAlias());
    cbTextureFiltering.setOnCheckedChangeListener((v, c) -> {
      MyLog.d(LOG_TAG, "Texture filtering: " + c);
      ((TextureRenderer) renderer).setTextureFiltering(c);
    });

    try {
      String path = Utils.resToFile(this, R.raw.plane);
      ModelLoader loader = new ModelLoader(this);
      loader.registerEventListener((e, m) ->
        ((TextureRenderer) renderer).onModelLoaded(m));
      loader.execute(path);
    } catch (IOException e) {
      MyLog.d(LOG_TAG, "Unable to load model: " + e);
      Toast.makeText(this, getString(
        R.string.errGraphics_unableLoadInternal, e.toString()),
        Toast.LENGTH_LONG).show();
      finish();
    }

    MyLog.d(LOG_TAG, "TexturesActivity started");
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent i) {
    if (requestCode == REQUEST_GET_TEXTURE && resultCode == RESULT_OK) {
      Uri data = i.getData();
      if (data != null) {
        String path = data.getPath();
        if (path != null) {
          MyLog.d(LOG_TAG, "Loading item: " + path);
          TextureLoader loader = new TextureLoader(this);
          loader.registerEventListener((event, texture) -> {
            ((TextureRenderer) renderer).onTextureLoaded(texture);
            cbTextureFiltering.setEnabled(texture != null);
          });
          loader.execute(path);
        } else {
          Toast.makeText(this, R.string.errGraphics_incorrectType,
            Toast.LENGTH_LONG).show();
        }
      }
    }

    else if (resultCode == RESULT_CANCELED)
      MyLog.d(LOG_TAG, "Choosing path aborted");
  }

  private void openFile() {
    MyLog.d(LOG_TAG, "Choose file from DirPicker...");
    Intent intent = new Intent(this, DirPickerActivity.class);
    intent.putExtra(DirPickerActivity.KEY_MODE, DirPickerActivity.Mode.FILE);
    startActivityForResult(intent, REQUEST_GET_TEXTURE);
  }

  private void clearTransform() {
    MyLog.d(LOG_TAG, "Clearing all transformations");
    Config config = ((TextureRenderer) renderer).clearTransform();
    rbTranslateX.setValue(config.getTranslate().x());
    rbTranslateY.setValue(config.getTranslate().y());
    rbScale.setValue(config.getScale().x());
    rbRotate.setValue((int) config.getRotate().z());
  }
}
