package ru.didim99.tstu.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.graphics.AsyncRenderer;
import ru.didim99.tstu.core.graphics.Config;
import ru.didim99.tstu.core.graphics.LineRenderer;
import ru.didim99.tstu.utils.MyLog;

public class GraphicsActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_GraphicsAct";

  private AsyncRenderer r;
  //view-elements
  private Button btnAnimate;
  private TextView tvAngle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "GraphicsActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_graphics);
    setupActionBar();

    MyLog.d(LOG_TAG, "View components init...");
    btnAnimate = findViewById(R.id.btnAnimate);
    tvAngle = findViewById(R.id.tvAngle);
    SeekBar sbAngle = findViewById(R.id.sbAngle);
    DrawerView targetView = findViewById(R.id.view);
    btnAnimate.setOnClickListener(v -> r.animate());
    findViewById(R.id.btnClear).setOnClickListener(v -> r.clear());
    ((CheckBox) findViewById(R.id.cbUseAA)).setOnCheckedChangeListener((v, c) -> {
      ((LineRenderer) r).setAntiAlias(c);
      btnAnimate.setEnabled(!c);
    });
    MyLog.d(LOG_TAG, "View components init competed");

    Config config = (Config) getLastCustomNonConfigurationInstance();
    r = new LineRenderer(targetView, config);
    r.execute();

    config = r.getConfig();
    tvAngle.setText(getString(R.string.graphics_angle, config.getAngle()));
    sbAngle.setMax(LineRenderer.ANGLE_MAX - LineRenderer.ANGLE_MIN);
    sbAngle.setProgress(config.getAngle() - LineRenderer.ANGLE_MIN);
    sbAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int angle = LineRenderer.ANGLE_MIN + progress;
        tvAngle.setText(getString(R.string.graphics_angle, angle));
        ((LineRenderer) r).setAngle(angle);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {}

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });

    MyLog.d(LOG_TAG, "GraphicsActivity started");
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    return r.getConfig();
  }

  @Override
  protected void onResume() {
    super.onResume();
    r.pause(false);
  }

  @Override
  protected void onPause() {
    super.onPause();
    r.pause(true);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    r.finish();
  }

  @Override
  protected void onSetupActionBar(ActionBar bar) {
    bar.setTitle(R.string.graphics_brezenhem);
  }
}
