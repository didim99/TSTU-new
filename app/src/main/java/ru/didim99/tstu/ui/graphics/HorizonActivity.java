package ru.didim99.tstu.ui.graphics;

import android.os.Bundle;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.graphics.Config;
import ru.didim99.tstu.core.graphics.HorizonRenderer;
import ru.didim99.tstu.ui.graphics.view.DrawerView;
import ru.didim99.tstu.ui.graphics.view.RangeBar;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 24.02.19.
 */
public class HorizonActivity extends AnimationActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_HorizonAct";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "HorizonActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_horizon);

    MyLog.d(LOG_TAG, "View components init...");
    DrawerView targetView = findViewById(R.id.view);
    findViewById(R.id.btnAnimate).setOnClickListener(v -> renderer.animate());
    findViewById(R.id.btnClear).setOnClickListener(v -> renderer.clear());
    RangeBar rbProjection = findViewById(R.id.rbProjection);
    RangeBar rbPitch = findViewById(R.id.rbPitch);
    RangeBar rbAlpha = findViewById(R.id.rbAlpha);
    RangeBar rbBeta = findViewById(R.id.rbBeta);
    RangeBar rbGamma = findViewById(R.id.rbGamma);
    MyLog.d(LOG_TAG, "View components init competed");

    Config config = (Config) getLastCustomNonConfigurationInstance();
    renderer = new HorizonRenderer(targetView, config);
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
}
