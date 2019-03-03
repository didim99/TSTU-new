package ru.didim99.tstu.ui.mp.lab2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.view.Menu;
import android.view.MenuItem;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.mp.CameraManager;
import ru.didim99.tstu.ui.BaseActivity;

public abstract class CameraActivity extends BaseActivity {

  Class actPrev, actNext;
  CameraManager cm;

  @Override
  @CallSuper
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    cm = CameraManager.getInstance().init(getApplicationContext());
  }

  @Override
  protected void onResume() {
    super.onResume();
    cm.attachToView();
  }

  @Override
  protected void onPause() {
    super.onPause();
    cm.detachFromView();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_mp_cam, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.act_prev:
        switchActivity(actPrev);
        return true;
      case R.id.act_next:
        switchActivity(actNext);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void switchActivity(Class target) {
    startActivity(new Intent(this, target));
    finish();
  }
}
