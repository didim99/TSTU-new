package ru.didim99.tstu.ui.graphics;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.graphics.AsyncRenderer;
import ru.didim99.tstu.core.graphics.Config;
import ru.didim99.tstu.core.graphics.FractalRenderer;
import ru.didim99.tstu.core.graphics.ModelRenderer;
import ru.didim99.tstu.core.graphics.ModelSaver;
import ru.didim99.tstu.core.graphics.utils.Model;
import ru.didim99.tstu.ui.view.DrawerView;
import ru.didim99.tstu.ui.view.RangeBar;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 12.12.19.
 */
public class FractalActivity extends AnimationActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_FractalAct";

  private static final class ControlType {
    private static final int BUILD = 0;
    private static final int SCENE = 1;
  }

  // View-elements
  private Button btnAnimate;
  private Button btnClear, btnReset;
  private Button btnBuild, btnScene;
  private View buildLayout, sceneLayout;
  private CheckBox cbDrawAxis, cbCentralBranch;
  private RangeBar rbMaxLevel, rbBranchCount;
  private RangeBar rbBranchL, rbBranchAngle;
  private RangeBar rbBranchLF, rbBranchAF;
  private RangeBar rbRotateX, rbRotateY, rbRotateZ;
  private RangeBar rbScale;
  // Workflow
  private int controlType;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "FractalActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_fractals);

    MyLog.d(LOG_TAG, "View components init...");
    DrawerView targetView = findViewById(R.id.view);
    btnAnimate = findViewById(R.id.btnAnimate);
    btnClear = findViewById(R.id.btnClear);
    btnReset = findViewById(R.id.btnReset);
    btnBuild = findViewById(R.id.btnBuild);
    btnScene = findViewById(R.id.btnScene);
    buildLayout = findViewById(R.id.buildLayout);
    sceneLayout = findViewById(R.id.sceneLayout);
    cbDrawAxis = findViewById(R.id.cbDrawAxis);
    cbCentralBranch = findViewById(R.id.cbCentralBranch);
    rbMaxLevel = findViewById(R.id.rbMaxLevel);
    rbBranchCount = findViewById(R.id.rbBranchCount);
    rbBranchL = findViewById(R.id.rbBranchL);
    rbBranchAngle = findViewById(R.id.rbBranchAngle);
    rbBranchLF = findViewById(R.id.rbBranchLF);
    rbBranchAF = findViewById(R.id.rbBranchAF);
    rbRotateX = findViewById(R.id.rbRotateX);
    rbRotateY = findViewById(R.id.rbRotateY);
    rbRotateZ = findViewById(R.id.rbRotateZ);
    rbScale = findViewById(R.id.rbScale);

    btnAnimate.setOnClickListener(v -> renderer.animate());
    btnClear.setOnClickListener(v -> renderer.clear());
    btnReset.setOnClickListener(v -> clearTransform());
    btnBuild.setOnClickListener(v -> setControlType(ControlType.BUILD));
    btnScene.setOnClickListener(v -> setControlType(ControlType.SCENE));
    setControlType(ControlType.BUILD);
    MyLog.d(LOG_TAG, "View components init competed");

    Config config = (Config) getLastCustomNonConfigurationInstance();
    renderer = new FractalRenderer(targetView, config);
    renderer.setEventListener(e -> uiLock(e.equals(
      AsyncRenderer.AnimationEvent.START)));
    renderer.start();

    config = renderer.getConfig();
    // Builder configuration
    rbMaxLevel.setBounds(FractalRenderer.LEVEL_MIN, FractalRenderer.LEVEL_MAX);
    rbMaxLevel.setValue(config.getMaxLevel());
    rbMaxLevel.setOnValueChangedListener(value ->
      ((FractalRenderer) renderer).setMaxLevel(value));
    rbBranchCount.setBounds(FractalRenderer.BCOUNT_MIN, FractalRenderer.BCOUNT_MAX);
    rbBranchCount.setValue(config.getBranchCount());
    rbBranchCount.setOnValueChangedListener(value ->
      ((FractalRenderer) renderer).setBranchCount(value));
    cbCentralBranch.setChecked(config.useCentralBranch());
    cbCentralBranch.setOnCheckedChangeListener((v, c) ->
      ((FractalRenderer) renderer).setCentralBranch(c));
    rbBranchL.setFactor(FractalRenderer.BL_FACTOR);
    rbBranchL.setBounds(FractalRenderer.BL_MIN, FractalRenderer.BL_MAX);
    rbBranchL.setValue(config.getBranchL());
    rbBranchL.setOnScaledValueChangedListener(value ->
      ((FractalRenderer) renderer).setBranchL(value));
    rbBranchAngle.setBounds(FractalRenderer.BANGLE_MIN, FractalRenderer.BANGLE_MAX);
    rbBranchAngle.setValue((int) config.getBranchAngle());
    rbBranchAngle.setOnValueChangedListener(value ->
      ((FractalRenderer) renderer).setBranchAngle(value));
    rbBranchLF.setFactor(FractalRenderer.BLF_FACTOR);
    rbBranchLF.setBounds(FractalRenderer.BLF_MIN, FractalRenderer.BLF_MAX);
    rbBranchLF.setValue(config.getBranchLF());
    rbBranchLF.setOnScaledValueChangedListener(value ->
      ((FractalRenderer) renderer).setBranchLF(value));
    rbBranchAF.setFactor(FractalRenderer.BAF_FACTOR);
    rbBranchAF.setBounds(FractalRenderer.BAF_MIN, FractalRenderer.BAF_MAX);
    rbBranchAF.setValue(config.getBranchAF());
    rbBranchAF.setOnScaledValueChangedListener(value ->
      ((FractalRenderer) renderer).setBranchAF(value));
    // Scene configuration
    cbDrawAxis.setChecked(config.isDrawAxis());
    cbDrawAxis.setOnCheckedChangeListener((v, c) ->
      ((FractalRenderer) renderer).setDrawAxis(c));
    // Scale configuration
    rbScale.setFactor(FractalRenderer.SCL_FACTOR);
    rbScale.setBounds(FractalRenderer.SCL_MIN, FractalRenderer.SCL_MAX);
    rbScale.setValue(config.getScale().x());
    rbScale.setOnScaledValueChangedListener(value ->
      ((ModelRenderer) renderer).setScale(value));
    // Rotation configuration
    rbRotateX.setBounds(FractalRenderer.ROT_MIN, FractalRenderer.ROT_MAX);
    rbRotateX.setValue((int) config.getRotate().x());
    rbRotateX.setOnValueChangedListener(value ->
      ((ModelRenderer) renderer).setRotateX(value));
    rbRotateY.setBounds(FractalRenderer.ROT_MIN, FractalRenderer.ROT_MAX);
    rbRotateY.setValue((int) config.getRotate().y());
    rbRotateY.setOnValueChangedListener(value ->
      ((ModelRenderer) renderer).setRotateY(value));
    rbRotateZ.setBounds(FractalRenderer.ROT_MIN, FractalRenderer.ROT_MAX);
    rbRotateZ.setValue((int) config.getRotate().z());
    rbRotateZ.setOnValueChangedListener(value ->
      ((ModelRenderer) renderer).setRotateZ(value));

    MyLog.d(LOG_TAG, "FractalActivity started");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_fractal, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.act_save) {
      inputFieldDialog(R.string.graphics_modelName,
        R.layout.dia_sf_text, this::saveToFile);
      return true;
    } else return super.onOptionsItemSelected(item);
  }

  private void setControlType(int type) {
    MyLog.d(LOG_TAG, "Switching to control type: " + type);
    boolean builder = type == ControlType.BUILD;
    int colorActive = getResources().getColor(R.color.colorPrimaryDark);
    int colorInactive = getResources().getColor(R.color.dirPicker_textActiveLight);
    buildLayout.setVisibility(builder ? View.VISIBLE : View.GONE);
    sceneLayout.setVisibility(builder ? View.GONE : View.VISIBLE);
    btnBuild.setTextColor(builder ? colorActive : colorInactive);
    btnScene.setTextColor(builder ? colorInactive : colorActive);
    btnReset.setText(builder ? R.string.graphics_resetBuilder
      : R.string.graphics_resetScene);
    controlType = type;
  }

  private void clearTransform() {
    MyLog.d(LOG_TAG, "Clearing all transformations");
    Config config;
    if (controlType == ControlType.BUILD) {
      config = ((FractalRenderer) renderer).resetBuilder();
      rbMaxLevel.setValue(config.getMaxLevel());
      rbBranchCount.setValue(config.getBranchCount());
      rbBranchL.setValue(config.getBranchL());
      rbBranchAngle.setValue((int) config.getBranchAngle());
      rbBranchLF.setValue(config.getBranchLF());
      rbBranchAF.setValue(config.getBranchAF());
      cbCentralBranch.setChecked(config.useCentralBranch());
    } else {
      config = ((FractalRenderer) renderer).clearTransform();
      rbScale.setValue(config.getScale().x());
      rbRotateX.setValue((int) config.getRotate().x());
      rbRotateY.setValue((int) config.getRotate().y());
      rbRotateZ.setValue((int) config.getRotate().z());
    }
  }

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    btnAnimate.setEnabled(!state);
    btnClear.setEnabled(!state);
    btnReset.setEnabled(!state);
    btnBuild.setEnabled(!state);
    btnScene.setEnabled(!state);
    cbDrawAxis.setEnabled(!state);
    cbCentralBranch.setEnabled(!state);
    rbMaxLevel.setEnabled(!state);
    rbBranchCount.setEnabled(!state);
    rbBranchL.setEnabled(!state);
    rbBranchAngle.setEnabled(!state);
    rbBranchLF.setEnabled(!state);
    rbBranchAF.setEnabled(!state);
    rbRotateX.setEnabled(!state);
    rbRotateY.setEnabled(!state);
    rbRotateZ.setEnabled(!state);
    rbScale.setEnabled(!state);

    if (state) MyLog.d(LOG_TAG, "UI locked");
    else MyLog.d(LOG_TAG, "UI unlocked");
  }

  private void saveToFile(AlertDialog dialog) {
    try {
      if (!((FractalRenderer) renderer).hasModel()) return;
      EditText input = dialog.findViewById(R.id.etInput);
      String name = InputValidator.getInstance().checkEmptyStr(input,
        R.string.errGraphics_emptyName, "Model name");
      Model model = ((FractalRenderer) renderer).getModel();
      model.setName(name);
      ModelSaver saver = new ModelSaver(this);
      saver.registerEventListener((e, msg) -> {
        boolean finished = e == CallbackTask.Event.FINISH;
        if (finished) Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        uiLock(!finished);
      });
      saver.execute(model);
      dialog.dismiss();
    } catch (InputValidator.ValidationException ignored) {}
  }
}
