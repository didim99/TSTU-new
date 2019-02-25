package ru.didim99.tstu.ui.graphics;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.graphics.Config;
import ru.didim99.tstu.core.graphics.HorizonRenderer;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 24.02.19.
 */
public class HorizonActivity extends AnimationActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_HorizonAct";

  private TextView tvProj;
  private TextView tvPitch;
  private TextView tvAlpha;
  private TextView tvBeta;
  private TextView tvGamma;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "HorizonActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_horizon);

    MyLog.d(LOG_TAG, "View components init...");
    DrawerView targetView = findViewById(R.id.view);
    findViewById(R.id.btnAnimate).setOnClickListener(v -> renderer.animate());
    findViewById(R.id.btnClear).setOnClickListener(v -> renderer.clear());
    SeekBar sbProj = findViewById(R.id.sbAngleProjection);
    SeekBar sbPitch = findViewById(R.id.sbAnglePitch);
    SeekBar sbAlpha = findViewById(R.id.sbAlpha);
    SeekBar sbBeta = findViewById(R.id.sbBeta);
    SeekBar sbGamma = findViewById(R.id.sbGamma);
    tvProj = findViewById(R.id.tvAngleProjection);
    tvPitch = findViewById(R.id.tvAnglePitch);
    tvAlpha = findViewById(R.id.tvAlpha);
    tvBeta = findViewById(R.id.tvBeta);
    tvGamma = findViewById(R.id.tvGamma);
    MyLog.d(LOG_TAG, "View components init competed");

    Config config = (Config) getLastCustomNonConfigurationInstance();
    renderer = new HorizonRenderer(targetView, config);
    renderer.execute();

    config = renderer.getConfig();
    tvProj.setText(getString(R.string.graphics_projection, config.getAngleZ()));
    tvPitch.setText(getString(R.string.graphics_angle, config.getAngleP()));
    tvAlpha.setText(getString(R.string.graphics_alpha, config.getAlpha()));
    tvBeta.setText(getString(R.string.graphics_beta, config.getBeta()));
    tvGamma.setText(getString(R.string.graphics_gamma, config.getGamma()));
    sbProj.setMax(HorizonRenderer.PROJ_MAX - HorizonRenderer.PROJ_MIN);
    sbProj.setProgress(config.getAngleZ() - HorizonRenderer.PROJ_MIN);
    sbPitch.setMax(HorizonRenderer.PITCH_MAX - HorizonRenderer.PITCH_MIN);
    sbPitch.setProgress(config.getAngleP() - HorizonRenderer.PITCH_MIN);
    sbAlpha.setMax(HorizonRenderer.ALPHA_MAX - HorizonRenderer.ALPHA_MIN);
    sbAlpha.setProgress((int) (config.getAlpha()
      / HorizonRenderer.A_FACTOR) - HorizonRenderer.ALPHA_MIN);
    sbBeta.setMax(HorizonRenderer.BETA_MAX - HorizonRenderer.BETA_MIN);
    sbBeta.setProgress((int) (config.getBeta()
      / HorizonRenderer.B_FACTOR) - HorizonRenderer.BETA_MIN);
    sbGamma.setMax(HorizonRenderer.GAMMA_MAX - HorizonRenderer.GAMMA_MIN);
    sbGamma.setProgress((int) (config.getGamma()
      / HorizonRenderer.G_FACTOR) - HorizonRenderer.GAMMA_MIN);

    sbProj.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onStopTrackingTouch(SeekBar seekBar) {}
      @Override public void onStartTrackingTouch(SeekBar seekBar) {}

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int angle = HorizonRenderer.PROJ_MIN + progress;
        tvProj.setText(getString(R.string.graphics_projection, angle));
        ((HorizonRenderer) renderer).setProjectionAngle(angle);
      }
    });

    sbPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onStopTrackingTouch(SeekBar seekBar) {}
      @Override public void onStartTrackingTouch(SeekBar seekBar) {}

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int angle = HorizonRenderer.PITCH_MIN + progress;
        tvPitch.setText(getString(R.string.graphics_angle, angle));
        ((HorizonRenderer) renderer).setPitchAngle(angle);
      }
    });

    sbAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onStopTrackingTouch(SeekBar seekBar) {}
      @Override public void onStartTrackingTouch(SeekBar seekBar) {}

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        double value = (HorizonRenderer.ALPHA_MIN + progress) * HorizonRenderer.A_FACTOR;
        tvAlpha.setText(getString(R.string.graphics_alpha, value));
        ((HorizonRenderer) renderer).setAlpha(value);
      }
    });

    sbBeta.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onStopTrackingTouch(SeekBar seekBar) {}
      @Override public void onStartTrackingTouch(SeekBar seekBar) {}

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        double value = (HorizonRenderer.BETA_MIN + progress) * HorizonRenderer.B_FACTOR;
        tvBeta.setText(getString(R.string.graphics_beta, value));
        ((HorizonRenderer) renderer).setBeta(value);
      }
    });

    sbGamma.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onStopTrackingTouch(SeekBar seekBar) {}
      @Override public void onStartTrackingTouch(SeekBar seekBar) {}

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        double value = (HorizonRenderer.GAMMA_MIN + progress) * HorizonRenderer.G_FACTOR;
        tvGamma.setText(getString(R.string.graphics_gamma, value));
        ((HorizonRenderer) renderer).setGamma(value);
      }
    });

    MyLog.d(LOG_TAG, "HorizonActivity started");
  }
}
