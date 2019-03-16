package ru.didim99.tstu.ui.graphics;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.graphics.Config;
import ru.didim99.tstu.core.graphics.EdgeRenderer;
import ru.didim99.tstu.core.graphics.EdgeRenderer.TransformType;
import ru.didim99.tstu.core.graphics.ModelLoader;
import ru.didim99.tstu.core.graphics.utils.EdgeModel;
import ru.didim99.tstu.core.graphics.utils.Projection;
import ru.didim99.tstu.ui.dirpicker.DirPickerActivity;
import ru.didim99.tstu.ui.view.DrawerView;
import ru.didim99.tstu.ui.view.RangeBar;
import ru.didim99.tstu.utils.MyLog;

public class TransformActivity extends AnimationActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_TransAct";
  private static final int REQUEST_GET_FILE = 1;

  // View-elements
  private View ppConfigLayout;
  private View cpConfigLayout;
  private View translateLayout;
  private View scaleLayout;
  private View rotateLayout;
  private CheckBox cbNegativeAxis;
  private CheckBox cbPointX, cbPointY, cbPointZ;
  private RangeBar rbTranslateX, rbTranslateY, rbTranslateZ;
  private RangeBar rbScaleX, rbScaleY, rbScaleZ;
  private RangeBar rbRotateX, rbRotateY, rbRotateZ;
  // Workflow
  private boolean syncScale;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "TransformActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_transform);

    MyLog.d(LOG_TAG, "View components init...");
    DrawerView targetView = findViewById(R.id.view);
    ppConfigLayout = findViewById(R.id.ppConfig);
    cpConfigLayout = findViewById(R.id.cpConfig);
    translateLayout = findViewById(R.id.translateConfig);
    scaleLayout = findViewById(R.id.scaleConfig);
    rotateLayout = findViewById(R.id.rotateConfig);
    cbPointX = findViewById(R.id.cbPointX);
    cbPointY = findViewById(R.id.cbPointY);
    cbPointZ = findViewById(R.id.cbPointZ);
    cbNegativeAxis = findViewById(R.id.cbNegativeAxis);
    CheckBox cbDrawAxis = findViewById(R.id.cbDrawAxis);
    CheckBox cbSyncScale = findViewById(R.id.cbSyncScale);
    RangeBar rbPPAngle = findViewById(R.id.rbParallelAngle);
    RangeBar rbPPFactor = findViewById(R.id.rbParallelFactor);
    RangeBar rbCPDistance = findViewById(R.id.rbCentralDistance);
    rbTranslateX = findViewById(R.id.rbTranslateX);
    rbTranslateY = findViewById(R.id.rbTranslateY);
    rbTranslateZ = findViewById(R.id.rbTranslateZ);
    rbScaleX = findViewById(R.id.rbScaleX);
    rbScaleY = findViewById(R.id.rbScaleY);
    rbScaleZ = findViewById(R.id.rbScaleZ);
    rbRotateX = findViewById(R.id.rbRotateX);
    rbRotateY = findViewById(R.id.rbRotateY);
    rbRotateZ = findViewById(R.id.rbRotateZ);
    Spinner spPType = findViewById(R.id.spProjectionType);
    Spinner spTType = findViewById(R.id.spTransformType);
    findViewById(R.id.btnClear).setOnClickListener(
      v -> ((EdgeRenderer) renderer).clearScene());
    findViewById(R.id.btnLoad).setOnClickListener(v -> openFile());
    findViewById(R.id.btnReset).setOnClickListener(v -> clearTransform());
    MyLog.d(LOG_TAG, "View components init competed");

    Config config = (Config) getLastCustomNonConfigurationInstance();
    renderer = new EdgeRenderer(targetView, config);
    renderer.start();

    config = renderer.getConfig();
    Projection projection = ((EdgeRenderer) renderer).getProjection();
    // Projection configuration
    rbPPAngle.setBounds(Projection.PPA_MIN, Projection.PPA_MAX);
    rbPPAngle.setValue(projection.getPPAngle());
    rbPPAngle.setOnValueChangedListener(value ->
      ((EdgeRenderer) renderer).getProjection().setPPAngle(value));
    rbPPFactor.setFactor(Projection.PPF_FACTOR);
    rbPPFactor.setBounds(Projection.PPF_MIN, Projection.PPF_MAX);
    rbPPFactor.setValue(projection.getPPFactor());
    rbPPFactor.setOnScaledValueChangedListener(value ->
      ((EdgeRenderer) renderer).getProjection().setPPFactor(value));
    rbCPDistance.setBounds(Projection.CPD_MIN, Projection.CPD_MAX);
    rbCPDistance.setValue(projection.getCPDistance());
    rbCPDistance.setOnValueChangedListener(value ->
      ((EdgeRenderer) renderer).getProjection().setCPDistance(value));
    // Translation configuration
    rbTranslateX.setFactor(EdgeRenderer.TRN_FACTOR);
    rbTranslateX.setBounds(EdgeRenderer.TRN_MIN, EdgeRenderer.TRN_MAX);
    rbTranslateX.setValue(config.getTranslate().x());
    rbTranslateX.setOnScaledValueChangedListener(value ->
      ((EdgeRenderer) renderer).setTranslateX(value));
    rbTranslateY.setFactor(EdgeRenderer.TRN_FACTOR);
    rbTranslateY.setBounds(EdgeRenderer.TRN_MIN, EdgeRenderer.TRN_MAX);
    rbTranslateY.setValue(config.getTranslate().y());
    rbTranslateY.setOnScaledValueChangedListener(value ->
      ((EdgeRenderer) renderer).setTranslateY(value));
    rbTranslateZ.setFactor(EdgeRenderer.TRN_FACTOR);
    rbTranslateZ.setBounds(EdgeRenderer.TRN_MIN, EdgeRenderer.TRN_MAX);
    rbTranslateZ.setValue(config.getTranslate().z());
    rbTranslateZ.setOnScaledValueChangedListener(value ->
      ((EdgeRenderer) renderer).setTranslateZ(value));
    // Scale configuration
    rbScaleX.setFactor(EdgeRenderer.SCL_FACTOR);
    rbScaleX.setBounds(EdgeRenderer.SCL_MIN, EdgeRenderer.SCL_MAX);
    rbScaleX.setValue(config.getScale().x());
    rbScaleX.setOnScaledValueChangedListener(value -> {
      if (syncScale) onSceneScaleChanged(value);
      else ((EdgeRenderer) renderer).setScaleX(value);
    });
    rbScaleY.setFactor(EdgeRenderer.SCL_FACTOR);
    rbScaleY.setBounds(EdgeRenderer.SCL_MIN, EdgeRenderer.SCL_MAX);
    rbScaleY.setValue(config.getScale().y());
    rbScaleY.setOnScaledValueChangedListener(value -> {
      if (syncScale) onSceneScaleChanged(value);
      else ((EdgeRenderer) renderer).setScaleY(value);
    });
    rbScaleZ.setFactor(EdgeRenderer.SCL_FACTOR);
    rbScaleZ.setBounds(EdgeRenderer.SCL_MIN, EdgeRenderer.SCL_MAX);
    rbScaleZ.setValue(config.getScale().z());
    rbScaleZ.setOnScaledValueChangedListener(value -> {
      if (syncScale) onSceneScaleChanged(value);
      else ((EdgeRenderer) renderer).setScaleZ(value);
    });
    // Rotation configuration
    rbRotateX.setBounds(EdgeRenderer.ROT_MIN, EdgeRenderer.ROT_MAX);
    rbRotateX.setValue((int) config.getRotate().x());
    rbRotateX.setOnValueChangedListener(value ->
      ((EdgeRenderer) renderer).setRotateX(value));
    rbRotateY.setBounds(EdgeRenderer.ROT_MIN, EdgeRenderer.ROT_MAX);
    rbRotateY.setValue((int) config.getRotate().y());
    rbRotateY.setOnValueChangedListener(value ->
      ((EdgeRenderer) renderer).setRotateY(value));
    rbRotateZ.setBounds(EdgeRenderer.ROT_MIN, EdgeRenderer.ROT_MAX);
    rbRotateZ.setValue((int) config.getRotate().z());
    rbRotateZ.setOnValueChangedListener(value ->
      ((EdgeRenderer) renderer).setRotateZ(value));

    spPType.setSelection(projection.getType());
    spPType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        ppConfigLayout.setVisibility(
          pos == Projection.Type.PARALLEL ? View.VISIBLE : View.GONE);
        cpConfigLayout.setVisibility(
          pos == Projection.Type.CENTRAL ? View.VISIBLE : View.GONE);
        ((EdgeRenderer) renderer).getProjection().applyType(pos);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {}
    });

    spTType.setSelection(EdgeRenderer.TransformType.TRANSLATE);
    spTType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        translateLayout.setVisibility(
          pos == TransformType.TRANSLATE ? View.VISIBLE : View.GONE);
        scaleLayout.setVisibility(
          pos == TransformType.SCALE ? View.VISIBLE : View.GONE);
        rotateLayout.setVisibility(
          pos == TransformType.ROTATE ? View.VISIBLE : View.GONE);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {}
    });

    cbNegativeAxis.setEnabled(config.isDrawAxis());
    cbNegativeAxis.setChecked(config.isNegativeAxis());
    cbNegativeAxis.setOnCheckedChangeListener((v, c) ->
      ((EdgeRenderer) renderer).setNegativeAxis(c));
    cbDrawAxis.setChecked(config.isDrawAxis());
    cbDrawAxis.setOnCheckedChangeListener((v, c) -> {
      MyLog.d(LOG_TAG, "Draw axis: " + c);
      ((EdgeRenderer) renderer).setDrawAxis(c);
      cbNegativeAxis.setEnabled(c);
    });

    cbPointX.setChecked(projection.isUseX());
    cbPointY.setChecked(projection.isUseY());
    cbPointZ.setChecked(projection.isUseZ());
    cbPointX.setOnCheckedChangeListener((v, c) -> onPointCountChanged());
    cbPointY.setOnCheckedChangeListener((v, c) -> onPointCountChanged());
    cbPointZ.setOnCheckedChangeListener((v, c) -> onPointCountChanged());
    onPointCountChanged();

    syncScale = config.isSyncScale();
    cbSyncScale.setChecked(syncScale);
    cbSyncScale.setOnCheckedChangeListener((v, c) -> {
      MyLog.d(LOG_TAG, "Sync scale: " + c);
      ((EdgeRenderer) renderer).setSyncScale(syncScale = c);
      if (c) onSceneScaleChanged(rbScaleX.getValue());
    });

    MyLog.d(LOG_TAG, "TransformActivity started");
  }

  private void clearTransform() {
    MyLog.d(LOG_TAG, "Clearing all transformations");
    Config config = ((EdgeRenderer) renderer).clearTransform();
    rbTranslateX.setValue(config.getTranslate().x());
    rbTranslateY.setValue(config.getTranslate().y());
    rbTranslateZ.setValue(config.getTranslate().z());
    rbScaleX.setValue(config.getScale().x());
    rbScaleY.setValue(config.getScale().y());
    rbScaleZ.setValue(config.getScale().z());
    rbRotateX.setValue((int) config.getRotate().x());
    rbRotateY.setValue((int) config.getRotate().y());
    rbRotateZ.setValue((int) config.getRotate().z());
  }

  private void onPointCountChanged() {
    MyLog.d(LOG_TAG, "Central projection points changed");
    boolean useX = cbPointX.isChecked();
    boolean useY = cbPointY.isChecked();
    boolean useZ = cbPointZ.isChecked();
    int count = 0;
    if (useX) count++;
    if (useY) count++;
    if (useZ) count++;
    boolean one = count == 1;
    cbPointX.setEnabled(!one || !useX);
    cbPointY.setEnabled(!one || !useY);
    cbPointZ.setEnabled(!one || !useZ);
    ((EdgeRenderer) renderer).getProjection()
      .setPoints(useX, useY, useZ);
  }

  private void onSceneScaleChanged(double value) {
    ((EdgeRenderer) renderer).setScale(value);
    rbScaleX.setValue(value);
    rbScaleY.setValue(value);
    rbScaleZ.setValue(value);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent i) {
    if (requestCode == REQUEST_GET_FILE) {
      if (resultCode == RESULT_OK) {
        Uri data = i.getData();
        if (data != null) {
          String path = data.getPath();
          if (path != null && path.endsWith(EdgeModel.FILE_MASK)) {
            MyLog.d(LOG_TAG, "Loading model: " + path);
            ModelLoader loader = new ModelLoader(this);
            loader.registerEventListener((event, model) ->
              ((EdgeRenderer) renderer).onModelLoaded(model));
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

  private void openFile() {
    MyLog.d(LOG_TAG, "Choose file from DirPicker...");
    Intent intent = new Intent(this, DirPickerActivity.class);
    intent.putExtra(DirPickerActivity.KEY_MODE, DirPickerActivity.Mode.FILE);
    startActivityForResult(intent, REQUEST_GET_FILE);
  }
}
