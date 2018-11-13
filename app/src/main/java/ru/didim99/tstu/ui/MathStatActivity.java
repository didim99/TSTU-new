package ru.didim99.tstu.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.jjoe64.graphview.GraphView;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.math.MathStat;
import ru.didim99.tstu.utils.MyLog;

public class MathStatActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_StatAct";

  private EditText etX, etF;
  private GraphView graph;
  private TextView tvOut;
  private Button btnGo;
  private Toast toast;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    MyLog.d(LOG_TAG, "MathStatActivity starting...");
    setContentView(R.layout.act_mathstat);

    setupActionBar();
    MyLog.d(LOG_TAG, "View components init...");
    etX = findViewById(R.id.etInputX);
    etF = findViewById(R.id.etInputF);
    graph = findViewById(R.id.graph);
    tvOut = findViewById(R.id.tvOut);
    btnGo = findViewById(R.id.btnGo);
    btnGo.setOnClickListener(v -> compute());
    toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "MathStatActivity started");
  }

  private void compute() {
    MyLog.d(LOG_TAG, "Reading values...");
    String xStr = etX.getText().toString();
    String fStr = etF.getText().toString();
    MathStat stat = new MathStat();

    try {
      uiLock(true);
      stat.compute(xStr, fStr);
      uiLock(false);
      stat.drawGraph(graph, MathStat.GraphType.POLYGON_F);
      //stat.drawGraph(graph, MathStat.GraphType.POLYGON_W);
      //stat.drawGraph(graph, MathStat.GraphType.HYSTOGRAM);
      tvOut.setText(stat.getTextResult(this));
    } catch (MathStat.ProcessException e) {
      uiLock(false);
      switch (stat.getErrorCode()) {
        case MathStat.ErrorCode.EMPTY_X:
          toast.setText(R.string.errMathStat_emptyX);
          toast.show();
          break;
        case MathStat.ErrorCode.EMPTY_F:
          toast.setText(R.string.errMathStat_emptyF);
          toast.show();
          break;
        case MathStat.ErrorCode.EMPTY_GROUP:
          toast.setText(getString(R.string.errMathStat_emptyGroup,
            stat.getBrokenGroup()));
          toast.show();
          break;
        case MathStat.ErrorCode.GROUPS_NOT_EQUALS:
          toast.setText(R.string.errMathStat_groupsNotEquals);
          toast.show();
          break;
        case MathStat.ErrorCode.ELEMENTS_NOT_EQUALS:
          toast.setText(getString(R.string.errMathStat_elementsNotEquals,
            stat.getBrokenGroup()));
          toast.show();
          break;
        case MathStat.ErrorCode.INCORRECT_FORMAT_X:
          toast.setText(getString(R.string.errMathStat_incorrectFmtX,
            stat.getBrokenGroup(), stat.getBrokenValue()));
          toast.show();
          break;
        case MathStat.ErrorCode.INCORRECT_FORMAT_F:
          toast.setText(getString(R.string.errMathStat_incorrectFmtF,
            stat.getBrokenGroup(), stat.getBrokenValue()));
          toast.show();
          break;
        case MathStat.ErrorCode.NEGATIVE_F:
          toast.setText(getString(R.string.errMathStat_negativeF,
            stat.getBrokenGroup(), stat.getBrokenValue()));
          toast.show();
          break;
      }
    }
  }

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    btnGo.setEnabled(!state);
    etX.setEnabled(!state);
    etF.setEnabled(!state);

    if (state) {
      MyLog.d(LOG_TAG, "Clearing UI...");
      tvOut.setText(null);
      graph.removeAllSeries();
      MyLog.d(LOG_TAG, "UI cleared");
      graph.setVisibility(View.INVISIBLE);
      MyLog.d(LOG_TAG, "UI locked");
    } else {
      graph.setVisibility(View.VISIBLE);
      MyLog.d(LOG_TAG, "UI unlocked");
    }
  }

  @Override
  protected void onSetupActionBar(ActionBar bar) {}
}
