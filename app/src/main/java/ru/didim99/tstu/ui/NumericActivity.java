package ru.didim99.tstu.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import ru.didim99.tstu.R;
import ru.didim99.tstu.TSTU;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.numeric.Config;
import ru.didim99.tstu.core.numeric.Result;
import ru.didim99.tstu.core.numeric.NumericTask;
import ru.didim99.tstu.core.numeric.TranscendentSolver;
import ru.didim99.tstu.utils.MyLog;

import static ru.didim99.tstu.core.numeric.Config.TaskType;

public class NumericActivity extends BaseActivity
  implements NumericTask.EventListener<Result> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_NumericAct";

  //view-elements
  private Button btnStart;
  private View pbMain;
  //main workflow
  private int type;
  private NumericTask task;
  private Result taskResult;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "NumericActivity starting...");
    type = getIntent().getIntExtra(TSTU.EXTRA_TYPE, TaskType.UNDEFINED);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_numeric);
    setupActionBar();

    MyLog.d(LOG_TAG, "Trying to connect with background task...");
    task = (NumericTask) getLastCustomNonConfigurationInstance();
    if (task == null) {
      MyLog.d(LOG_TAG, "No existing background task found");
    } else {
      task.registerEventListener(this);
      MyLog.d(LOG_TAG, "Connecting to background task completed "
        + "(" + task.hashCode() + ")");
    }

    MyLog.d(LOG_TAG, "View components init...");
    TextView title = findViewById(R.id.tvTitle);
    pbMain = findViewById(R.id.pbMain);
    btnStart = findViewById(R.id.btnStart);
    btnStart.setOnClickListener(v -> startTask());

    switch (type) {
      case TaskType.TRANSCENDENT:
        title.setText(R.string.numeric_inputEquation);
        break;
    }

    MyLog.d(LOG_TAG, "View components init completed");
  }

  @Override
  public NumericTask onRetainCustomNonConfigurationInstance() {
    if (task != null) {
      task.unregisterEventListener();
      return task;
    } else
      return null;
  }

  @Override
  public void onTaskEvent(int event, Result result) {
    switch (event) {
      case CallbackTask.Event.START:
        uiLock(true);
        break;
      case CallbackTask.Event.FINISH:
        this.taskResult = result;
        uiLock(false);
        break;
    }
  }

  @Override
  protected void onSetupActionBar(ActionBar bar) {
    switch (type) {
      case TaskType.TRANSCENDENT:
        bar.setTitle(R.string.numeric_transcendentSolver);
        break;
    }
  }

  private void startTask() {
    Config config = new Config.Builder().taskType(type)
      .solveMethod(TranscendentSolver.Method.BINARY).build();
    task = new NumericTask(getApplicationContext());
    task.registerEventListener(this);
    task.execute(config);
  }

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    btnStart.setEnabled(!state);

    if (state) {
      /*MyLog.d(LOG_TAG, "Clearing UI...");
      MyLog.d(LOG_TAG, "UI cleared");*/
      pbMain.setVisibility(View.VISIBLE);
      MyLog.d(LOG_TAG, "UI locked");
    } else {
      pbMain.setVisibility(View.INVISIBLE);
      MyLog.d(LOG_TAG, "UI unlocked");
      uiSet();
    }
  }

  private void uiSet() {
    /*MyLog.d(LOG_TAG, "Setting up UI");

    MyLog.d(LOG_TAG, "UI setup completed");*/
  }
}
