package ru.didim99.tstu.ui.graphics;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.graphics.Config;
import ru.didim99.tstu.core.graphics.task.ModelLoader;
import ru.didim99.tstu.core.graphics.ModelRenderer;
import ru.didim99.tstu.core.graphics.utils.Model;
import ru.didim99.tstu.ui.view.DrawerView;
import ru.didim99.tstu.ui.view.RangeBar;
import ru.didim99.tstu.utils.MyLog;

public class ShadingActivity extends AnimationActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ShaderAct";

  // View-elements
  private RangeBar rbScale;
  private RangeBar rbRotateX, rbRotateY, rbRotateZ;
  private CheckBox cbVNormals, cbUseLamp;
  private Button btnLoad;
  private View lightLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "ShadingActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_shading);

    MyLog.d(LOG_TAG, "View components init...");
    DrawerView targetView = findViewById(R.id.view);
    lightLayout = findViewById(R.id.lightLayout);
    btnLoad = findViewById(R.id.btnLoad);
    rbScale = findViewById(R.id.rbScale);
    rbRotateX = findViewById(R.id.rbRotateX);
    rbRotateY = findViewById(R.id.rbRotateY);
    rbRotateZ = findViewById(R.id.rbRotateZ);
    cbVNormals = findViewById(R.id.cbVNormals);
    cbUseLamp = findViewById(R.id.cbUseLamp);
    RangeBar rbKd = findViewById(R.id.rbKd);
    btnLoad.setOnClickListener(v -> openFile());
    findViewById(R.id.btnClear).setOnClickListener(v -> renderer.clear());
    findViewById(R.id.btnReset).setOnClickListener(v -> clearTransform());
    MyLog.d(LOG_TAG, "View components init competed");

    Config config = (Config) getLastCustomNonConfigurationInstance();
    renderer = new ModelRenderer(Model.Type.FACE, targetView, config);
    ((ModelRenderer) renderer).getProjection().setPPFactor(0.4);
    ((ModelRenderer) renderer).setNegativeAxis(true);
    renderer.start();

    config = renderer.getConfig();
    cbVNormals.setChecked(config.isUseVNormals());
    cbVNormals.setOnCheckedChangeListener((v, c) ->
      ((ModelRenderer) renderer).setUseVNormals(c));
    // Scale configuration
    rbScale.setFactor(ModelRenderer.SCL_FACTOR);
    rbScale.setBounds(ModelRenderer.SCL_MIN, ModelRenderer.SCL_MAX);
    rbScale.setValue(config.getScale().z());
    rbScale.setOnScaledValueChangedListener(value ->
      ((ModelRenderer) renderer).setScale(value));
    // Rotation configuration
    rbRotateX.setBounds(ModelRenderer.ROT_MIN / 2, ModelRenderer.ROT_MAX / 2);
    rbRotateX.setValue((int) config.getRotate().x());
    rbRotateX.setOnValueChangedListener(value ->
      ((ModelRenderer) renderer).setRotateX(value));
    rbRotateY.setBounds(ModelRenderer.ROT_MIN / 2, ModelRenderer.ROT_MAX / 2);
    rbRotateY.setValue((int) config.getRotate().y());
    rbRotateY.setOnValueChangedListener(value ->
      ((ModelRenderer) renderer).setRotateY(value));
    rbRotateZ.setBounds(ModelRenderer.ROT_MIN / 2, ModelRenderer.ROT_MAX / 2);
    rbRotateZ.setValue((int) config.getRotate().z());
    rbRotateZ.setOnValueChangedListener(value ->
      ((ModelRenderer) renderer).setRotateZ(value));
    // Light configuration
    cbUseLamp.setChecked(config.isUseLamp());
    cbUseLamp.setOnCheckedChangeListener((v, c) -> {
      lightLayout.setVisibility(c ? View.VISIBLE : View.GONE);
      ((ModelRenderer) renderer).setUseLamp(c);
    });
    rbKd.setFactor(ModelRenderer.KD_FACTOR);
    rbKd.setBounds(ModelRenderer.KD_MIN, ModelRenderer.KD_MAX);
    rbKd.setValue(config.getKd());
    rbKd.setOnScaledValueChangedListener(value ->
      ((ModelRenderer) renderer).setKd(value));
    lightLayout.setVisibility(config.isUseLamp()
      ? View.VISIBLE : View.GONE);

    MyLog.d(LOG_TAG, "ShadingActivity started");
  }

  private void clearTransform() {
    MyLog.d(LOG_TAG, "Clearing all transformations");
    Config config = ((ModelRenderer) renderer).clearTransform();
    rbScale.setValue(config.getScale().x());
    rbRotateX.setValue((int) config.getRotate().x());
    rbRotateY.setValue((int) config.getRotate().y());
    rbRotateZ.setValue((int) config.getRotate().z());
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent i) {
    if (requestCode == REQUEST_GET_FILE) {
      if (resultCode == RESULT_OK) {
        Uri data = i.getData();
        if (data != null) {
          String path = data.getPath();
          if (path != null && path.endsWith(Model.FILE_MASK)) {
            MyLog.d(LOG_TAG, "Loading model: " + path);
            ModelLoader loader = new ModelLoader(this);
            loader.registerEventListener(this::onModelLoadEevent);
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
  }

  private void onModelLoadEevent(CallbackTask.Event event, Model model) {
    ((ModelRenderer) renderer).onModelLoaded(model);
    boolean lock = event == CallbackTask.Event.START;
    btnLoad.setText(lock ? R.string.graphics_loading
      : R.string.graphics_loadModel);
    cbVNormals.setEnabled(!lock);
    cbUseLamp.setEnabled(!lock);
    btnLoad.setEnabled(!lock);
  }
}
