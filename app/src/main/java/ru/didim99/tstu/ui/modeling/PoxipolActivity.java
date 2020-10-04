package ru.didim99.tstu.ui.modeling;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import ru.didim99.tstu.R;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 02.10.20.
 */

public class PoxipolActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_PXAct";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "StartActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_poxipol);

    MyLog.d(LOG_TAG, "StartActivity created");
  }

  @Override
  protected void onSetupActionBar(ActionBar bar) {
    bar.setSubtitle(R.string.modeling_poxipol);
  }
}
