package ru.didim99.tstu.ui.security;

import android.os.Bundle;
import ru.didim99.tstu.R;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 07.09.20.
 */

public class SimpleCipherActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_SCAct";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "SimpleCipherActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_simple_cipher);

    MyLog.d(LOG_TAG, "View components init...");

    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "SimpleCipherActivity created");
  }
}
