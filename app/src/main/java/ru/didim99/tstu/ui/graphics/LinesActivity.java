package ru.didim99.tstu.ui.graphics;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.graphics.Config;
import ru.didim99.tstu.core.graphics.LineRenderer;
import ru.didim99.tstu.ui.view.DrawerView;
import ru.didim99.tstu.utils.MyLog;

public class LinesActivity extends AnimationActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_LinesAct";

  //view-elements
  private Button btnAnimate;
  private TextView tvAngle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "LinesActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_lines);

    MyLog.d(LOG_TAG, "View components init...");
    btnAnimate = findViewById(R.id.btnAnimate);
    tvAngle = findViewById(R.id.tvAngle);
    SeekBar sbAngle = findViewById(R.id.sbAngle);
    DrawerView targetView = findViewById(R.id.view);
    btnAnimate.setOnClickListener(v -> renderer.animate());
    findViewById(R.id.btnClear).setOnClickListener(v -> renderer.clear());
    ((CheckBox) findViewById(R.id.cbUseAA)).setOnCheckedChangeListener((v, c) -> {
      ((LineRenderer) renderer).setAntiAlias(c);
      btnAnimate.setEnabled(!c);
    });
    MyLog.d(LOG_TAG, "View components init competed");

    Config config = (Config) getLastCustomNonConfigurationInstance();
    renderer = new LineRenderer(targetView, config);
    renderer.start();

    config = renderer.getConfig();
    tvAngle.setText(getString(R.string.graphics_angle, config.getAngle()));
    sbAngle.setMax(LineRenderer.ANGLE_MAX - LineRenderer.ANGLE_MIN);
    sbAngle.setProgress(config.getAngle() - LineRenderer.ANGLE_MIN);
    sbAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onStartTrackingTouch(SeekBar seekBar) {}
      @Override public void onStopTrackingTouch(SeekBar seekBar) {}

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int angle = LineRenderer.ANGLE_MIN + progress;
        tvAngle.setText(getString(R.string.graphics_angle, angle));
        ((LineRenderer) renderer).setAngle(angle);
      }
    });

    MyLog.d(LOG_TAG, "LinesActivity started");
  }
}
