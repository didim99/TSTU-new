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
import java.io.IOException;
import java.util.ArrayList;
import ru.didim99.tstu.R;
import ru.didim99.tstu.TSTU;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.numeric.Config;
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

  //view-elements
  private OutListAdapter adapter;
  private Button btnStart, btnSave;
  private CheckBox cbTConst;
  private View pbMain;
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
    setContentView(R.layout.act_numeric);
    setupActionBar();

    MyLog.d(LOG_TAG, "View components init...");
    TextView title = findViewById(R.id.tvTitle);
    cbTConst = findViewById(R.id.cbTConst);
    pbMain = findViewById(R.id.pbMain);
    btnStart = findViewById(R.id.btnStart);
    btnSave = findViewById(R.id.btnSave);
    btnStart.setOnClickListener(v -> startTask());
    btnSave.setOnClickListener(v -> saveToFile());
    cbTConst.setOnCheckedChangeListener(
      (buttonView, isChecked) -> tConst = isChecked);

    adapter = new OutListAdapter(this);
    RecyclerView rvOut = findViewById(R.id.rvOut);
    rvOut.setAdapter(adapter);

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
      }

      String path = getExternalCacheDir().getAbsolutePath() + fileName;
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
    }
  }

  private void startTask() {
    Config.Builder builder = new Config.Builder();
    builder.taskType(type).tConst(tConst);

    switch (type) {
      case TaskType.LINEAR_SYSTEM:
        builder.fileName(getExternalCacheDir().getAbsolutePath()
          + "/linearSystemInput.txt");
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
    cbTConst.setEnabled(!state);
    btnStart.setEnabled(!state);
    btnSave.setEnabled(!state);

    if (state) {
      MyLog.d(LOG_TAG, "Clearing UI...");
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
      case TaskType.TRANSCENDENT:
        for (Result result : taskResult)
          data.add(TranscendentSolver.getTextResult(this, result));
        break;
      case TaskType.LINEAR_SYSTEM:
        for (Matrix matrix : taskResult.get(0).getMatrixSeries())
          data.add(matrix.toString());
        break;
    }

    adapter.refreshData(data);
    MyLog.d(LOG_TAG, "UI setup completed");
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
