package ru.didim99.tstu.ui.graphics;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.graphics.Config;
import ru.didim99.tstu.core.graphics.LineRenderer;
import ru.didim99.tstu.ui.view.DrawerView;
import ru.didim99.tstu.ui.view.RangeBar;
import ru.didim99.tstu.utils.MyLog;

public class LinesActivity extends AnimationActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_LinesAct";

  //view-elements
  private Button btnAnimate;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "LinesActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_lines);

    MyLog.d(LOG_TAG, "View components init...");
    btnAnimate = findViewById(R.id.btnAnimate);
    RangeBar rbAngle = findViewById(R.id.rbAngle);
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

    rbAngle.setBounds(LineRenderer.ANGLE_MIN, LineRenderer.ANGLE_MAX);
    rbAngle.setValue(renderer.getConfig().getAngle());
    rbAngle.setOnValueChangedListener(value ->
      ((LineRenderer) renderer).setAngle(value));

    MyLog.d(LOG_TAG, "LinesActivity started");
  }
}
