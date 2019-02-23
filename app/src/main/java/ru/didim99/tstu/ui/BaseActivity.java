package ru.didim99.tstu.ui;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Basic Activity with common UI features
 * Created by didim99 on 16.09.18.
 */
public abstract class BaseActivity extends AppCompatActivity {

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
}
