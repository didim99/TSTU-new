package ru.didim99.tstu.ui;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.MyLog;

/**
 * Basic Activity with common UI features
 * Created by didim99 on 16.09.18.
 */
public abstract class BaseActivity extends AppCompatActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_BaseAct";

  //main workflow
  protected boolean disableBackBtn = false;
  protected boolean uiLocked = false;

  @Override
  @CallSuper
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setupActionBar();
  }

  @Override
  public final void onBackPressed() {
    if (!uiLocked)
      super.onBackPressed();
  }

  @Override
  @CallSuper
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home)
      onBackPressed();
    return false;
  }

  private void setupActionBar() {
    ActionBar bar = getSupportActionBar();
    if (bar != null) {
      onSetupActionBar(bar);
      if (!disableBackBtn) {
        bar.setDisplayShowHomeEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
      }
    }
  }

  protected void onSetupActionBar(ActionBar bar) {}

  protected void inputFieldDialog(@StringRes int titleId, @LayoutRes int viewId,
                                  DialogEventListener listener) {
    MyLog.d(LOG_TAG, "Field dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(titleId);
    adb.setView(viewId);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    MyLog.d(LOG_TAG, "Field dialog created");
    AlertDialog dialog = adb.create();
    dialog.setOnShowListener((di) -> {
      AlertDialog d = (AlertDialog) di;
      d.getButton(AlertDialog.BUTTON_POSITIVE)
        .setOnClickListener(v -> listener.onEvent(d));
    });
    dialog.show();
  }
}
