package ru.didim99.tstu.ui.graphics;

import android.os.Bundle;
import android.widget.Button;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.graphics.AsyncRenderer;
import ru.didim99.tstu.core.graphics.Config;
import ru.didim99.tstu.core.graphics.FractalRenderer;
import ru.didim99.tstu.ui.view.DrawerView;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 12.12.19.
 */
public class FractalActivity extends AnimationActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_FractalAct";

  private Button btnAnimate;
  private Button btnClear, btnReset;

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

    btnAnimate.setOnClickListener(v -> renderer.animate());
    btnClear.setOnClickListener(v -> renderer.clear());
    MyLog.d(LOG_TAG, "View components init competed");

    Config config = (Config) getLastCustomNonConfigurationInstance();
    renderer = new FractalRenderer(targetView, config);
    renderer.setEventListener(e -> uiLock(e.equals(
      AsyncRenderer.AnimationEvent.START)));
    renderer.start();



    MyLog.d(LOG_TAG, "FractalActivity started");
  }

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    btnAnimate.setEnabled(!state);
    btnClear.setEnabled(!state);
    btnReset.setEnabled(!state);

    if (state) MyLog.d(LOG_TAG, "UI locked");
    else MyLog.d(LOG_TAG, "UI unlocked");
  }
}
