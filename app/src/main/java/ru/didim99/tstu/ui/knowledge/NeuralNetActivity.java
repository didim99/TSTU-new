package ru.didim99.tstu.ui.knowledge;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.jjoe64.graphview.GraphView;
import java.util.List;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.knowledge.neuralnet.Net;
import ru.didim99.tstu.core.knowledge.neuralnet.NetLoader;
import ru.didim99.tstu.core.knowledge.neuralnet.ReferenceRule;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.ui.UIManager;
import ru.didim99.tstu.ui.view.RangeBar;
import ru.didim99.tstu.utils.MyLog;

public class NeuralNetActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_NNAct";

  private enum ControlType { LEARN, PREDICT }

  // View-elements
  private Button btnLoad;
  private Button btnLearnMode;
  private Button btnPredictMode;
  private View learnLayout;
  private View predictLayout;
  private Button btnInit, btnLearn;
  private RangeBar rbLearnSpeed;
  private TextView tvLearningBase;
  private TextView tvOutValues;
  private NetView netView;
  private GraphView graphView;
  private InputStateAdapter adapter;
  // Workflow
  private ControlType controlType;
  private Net net;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "NeuralNetActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_neural_net);

    MyLog.d(LOG_TAG, "View components init...");
    RecyclerView rvInputList = findViewById(R.id.rvInputState);
    btnLoad = findViewById(R.id.btnLoad);
    btnLearnMode = findViewById(R.id.btnLearnMode);
    btnPredictMode = findViewById(R.id.btnPredictMode);
    learnLayout = findViewById(R.id.learnLayout);
    predictLayout = findViewById(R.id.predictLayout);
    btnInit = findViewById(R.id.btnInit);
    btnLearn = findViewById(R.id.btnLearn);
    rbLearnSpeed = findViewById(R.id.rbLearnSpeed);
    tvLearningBase = findViewById(R.id.tvLearningBase);
    tvOutValues = findViewById(R.id.tvOutValues);
    netView = findViewById(R.id.netView);
    graphView = findViewById(R.id.graphView);

    adapter = new InputStateAdapter(this, this::onInputValueChanged);
    rvInputList.setLayoutManager(new LinearLayoutManager(this));
    rvInputList.setAdapter(adapter);

    rbLearnSpeed.setFactor(Net.LSPD_FACTOR);
    rbLearnSpeed.setBounds(Net.LSPD_MIN, Net.LSPD_MAX);

    btnLoad.setOnClickListener(v -> openFile());
    btnInit.setOnClickListener(v -> initNet());
    btnLearn.setOnClickListener(v -> startLearning());
    btnLearnMode.setOnClickListener(v -> setControlType(ControlType.LEARN));
    btnPredictMode.setOnClickListener(v -> setControlType(ControlType.PREDICT));
    setControlType(ControlType.LEARN);
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "Loading saved instance...");
    net = (Net) getLastCustomNonConfigurationInstance();
    if (net != null) {
      MyLog.d(LOG_TAG, "Instance loaded: " + net);
      onNetLoaded();
    } else {
      MyLog.d(LOG_TAG, "No existing instance found");
    }

    MyLog.d(LOG_TAG, "TranslatorActivity started");
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent i) {
    if (requestCode == REQUEST_GET_FILE) {
      if (resultCode == RESULT_OK) {
        Uri data = i.getData();
        if (data != null) {
          String path = data.getPath();
          if (path != null && path.endsWith(Net.FILE_MASK)) {
            MyLog.d(LOG_TAG, "Loading network model: " + path);
            NetLoader loader = new NetLoader(this);
            loader.registerEventListener(this::onNetLoadEvent);
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

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    netView.setDataSource(null);
    return net;
  }

  private void onNetLoadEvent(CallbackTask.Event event, Net net) {
    boolean lock = event == CallbackTask.Event.START;
    btnLoad.setText(lock ? R.string.loading : R.string.buttonLoad);
    btnLoad.setEnabled(!lock);
    if (net != null) {
      this.net = net;
      onNetLoaded();
    }
  }

  private void onNetLoaded() {
    MyLog.d(LOG_TAG, "Loaded net: " + net);
    rbLearnSpeed.setValue(net.getLearnSpeed());
    tvLearningBase.setText(describeLearningBase(
      net.getConfig().getLearningBase()));
    netView.setDataSource(net);
    adapter.setDataSource(net);
    updateViews();
  }

  private void onInputValueChanged(int index, double value) {
    net.setInputValue(index, value);
    updateViews();
  }

  private void setControlType(ControlType type) {
    MyLog.d(LOG_TAG, "Switching to control type: " + type);
    boolean learning = ControlType.LEARN.equals(type);
    UIManager uiManager = UIManager.getInstance();
    int colorActive = getResources().getColor(
      uiManager.resolveAttr(R.attr.colorAccent));
    int colorInactive = getResources().getColor(
      uiManager.resolveAttr(R.attr.colorActive));
    graphView.setVisibility(learning ? View.VISIBLE : View.GONE);
    learnLayout.setVisibility(learning ? View.VISIBLE : View.GONE);
    predictLayout.setVisibility(learning ? View.GONE : View.VISIBLE);
    btnLearnMode.setTextColor(learning ? colorActive : colorInactive);
    btnPredictMode.setTextColor(learning ? colorInactive : colorActive);
    controlType = type;
  }

  private void initNet() {
    net.randomizeWeights();
    updateViews();
  }

  private void startLearning() {
    net.setLearnSpeed(rbLearnSpeed.getValue());
  }

  private void updateViews() {
    net.calculateOutput();
    netView.invalidate();
    describeOutputs();
  }

  private String describeLearningBase(List<ReferenceRule> learningBase) {
    StringBuilder sb = new StringBuilder();
    for (ReferenceRule rule : learningBase)
      sb.append(rule).append("\n");
    return sb.toString();
  }

  private void describeOutputs() {
    StringBuilder sb = new StringBuilder();
    List<Double> outputs = net.getOutputValues();
    int lastOutput = outputs.size() - 1;

    for (int i = 0; i <= lastOutput; i++) {
      sb.append(String.format(Locale.US, "%.3f", outputs.get(i)));
      if (i < lastOutput) sb.append(", ");
    }

    tvOutValues.setText(sb);
  }
}
