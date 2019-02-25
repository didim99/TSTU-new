package ru.didim99.tstu.ui.graphics;

import ru.didim99.tstu.core.graphics.AsyncRenderer;
import ru.didim99.tstu.ui.BaseActivity;

/**
 * Basic class for activities with AsyncRenderer
 * Created by didim99 on 24.02.19.
 */
public abstract class AnimationActivity extends BaseActivity {
  AsyncRenderer renderer;

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    return renderer.getConfig();
  }

  @Override
  protected void onResume() {
    super.onResume();
    renderer.pause(false);
  }

  @Override
  protected void onPause() {
    super.onPause();
    renderer.pause(true);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    renderer.finish();
  }
}
