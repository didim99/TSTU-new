package ru.didim99.tstu.ui.knowledge;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import ru.didim99.tstu.R;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.ui.UIManager;
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
  // Workflow
  private ControlType controlType;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "NeuralNetActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_neural_net);

    MyLog.d(LOG_TAG, "View components init...");
    btnLoad = findViewById(R.id.btnLoad);
    btnLearnMode = findViewById(R.id.btnLearnMode);
    btnPredictMode = findViewById(R.id.btnPredictMode);
    learnLayout = findViewById(R.id.learnLayout);
    predictLayout = findViewById(R.id.predictLayout);
    btnLoad = findViewById(R.id.btnLoad);

    btnLearnMode.setOnClickListener(v -> setControlType(ControlType.LEARN));
    btnPredictMode.setOnClickListener(v -> setControlType(ControlType.PREDICT));
    setControlType(ControlType.LEARN);
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "TranslatorActivity started");
  }

  private void setControlType(ControlType type) {
    MyLog.d(LOG_TAG, "Switching to control type: " + type);
    boolean learning = ControlType.LEARN.equals(type);
    UIManager uiManager = UIManager.getInstance();
    int colorActive = getResources().getColor(
      uiManager.resolveAttr(R.attr.colorAccent));
    int colorInactive = getResources().getColor(
      uiManager.resolveAttr(R.attr.colorActive));
    learnLayout.setVisibility(learning ? View.VISIBLE : View.GONE);
    predictLayout.setVisibility(learning ? View.GONE : View.VISIBLE);
    btnLearnMode.setTextColor(learning ? colorActive : colorInactive);
    btnPredictMode.setTextColor(learning ? colorInactive : colorActive);
    controlType = type;
  }
}
