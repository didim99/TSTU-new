package ru.didim99.tstu.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
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
  implements CallbackTask.EventListener<ArrayList<Result>>, OptTask.StateChangeListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_OptAct";

  //view-elements
  private ImageView plotView;
  private TextListAdapter adapter;
  private Spinner spMethod, spFunction;
  private TextView tvTaskState;
  private Button btnStart;
  private TextView tvOut;
  private View pbMain;
  private CheckBox cbLimits;
  private Spinner spLimitType;
  private Spinner spLimitMethod;
  private View rowLimits, rowLimits2;
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
    spMethod = findViewById(R.id.spMethod);
    spFunction = findViewById(R.id.spFunction);
    plotView = findViewById(R.id.plotView);
    tvTaskState = findViewById(R.id.tvTaskState);
    btnStart = findViewById(R.id.btnStart);
    pbMain = findViewById(R.id.pbMain);
    tvOut = findViewById(R.id.tvOut);
    cbLimits = findViewById(R.id.cbUseLimits);
    rowLimits = findViewById(R.id.rowLimits);
    rowLimits2 = findViewById(R.id.rowLimits2);
    spLimitType = findViewById(R.id.spLimitType);
    spLimitMethod = findViewById(R.id.spLimitMethod);
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
      case Config.TaskType.MULTI_ARG:
        spMethod.setAdapter(new ArrayAdapter<>(
          this, android.R.layout.simple_list_item_1,
          getResources().getStringArray(R.array.opt_methods)));
        spFunction.setAdapter(new ArrayAdapter<>(
          this, android.R.layout.simple_list_item_1,
          getResources().getStringArray(R.array.opt_functions)));
        spLimitType.setAdapter(new ArrayAdapter<>(
          this, android.R.layout.simple_list_item_1,
          getResources().getStringArray(R.array.opt_limitTypes)));
        spLimitMethod.setAdapter(new ArrayAdapter<>(
          this, android.R.layout.simple_list_item_1,
          getResources().getStringArray(R.array.opt_limitMethods)));
        cbLimits.setOnCheckedChangeListener((v, c) -> onUseLimitsChanged(c));
        spLimitType.setOnItemSelectedListener(
          new SpinnerAdapter(this::onLimitTypeChanged));
        onUseLimitsChanged(cbLimits.isChecked());
        break;
    }

    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "Trying to connect with background task...");
    task = (OptTask) getLastCustomNonConfigurationInstance();
    if (task == null) {
      MyLog.d(LOG_TAG, "No existing background task found");
    } else {
      task.registerEventListener(this);
      task.setStateChangeListener(this);
      MyLog.d(LOG_TAG, "Connecting to background task completed "
        + "(" + task.hashCode() + ")");
    }
  }

  @Override
  public OptTask onRetainCustomNonConfigurationInstance() {
    if (task != null) {
      task.setStateChangeListener(null);
      task.unregisterEventListener();
      return task;
    } else
      return null;
  }

  @Override
  protected void onSetupActionBar(ActionBar bar) {
    switch (type) {
      case Config.TaskType.SINGLE_ARG:
        bar.setTitle(R.string.opt_localExtrema);
        break;
      case Config.TaskType.MULTI_ARG:
        bar.setTitle(R.string.opt_localExtremaR2Zero);
        break;
    }
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
  public void onTaskStateChanged(String taskState) {
    if (type == Config.TaskType.MULTI_ARG)
      tvTaskState.setText(taskState);
  }

  private void onLimitTypeChanged(int limitType) {
    MyLog.v(LOG_TAG, "Limit type changed: " + limitType);
    boolean combined = limitType == Config.LimitType.EQUALITY;
    if (combined) spLimitMethod.setSelection(Config.FineMethod.COMBINED);
    spLimitMethod.setEnabled(!combined);
  }

  private void onUseLimitsChanged(boolean state) {
    MyLog.v(LOG_TAG, "UseLimits state changed: " + state);
    rowLimits.setVisibility(state ? View.VISIBLE : View.GONE);
    rowLimits2.setVisibility(state ? View.VISIBLE : View.GONE);
  }

  private void startTask() {
    Config config = new Config(type);

    if (type == Config.TaskType.MULTI_ARG) {
      config.setMethod(spMethod.getSelectedItemPosition());
      config.setFunction(spFunction.getSelectedItemPosition());
      config.setLimitType(spLimitType.getSelectedItemPosition());
      config.setLimitMethod(spLimitMethod.getSelectedItemPosition());
      config.setUseLimits(cbLimits.isChecked());
    }

    task = new OptTask(getApplicationContext());
    task.setStateChangeListener(this);
    task.registerEventListener(this);
    task.execute(config);
  }

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    btnStart.setEnabled(!state);
    if (spMethod != null)
      spMethod.setEnabled(!state);
    if (spFunction != null)
      spFunction.setEnabled(!state);
    if (spLimitType != null)
      spLimitType.setEnabled(!state);
    if (spLimitMethod != null)
      spLimitMethod.setEnabled(!state);
    if (cbLimits != null)
      cbLimits.setEnabled(!state);

    if (state) {
      MyLog.d(LOG_TAG, "Clearing UI...");
      if (adapter != null)
        adapter.refreshData(null);
      if (plotView != null)
        plotView.setImageBitmap(null);
      if (tvOut != null)
        tvOut.setText(null);

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
      case Config.TaskType.MULTI_ARG:
        tvTaskState.setText(null);
        Result result = taskResult.get(0);
        plotView.setImageBitmap(result.getBitmap());
        tvOut.setText(result.getDescription());
        onLimitTypeChanged(spLimitType.getSelectedItemPosition());
        onUseLimitsChanged(cbLimits.isChecked());
        break;
    }

    if (adapter != null)
      adapter.refreshData(data);
    MyLog.d(LOG_TAG, "UI setup completed");
  }
}
