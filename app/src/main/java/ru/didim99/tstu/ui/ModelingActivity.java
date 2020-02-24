package ru.didim99.tstu.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.BaseSeries;
import ru.didim99.tstu.R;
import ru.didim99.tstu.TSTU;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.modeling.Config;
import ru.didim99.tstu.core.modeling.Functions;
import ru.didim99.tstu.core.modeling.ModelingTask;
import ru.didim99.tstu.core.modeling.Result;
import ru.didim99.tstu.core.optimization.math.PointD;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 10.02.20.
 */
public class ModelingActivity extends BaseActivity
  implements CallbackTask.EventListener<Result> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ModelingAct";

  //view-elements
  private Spinner spVariable;
  private Button btnStart;
  private TextView tvOut;
  private GraphView graphView;
  private View pbMain;
  //main workflow
  private int type;
  private ModelingTask task;
  private Result taskResult;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "ModelingActivity starting...");
    type = getIntent().getIntExtra(TSTU.EXTRA_TYPE, Config.TaskType.UNDEFINED);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_modeling);

    MyLog.d(LOG_TAG, "View components init...");
    spVariable = findViewById(R.id.spVariable);
    btnStart = findViewById(R.id.btnStart);
    tvOut = findViewById(R.id.tvOut);
    graphView = findViewById(R.id.graphView);
    pbMain = findViewById(R.id.pbMain);
    btnStart.setOnClickListener(v -> startTask());

    switch (type) {
      case Config.TaskType.STATIC_CURVE:
        LegendRenderer legend = graphView.getLegendRenderer();
        legend.setAlign(LegendRenderer.LegendAlign.TOP);
        spVariable.setAdapter(new ArrayAdapter<>(
          this, android.R.layout.simple_list_item_1,
          Functions.getVarList()));
        break;
    }

    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "Trying to connect with background task...");
    task = (ModelingTask) getLastCustomNonConfigurationInstance();
    if (task == null) {
      MyLog.d(LOG_TAG, "No existing background task found");
    } else {
      task.registerEventListener(this);
      MyLog.d(LOG_TAG, "Connecting to background task completed "
        + "(" + task.hashCode() + ")");
    }
  }

  @Override
  public ModelingTask onRetainCustomNonConfigurationInstance() {
    if (task != null) {
      task.unregisterEventListener();
      return task;
    } else
      return null;
  }

  @Override
  protected void onSetupActionBar(ActionBar bar) {
    switch (type) {
      case Config.TaskType.STATIC_CURVE:
        bar.setTitle(R.string.modeling_staticCurve);
        break;
      case Config.TaskType.DYNAMIC_CURVE:
        bar.setTitle(R.string.modeling_dynamicCurve);
        break;
    }
  }

  @Override
  public void onTaskEvent(CallbackTask.Event event, Result result) {
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

  private void startTask() {
    Config config = new Config(type);

    switch (type) {
      case Config.TaskType.STATIC_CURVE:
        config.setVariable(spVariable.getSelectedItemPosition());
        break;
    }

    task = new ModelingTask(getApplicationContext());
    task.registerEventListener(this);
    task.execute(config);
  }

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    btnStart.setEnabled(!state);

    if (state) {
      MyLog.d(LOG_TAG, "Clearing UI...");
      if (tvOut != null)
        tvOut.setText(null);
      if (graphView != null) {
        graphView.removeAllSeries();
        graphView.setVisibility(View.INVISIBLE);
      }

      MyLog.d(LOG_TAG, "UI cleared");
      pbMain.setVisibility(View.VISIBLE);
      MyLog.d(LOG_TAG, "UI locked");
    } else {
      if (graphView != null)
        graphView.setVisibility(View.VISIBLE);
      pbMain.setVisibility(View.INVISIBLE);
      MyLog.d(LOG_TAG, "UI unlocked");
      uiSet();
    }
  }

  private void uiSet() {
    MyLog.d(LOG_TAG, "Setting up UI");

    switch (type) {
      case Config.TaskType.STATIC_CURVE:
        tvOut.setText(taskResult.getDescription());
        BaseSeries<PointD> series = (BaseSeries<PointD>) taskResult.getSeries();
        series.setColor(getResources().getColor(R.color.graph0));
        graphView.addSeries(series);
        graphView.getLegendRenderer().setVisible(true);
        graphView.getViewport().setScalable(true);
        break;
    }

    MyLog.d(LOG_TAG, "UI setup completed");
  }
}
