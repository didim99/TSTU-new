package ru.didim99.tstu.ui.itheory;

import android.os.Bundle;
import android.support.annotation.Nullable;
import ru.didim99.tstu.R;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 27.02.20.
 */
public class CompressionActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_CompAct";

  // View elements

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "CompressionActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_compression_text);

    MyLog.d(LOG_TAG, "View components init...");

    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "AlphabetActivity created");
  }
}
