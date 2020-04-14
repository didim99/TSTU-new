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
import com.jjoe64.graphview.series.Series;

import ru.didim99.tstu.R;
import ru.didim99.tstu.TSTU;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.modeling.Config;
import ru.didim99.tstu.core.modeling.Functions;
import ru.didim99.tstu.core.modeling.ModelingTask;
import ru.didim99.tstu.core.modeling.Result;
import ru.didim99.tstu.core.optimization.math.PointRN;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 10.02.20.
 */
public class ModelingActivity extends BaseActivity
  implements CallbackTask.EventListener<Result> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ModelingAct";

  private static final int[] COLORS = {
    R.color.graph0, R.color.graph1,
    R.color.graph2, R.color.graph3,
    R.color.graph4
  };

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
    TextView tvVarType = findViewById(R.id.tvVarType);

    switch (type) {
      case Config.TaskType.LUMPED_STATIC_CURVE:
      case Config.TaskType.LUMPED_DYNAMIC_CURVE:
        spVariable.setAdapter(new ArrayAdapter<>(
          this, android.R.layout.simple_list_item_1,
          Functions.getVarList()));
        break;
      case Config.TaskType.DIST_STATIC_CURVES:
        tvVarType.setText(R.string.modeling_concentration);
        spVariable.setAdapter(new ArrayAdapter<>(
          this, android.R.layout.simple_list_item_1, getResources()
          .getStringArray(R.array.modeling_reactComponents)));
        break;
    }

    LegendRenderer legend = graphView.getLegendRenderer();
    legend.setAlign(LegendRenderer.LegendAlign.TOP);

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
      case Config.TaskType.LUMPED_STATIC_CURVE:
        bar.setTitle(R.string.modeling_lumpedStaticCurve);
        break;
      case Config.TaskType.LUMPED_DYNAMIC_CURVE:
        bar.setTitle(R.string.modeling_lumpedDynamicCurve);
        break;
      case Config.TaskType.DIST_STATIC_CURVES:
        bar.setTitle(R.string.modeling_distStaticCurve);
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
      case Config.TaskType.LUMPED_STATIC_CURVE:
      case Config.TaskType.LUMPED_DYNAMIC_CURVE:
      case Config.TaskType.DIST_STATIC_CURVES:
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
      case Config.TaskType.LUMPED_STATIC_CURVE:
      case Config.TaskType.LUMPED_DYNAMIC_CURVE:
      case Config.TaskType.DIST_STATIC_CURVES:
        tvOut.setText(taskResult.getDescription());

        int clr = 0;
        for (Series<PointRN> s : taskResult.getSeriesFamily()) {
          BaseSeries<PointRN> series = (BaseSeries<PointRN>) s;
          series.setColor(getResources().getColor(
            COLORS[clr++ % COLORS.length]));
          graphView.addSeries(series);
        }

        graphView.getLegendRenderer().setVisible(true);
        graphView.getViewport().setScalable(true);
        break;
    }

    MyLog.d(LOG_TAG, "UI setup completed");
  }
}
