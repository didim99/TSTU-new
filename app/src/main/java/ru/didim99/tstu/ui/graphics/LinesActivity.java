package ru.didim99.tstu.ui.graphics;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.graphics.AsyncRenderer;
import ru.didim99.tstu.core.graphics.Config;
import ru.didim99.tstu.core.graphics.LineRenderer;
import ru.didim99.tstu.ui.view.DrawerView;
import ru.didim99.tstu.ui.view.RangeBar;
import ru.didim99.tstu.utils.MyLog;

public class LinesActivity extends AnimationActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_LinesAct";

  //view-elements
  private Button btnAnimate;
  private RangeBar rbAngle;
  private CheckBox cbAntiAlias;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "LinesActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_lines);

    MyLog.d(LOG_TAG, "View components init...");
    DrawerView targetView = findViewById(R.id.view);
    btnAnimate = findViewById(R.id.btnAnimate);
    Button btnClear = findViewById(R.id.btnClear);
    rbAngle = findViewById(R.id.rbAngle);
    cbAntiAlias = findViewById(R.id.cbUseAA);
    btnAnimate.setOnClickListener(v -> renderer.animate());
    btnClear.setOnClickListener(v -> renderer.clear());
    cbAntiAlias.setOnCheckedChangeListener((v, c) -> {
      ((LineRenderer) renderer).setAntiAlias(c);
      btnAnimate.setEnabled(!c); });
    MyLog.d(LOG_TAG, "View components init competed");

    Config config = (Config) getLastCustomNonConfigurationInstance();
    renderer = new LineRenderer(targetView, config);
    renderer.setEventListener(e -> uiLock(e.equals(
      AsyncRenderer.AnimationEvent.START)));
    renderer.start();

    rbAngle.setBounds(LineRenderer.ANGLE_MIN, LineRenderer.ANGLE_MAX);
    rbAngle.setValue(renderer.getConfig().getAngle());
    rbAngle.setOnValueChangedListener(value ->
      ((LineRenderer) renderer).setAngle(value));

    MyLog.d(LOG_TAG, "LinesActivity started");
  }

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    btnAnimate.setEnabled(!state
      && !cbAntiAlias.isChecked());
    rbAngle.setEnabled(!state);
    cbAntiAlias.setEnabled(!state);

    if (state) MyLog.d(LOG_TAG, "UI locked");
    else MyLog.d(LOG_TAG, "UI unlocked");
  }
}
