package ru.didim99.tstu.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import java.io.IOException;
import java.util.ArrayList;
import ru.didim99.tstu.R;
import ru.didim99.tstu.TSTU;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.numeric.Config;
import ru.didim99.tstu.core.numeric.DiffSolver;
import ru.didim99.tstu.core.numeric.Integrator;
import ru.didim99.tstu.core.numeric.Interpolator;
import ru.didim99.tstu.core.numeric.LinearSystemSolver;
import ru.didim99.tstu.core.numeric.Matrix;
import ru.didim99.tstu.core.numeric.Result;
import ru.didim99.tstu.core.numeric.NumericTask;
import ru.didim99.tstu.core.numeric.TranscendentSolver;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

import static ru.didim99.tstu.core.numeric.Config.TaskType;

public class NumericActivity extends BaseActivity
  implements NumericTask.EventListener<ArrayList<Result>> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_NumericAct";
  private static final String DIRNAME = "/numeric";

  //view-elements
  private OutListAdapter adapter;
  private Button btnStart, btnSave;
  private CheckBox cbTConst;
  private View pbMain;
  private TextView tvOut;
  private GraphView graphView;
  //main workflow
  private int type;
  private boolean tConst;
  private NumericTask task;
  private ArrayList<Result> taskResult;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "NumericActivity starting...");
    type = getIntent().getIntExtra(TSTU.EXTRA_TYPE, TaskType.UNDEFINED);
    super.onCreate(savedInstanceState);
    switch (type) {
      case TaskType.INTERPOLATION:
      case TaskType.INTEGRATION:
      case TaskType.DIFF_EQUATION:
        setContentView(R.layout.act_numeric_graph);
        break;
      default:
        setContentView(R.layout.act_numeric);
        break;
    }

    MyLog.d(LOG_TAG, "View components init...");
    TextView title = findViewById(R.id.tvTitle);
    cbTConst = findViewById(R.id.cbTConst);
    pbMain = findViewById(R.id.pbMain);
    btnStart = findViewById(R.id.btnStart);
    btnSave = findViewById(R.id.btnSave);
    tvOut = findViewById(R.id.tvOut);
    graphView = findViewById(R.id.graph);
    btnStart.setOnClickListener(v -> startTask());
    btnSave.setOnClickListener(v -> saveToFile());
    cbTConst.setOnCheckedChangeListener(
      (buttonView, isChecked) -> tConst = isChecked);
    if (graphView != null) {
      LegendRenderer legend = graphView.getLegendRenderer();
      legend.setAlign(LegendRenderer.LegendAlign.TOP);
    }

    RecyclerView rvOut = findViewById(R.id.rvOut);
    if (rvOut != null) {
      adapter = new OutListAdapter(this);
      rvOut.setAdapter(adapter);
    }

    switch (type) {
      case TaskType.TRANSCENDENT:
        title.setText(R.string.numeric_inputEquation);
        rvOut.setLayoutManager(new LinearLayoutManager(
          this, RecyclerView.HORIZONTAL, false));
        break;
      case TaskType.LINEAR_SYSTEM:
        title.setText(R.string.numeric_linearSolver_typeLU);
        cbTConst.setText(R.string.numeric_setRandom);
        rvOut.setLayoutManager(new GridLayoutManager(this, 2));
        break;
      case TaskType.INTERPOLATION:
        cbTConst.setVisibility(View.GONE);
        title.setText(R.string.numeric_interpolation_newton);
        break;
      case TaskType.INTEGRATION:
        cbTConst.setVisibility(View.GONE);
        title.setText(R.string.numeric_integration_trapezium);
        break;
      case TaskType.DIFF_EQUATION:
        cbTConst.setText(R.string.numeric_calcDelta);
        title.setText(R.string.numeric_diffSolver_euler);
        break;
    }

    MyLog.d(LOG_TAG, "View components init completed");

    tConst = cbTConst.isChecked();

    MyLog.d(LOG_TAG, "Trying to connect with background task...");
    task = (NumericTask) getLastCustomNonConfigurationInstance();
    if (task == null) {
      MyLog.d(LOG_TAG, "No existing background task found");
    } else {
      task.registerEventListener(this);
      MyLog.d(LOG_TAG, "Connecting to background task completed "
        + "(" + task.hashCode() + ")");
    }
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
      case TaskType.TRANSCENDENT:
        bar.setTitle(R.string.numeric_transcendentSolver);
        break;
      case TaskType.LINEAR_SYSTEM:
        bar.setTitle(R.string.numeric_linearSystemSolver);
        break;
      case TaskType.INTERPOLATION:
        bar.setTitle(R.string.numeric_interpolator);
        break;
      case TaskType.INTEGRATION:
        bar.setTitle(R.string.numeric_integrator);
        break;
      case TaskType.DIFF_EQUATION:
        bar.setTitle(R.string.numeric_diffSolver);
        break;
    }
  }

  private void startTask() {
    String rootDir = getExternalCacheDir().getAbsolutePath().concat(DIRNAME);
    Config.Builder builder = new Config.Builder();
    builder.taskType(type).tConst(tConst);

    switch (type) {
      case TaskType.LINEAR_SYSTEM:
        builder.fileName(rootDir + "/linearSystemInput.txt");
        break;
      case TaskType.INTERPOLATION:
        builder.fileName(rootDir + "/InterpolationInput.txt");
        break;
    }

    task = new NumericTask(getApplicationContext());
    task.registerEventListener(this);
    task.execute(builder.build());
  }

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    btnStart.setEnabled(!state);
    cbTConst.setEnabled(!state);
    btnSave.setEnabled(!state);

    if (state) {
      MyLog.d(LOG_TAG, "Clearing UI...");
      if (adapter != null)
        adapter.refreshData(null);
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
    ArrayList<String> data = new ArrayList<>();
    Result result;

    switch (type) {
      case TaskType.TRANSCENDENT:
        for (Result res : taskResult)
          data.add(TranscendentSolver.getTextResult(this, res));
        break;
      case TaskType.LINEAR_SYSTEM:
        for (Matrix matrix : taskResult.get(0).getMatrixSeries())
          data.add(matrix.toString());
        break;
      case TaskType.INTERPOLATION:
        result = taskResult.get(0);
        tvOut.setText(Interpolator.getTextResult(result, false));
        Interpolator.drawGraph(this, result, graphView);
        break;
      case TaskType.INTEGRATION:
        result = taskResult.get(0);
        tvOut.setText(Integrator.getTextResult(result));
        Integrator.drawGraph(this, result, graphView);
        break;
      case TaskType.DIFF_EQUATION:
        result = taskResult.get(0);
        tvOut.setText(DiffSolver.getTextResult(result));
        DiffSolver.drawGraph(this, result, graphView);
        break;
    }

    if (adapter != null)
      adapter.refreshData(data);
    MyLog.d(LOG_TAG, "UI setup completed");
  }

  private void saveToFile() {
    if (taskResult == null) {
      Toast.makeText(this, R.string.errNumeric_nothingToSave, Toast.LENGTH_LONG).show();
      return;
    }

    MyLog.d(LOG_TAG, "Saving result to file");
    ArrayList<String> data = new ArrayList<>();
    String fileName = "";

    try {
      switch (type) {
        case TaskType.TRANSCENDENT:
          for (Result result : taskResult)
            data.add(TranscendentSolver.getTextResult(this, result));
          fileName = "/transcendent.txt";
          break;
        case TaskType.LINEAR_SYSTEM:
          Result result = taskResult.get(0);
          data.add("Input system:");
          data.add(LinearSystemSolver.buildSystem(result));
          data.add("Input system matrix:");
          data.add(LinearSystemSolver.buildSystemMatrix(result));
          for (Matrix matrix : result.getMatrixSeries()) {
            data.add(matrix.toString());
            data.add("");
          }
          fileName = "/linearSystem.txt";
          break;
        case TaskType.INTERPOLATION:
          data.add(Interpolator.getTextResult(taskResult.get(0), true));
          fileName = "/interpolator.txt";
          break;
        case TaskType.INTEGRATION:
          data.add(Integrator.getTextResult(taskResult.get(0)));
          fileName = "/integrator.txt";
          break;
        case TaskType.DIFF_EQUATION:
          data.add(DiffSolver.getTextResult(taskResult.get(0)));
          fileName = "/diffSolver.txt";
          break;
      }

      String path = getExternalCacheDir()
        .getAbsolutePath().concat(DIRNAME).concat(fileName);
      Utils.writeFile(path, data);
      MyLog.d(LOG_TAG, "Saved to: " + path);
      Toast.makeText(this, getString(R.string.savedToFile, path),
        Toast.LENGTH_LONG).show();
    } catch (IOException e) {
      MyLog.w(LOG_TAG, "Unable to write output file: " + e.toString());
      Toast.makeText(this, getString(R.string.cantWriteFile, e.toString()),
        Toast.LENGTH_LONG).show();
    }
  }

  static class OutListAdapter extends RecyclerView.Adapter<OutListAdapter.ViewHolder> {
    private ArrayList<String> data;
    private LayoutInflater inflater;

    OutListAdapter(Context context) {
      this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public OutListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      return new ViewHolder(inflater.inflate(R.layout.item_out_data, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OutListAdapter.ViewHolder holder, int position) {
      holder.tvOut.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
      return data == null ? 0 : data.size();
    }

    void refreshData(ArrayList<String> data) {
      this.data = data;
      notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
      final TextView tvOut;

      ViewHolder(View itemView) {
        super(itemView);
        this.tvOut = itemView.findViewById(R.id.tvOut);
      }
    }
  }
}
