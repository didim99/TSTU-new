package ru.didim99.tstu.ui.knowledge;

import android.os.Bundle;
import ru.didim99.tstu.R;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 28.02.21.
 */

public class RuleBaseActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_RBAct";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "RuleBaseActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_rule_base);

    MyLog.d(LOG_TAG, "View components init...");

    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "RuleBaseActivity started");
  }
}
