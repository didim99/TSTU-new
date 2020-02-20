package ru.didim99.tstu.ui.graphics;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.graphics.Config;
import ru.didim99.tstu.core.graphics.CurveRenderer;
import ru.didim99.tstu.core.graphics.curve.Curve;
import ru.didim99.tstu.ui.utils.SpinnerAdapter;
import ru.didim99.tstu.ui.view.DrawerView;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 15.02.20.
 */
public class CurvesActivity extends AnimationActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_CurvesAct";
  private static final int MENU_HIDE_MS = 300;

  private TextView tvHelp;
  private CheckBox cbSyncControls;
  private LinearLayout menuLayout;
  private ImageView ivHideMenu;
  private boolean menuHidden;
  private int menuOffset, endW;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "CurvesActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_curves);
    menuHidden = false;

    MyLog.d(LOG_TAG, "View components init...");
    DrawerView targetView = findViewById(R.id.view);
    Spinner spCurveType = findViewById(R.id.spCurveType);
    CheckBox cbDrawPoints = findViewById(R.id.cbCurvePoints);
    CheckBox cbDrawFrame = findViewById(R.id.cbCurveFrame);
    cbSyncControls = findViewById(R.id.cbCurveSync);
    menuLayout = findViewById(R.id.menuLayout);
    ivHideMenu = findViewById(R.id.ivHideMenu);
    tvHelp = findViewById(R.id.tvHelp);
    ivHideMenu.setOnClickListener(this::toggleMenuState);
    MyLog.d(LOG_TAG, "View components init competed");

    Config config = (Config) getLastCustomNonConfigurationInstance();
    renderer = new CurveRenderer(targetView, config, getResources());
    renderer.start();

    config = renderer.getConfig();
    Curve curve = config.getCurve();
    // Curve configuration
    spCurveType.setSelection(curve.getType());
    spCurveType.setOnItemSelectedListener(
      new SpinnerAdapter(this::onCurveTypeChanged));
    // Drawing configuration
    cbDrawPoints.setChecked(curve.isDrawPoints());
    cbDrawPoints.setOnCheckedChangeListener((v, c) ->
      ((CurveRenderer) renderer).setDrawPoints(c));
    cbDrawFrame.setChecked(curve.isDrawFrame());
    cbDrawFrame.setOnCheckedChangeListener((v, c) ->
      ((CurveRenderer) renderer).setDrawFrame(c));
    cbSyncControls.setChecked(curve.isSyncControls());
    cbSyncControls.setOnCheckedChangeListener((v, c) ->
      ((CurveRenderer) renderer).setSyncControls(c));
    // Events handling
    targetView.setOnTouchListener(new PointsTouchListener(
      ((CurveRenderer) renderer)::onTouchEvent));
    ((CurveRenderer) renderer).setStateChangeListener(count ->
      tvHelp.setVisibility(count > 0 ? View.GONE : View.VISIBLE));
    onCurveTypeChanged(spCurveType.getSelectedItemPosition());

    MyLog.d(LOG_TAG, "CurvesActivity started");
  }

  private void toggleMenuState(View button) {
    MyLog.d(LOG_TAG, menuHidden ? "Show main menu" : "Hide main menu");
    if (menuOffset == 0) menuOffset = -menuLayout.getWidth();
    int start = menuHidden ? menuOffset : 0,
        end = endW = menuHidden ? 0 : menuOffset;
    if (menuHidden) menuLayout.setVisibility(View.VISIBLE);
    button.setRotationY(menuHidden ? 0 : 180);
    ValueAnimator animator = ValueAnimator.ofInt(start, end);
    animator.addUpdateListener(this::onAnimationFrame);
    animator.setDuration(MENU_HIDE_MS);
    ivHideMenu.setEnabled(false);
    menuHidden = !menuHidden;
    animator.start();
  }

  private void onAnimationFrame(ValueAnimator animator) {
    int value = (Integer) animator.getAnimatedValue();
    menuLayout.setTranslationX(value);
    ivHideMenu.setTranslationX(value);
    if (value == endW) {
      ivHideMenu.setEnabled(true);
      if (menuHidden) {
        menuLayout.setVisibility(View.GONE);
        ivHideMenu.setTranslationX(0);
      }
    }
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
    cbSyncControls.setEnabled(renderer.getConfig()
      .getCurve().hasControlPoints());
  }
}
