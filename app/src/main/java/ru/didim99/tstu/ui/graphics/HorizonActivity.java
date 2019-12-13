package ru.didim99.tstu.ui.graphics;

import android.os.Bundle;
import android.widget.Button;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.graphics.AsyncRenderer;
import ru.didim99.tstu.core.graphics.Config;
import ru.didim99.tstu.core.graphics.HorizonRenderer;
import ru.didim99.tstu.ui.view.DrawerView;
import ru.didim99.tstu.ui.view.RangeBar;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 24.02.19.
 */
public class HorizonActivity extends AnimationActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_HorizonAct";

  private Button btnAnimate;
  private RangeBar rbProjection, rbPitch;
  private RangeBar rbAlpha, rbBeta, rbGamma;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "HorizonActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_horizon);

    MyLog.d(LOG_TAG, "View components init...");
    DrawerView targetView = findViewById(R.id.view);
    btnAnimate = findViewById(R.id.btnAnimate);
    Button btnClear = findViewById(R.id.btnClear);
    rbProjection = findViewById(R.id.rbProjection);
    rbPitch = findViewById(R.id.rbPitch);
    rbAlpha = findViewById(R.id.rbAlpha);
    rbBeta = findViewById(R.id.rbBeta);
    rbGamma = findViewById(R.id.rbGamma);
    btnAnimate.setOnClickListener(v -> renderer.animate());
    btnClear.setOnClickListener(v -> renderer.clear());
    MyLog.d(LOG_TAG, "View components init competed");

    Config config = (Config) getLastCustomNonConfigurationInstance();
    renderer = new HorizonRenderer(targetView, config);
    renderer.setEventListener(e -> uiLock(e.equals(
      AsyncRenderer.AnimationEvent.START)));
    renderer.start();

    config = renderer.getConfig();
    rbProjection.setBounds(HorizonRenderer.PROJ_MIN, HorizonRenderer.PROJ_MAX);
    rbProjection.setValue(config.getAngleZ());
    rbProjection.setOnValueChangedListener(angle ->
      ((HorizonRenderer) renderer).setProjectionAngle(angle));
    rbPitch.setBounds(HorizonRenderer.PITCH_MIN, HorizonRenderer.PITCH_MAX);
    rbPitch.setValue(config.getAngleP());
    rbPitch.setOnValueChangedListener(angle ->
      ((HorizonRenderer) renderer).setPitchAngle(angle));
    rbAlpha.setFactor(HorizonRenderer.A_FACTOR);
    rbAlpha.setBounds(HorizonRenderer.ALPHA_MIN, HorizonRenderer.ALPHA_MAX);
    rbAlpha.setValue(config.getAlpha());
    rbAlpha.setOnScaledValueChangedListener(value ->
      ((HorizonRenderer) renderer).setAlpha(value));
    rbBeta.setFactor(HorizonRenderer.B_FACTOR);
    rbBeta.setBounds(HorizonRenderer.BETA_MIN, HorizonRenderer.BETA_MAX);
    rbBeta.setValue(config.getBeta());
    rbBeta.setOnScaledValueChangedListener(value ->
      ((HorizonRenderer) renderer).setBeta(value));
    rbGamma.setFactor(HorizonRenderer.G_FACTOR);
    rbGamma.setBounds(HorizonRenderer.GAMMA_MIN, HorizonRenderer.GAMMA_MAX);
    rbGamma.setValue(config.getGamma());
    rbGamma.setOnScaledValueChangedListener(value ->
      ((HorizonRenderer) renderer).setGamma(value));

    MyLog.d(LOG_TAG, "HorizonActivity started");
  }

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    btnAnimate.setEnabled(!state);
    rbProjection.setEnabled(!state);
    rbPitch.setEnabled(!state);
    rbAlpha.setEnabled(!state);
    rbBeta.setEnabled(!state);
    rbGamma.setEnabled(!state);

    if (state) MyLog.d(LOG_TAG, "UI locked");
    else MyLog.d(LOG_TAG, "UI unlocked");
  }
}
