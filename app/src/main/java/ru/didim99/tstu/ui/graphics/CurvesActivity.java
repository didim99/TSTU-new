package ru.didim99.tstu.ui.graphics;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.graphics.Config;
import ru.didim99.tstu.core.graphics.CurveRenderer;
import ru.didim99.tstu.ui.utils.SpinnerAdapter;
import ru.didim99.tstu.ui.view.DrawerView;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 15.02.20.
 */
public class CurvesActivity extends AnimationActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_CurvesAct";

  private TextView tvHelp;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "CurvesActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_curves);

    MyLog.d(LOG_TAG, "View components init...");
    DrawerView targetView = findViewById(R.id.view);
    Spinner spCurveType = findViewById(R.id.spCurveType);
    tvHelp = findViewById(R.id.tvHelp);
    MyLog.d(LOG_TAG, "View components init competed");

    Config config = (Config) getLastCustomNonConfigurationInstance();
    renderer = new CurveRenderer(targetView, config, getResources());
    renderer.start();

    config = renderer.getConfig();
    spCurveType.setSelection(config.getCurveType());
    spCurveType.setOnItemSelectedListener(
      new SpinnerAdapter(this::onCurveTypeChanged));
    targetView.setOnTouchListener(new PointsTouchListener(
      ((CurveRenderer) renderer)::onTouchEvent));
    ((CurveRenderer) renderer).setStateChangeListener(count ->
      tvHelp.setVisibility(count > 0 ? View.GONE : View.VISIBLE));

    MyLog.d(LOG_TAG, "CurvesActivity started");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_curves, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.act_clear) {
      renderer.clear();
      return true;
    } else return super.onOptionsItemSelected(item);
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    ((CurveRenderer) renderer).setStateChangeListener(null);
    return super.onRetainCustomNonConfigurationInstance();
  }

  private void onCurveTypeChanged(int type) {
    MyLog.d(LOG_TAG, "Curve type changed: " + type);
    ((CurveRenderer) renderer).setCurveType(type);
  }
}
