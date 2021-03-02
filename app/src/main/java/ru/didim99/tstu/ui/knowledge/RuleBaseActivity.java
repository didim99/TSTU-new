package ru.didim99.tstu.ui.knowledge;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.knowledge.rulebase.RuleBase;
import ru.didim99.tstu.core.knowledge.rulebase.RuleBaseLoader;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 28.02.21.
 */

public class RuleBaseActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_RBAct";

  // View-elements
  private Button btnLoad;
  // Workflow
  private RuleBase ruleBase;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "RuleBaseActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_rule_base);

    MyLog.d(LOG_TAG, "View components init...");
    btnLoad = findViewById(R.id.btnLoad);

    btnLoad.setOnClickListener(v -> openFile());
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "RuleBaseActivity started");
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent i) {
    if (requestCode == REQUEST_GET_FILE) {
      if (resultCode == RESULT_OK) {
        Uri data = i.getData();
        if (data != null) {
          String path = data.getPath();
          if (path != null && path.endsWith(RuleBase.FILE_MASK)) {
            MyLog.d(LOG_TAG, "Loading rule base: " + path);
            RuleBaseLoader loader = new RuleBaseLoader(this);
            loader.registerEventListener(this::onBaseLoadEvent);
            loader.execute(path);
          } else {
            Toast.makeText(this, R.string.errGeneric_incorrectType,
              Toast.LENGTH_LONG).show();
          }
        }
      }

      else if (resultCode == RESULT_CANCELED)
        MyLog.d(LOG_TAG, "Choosing path aborted");
    }
  }

  private void onBaseLoadEvent(CallbackTask.Event event, RuleBase ruleBase) {
    boolean lock = event == CallbackTask.Event.START;
    btnLoad.setText(lock ? R.string.loading : R.string.buttonLoad);
    btnLoad.setEnabled(!lock);
    if (ruleBase != null)
      this.ruleBase = ruleBase;
  }
}
