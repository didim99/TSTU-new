package ru.didim99.tstu.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import ru.didim99.tstu.R;
import ru.didim99.tstu.TSTU;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.optimization.Config;
import ru.didim99.tstu.core.optimization.ExtremaFinder;
import ru.didim99.tstu.core.optimization.OptTask;
import ru.didim99.tstu.core.optimization.Result;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 05.09.19.
 */
public class OptimizationActivity extends BaseActivity
  implements CallbackTask.EventListener<ArrayList<Result>> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_OptAct";

  //view-elements
  private ImageView plotView;
  private TextListAdapter adapter;
  private Button btnStart;
  private View pbMain;
  //main workflow
  private int type;
  private OptTask task;
  private ArrayList<Result> taskResult;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "OptimizationActivity starting...");
    type = getIntent().getIntExtra(TSTU.EXTRA_TYPE, Config.TaskType.UNDEFINED);
    super.onCreate(savedInstanceState);
    if (type == Config.TaskType.SINGLE_ARG) {
      setContentView(R.layout.act_optimization);
    } else {
      setContentView(R.layout.act_optimization_graph);
    }

    MyLog.d(LOG_TAG, "View components init...");
    TextView title = findViewById(R.id.tvTitle);
    RecyclerView rvOut = findViewById(R.id.rvOut);
    plotView = findViewById(R.id.plotView);
    btnStart = findViewById(R.id.btnStart);
    pbMain = findViewById(R.id.pbMain);
    btnStart.setOnClickListener(v -> startTask());

    switch (type) {
      case Config.TaskType.SINGLE_ARG:
        title.setText(getString(R.string.opt_localExtrema_function,
          ExtremaFinder.DEF_START, ExtremaFinder.DEF_END));
        adapter = new TextListAdapter(this);
        rvOut.setLayoutManager(new LinearLayoutManager(
          this, RecyclerView.HORIZONTAL, false));
        rvOut.setAdapter(adapter);
        break;
      case Config.TaskType.ZERO_ORDER:

        break;
    }

    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "Trying to connect with background task...");
    task = (OptTask) getLastCustomNonConfigurationInstance();
    if (task == null) {
      MyLog.d(LOG_TAG, "No existing background task found");
    } else {
      task.registerEventListener(this);
      MyLog.d(LOG_TAG, "Connecting to background task completed "
        + "(" + task.hashCode() + ")");
    }
  }

  @Override
  public OptTask onRetainCustomNonConfigurationInstance() {
    if (task != null) {
      task.unregisterEventListener();
      return task;
    } else
      return null;
  }

  @Override
  public void onTaskEvent(CallbackTask.Event event, ArrayList<Result> result) {
    switch (event) {
      case START:
        uiLock(true);
        break;
      case FINISH:
        this.taskResult = result;
        uiLock(false);
        break;
    }
  }

  @Override
  protected void onSetupActionBar(ActionBar bar) {
    switch (type) {
      case Config.TaskType.SINGLE_ARG:
        bar.setTitle(R.string.opt_localExtrema);
        break;
      case Config.TaskType.ZERO_ORDER:
        bar.setTitle(R.string.opt_localExtremaR2Zero);
        break;
    }
  }

  private void startTask() {
    task = new OptTask(getApplicationContext());
    task.registerEventListener(this);
    task.execute(new Config(type));
  }

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    btnStart.setEnabled(!state);

    if (state) {
      MyLog.d(LOG_TAG, "Clearing UI...");
      if (adapter != null)
        adapter.refreshData(null);

      MyLog.d(LOG_TAG, "UI cleared");
      pbMain.setVisibility(View.VISIBLE);
      MyLog.d(LOG_TAG, "UI locked");
    } else {
      pbMain.setVisibility(View.INVISIBLE);
      MyLog.d(LOG_TAG, "UI unlocked");
      uiSet();
    }
  }

  private void uiSet() {
    MyLog.d(LOG_TAG, "Setting up UI");
    ArrayList<String> data = new ArrayList<>();

    switch (type) {
      case Config.TaskType.SINGLE_ARG:
        for (Result res : taskResult)
          data.add(ExtremaFinder.getTextResult(this, res));
        break;
      case Config.TaskType.ZERO_ORDER:
        Result result = taskResult.get(0);
        plotView.setImageBitmap(result.getBitmap());
        break;
    }

    if (adapter != null)
      adapter.refreshData(data);
    MyLog.d(LOG_TAG, "UI setup completed");
  }
}
