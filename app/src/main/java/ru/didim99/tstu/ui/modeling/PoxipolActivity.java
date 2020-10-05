package ru.didim99.tstu.ui.modeling;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.BaseSeries;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.modeling.poxipol.PoxipolTask;
import ru.didim99.tstu.core.modeling.poxipol.Result;
import ru.didim99.tstu.core.optimization.math.PointRN;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 02.10.20.
 */

public class PoxipolActivity extends BaseActivity
  implements CallbackTask.EventListener<Result> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_PXAct";

  // view-elements
  private Button btnStart;
  private TextView tvOut;
  private TextView tvExecTime;
  private GraphView graphView;
  private View pbMain;
  // main workflow
  private PoxipolTask task;
  private Result taskResult;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "PoxipolActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_poxipol);

    MyLog.d(LOG_TAG, "View components init...");
    btnStart = findViewById(R.id.btnStart);
    tvOut = findViewById(R.id.tvOut);
    tvExecTime = findViewById(R.id.tvExecTime);
    graphView = findViewById(R.id.graphView);
    pbMain = findViewById(R.id.pbMain);
    btnStart.setOnClickListener(v -> startTask());

    LegendRenderer legend = graphView.getLegendRenderer();
    legend.setAlign(LegendRenderer.LegendAlign.TOP);

    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "Trying to connect with background task...");
    task = (PoxipolTask) getLastCustomNonConfigurationInstance();
    if (task == null) {
      MyLog.d(LOG_TAG, "No existing background task found");
    } else {
      task.registerEventListener(this);
      MyLog.d(LOG_TAG, "Connecting to background task completed "
        + "(" + task.hashCode() + ")");
    }

    MyLog.d(LOG_TAG, "PoxipolActivity created");
  }

  @Override
  public PoxipolTask onRetainCustomNonConfigurationInstance() {
    if (task != null) {
      task.unregisterEventListener();
      return task;
    } else
      return null;
  }

  @Override
  protected void onSetupActionBar(ActionBar bar) {
    bar.setSubtitle(R.string.modeling_poxipol);
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
    task = new PoxipolTask(getApplicationContext());
    task.registerEventListener(this);
    task.execute(PoxipolTask.Action.INTEGRATE);
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
    if (taskResult == null) return;

    tvExecTime.setText(getString(R.string.executionTime,
      taskResult.getTime() / 1000f));
    tvOut.setText(taskResult.getDescription());

    BaseSeries<PointRN> series = (BaseSeries<PointRN>) taskResult.getSeries();
    series.setColor(getResources().getColor(R.color.graph0));
    graphView.addSeries(series);

    graphView.getLegendRenderer().setVisible(true);
    graphView.getViewport().setScalable(true);
    MyLog.d(LOG_TAG, "UI setup completed");
  }
}
