package ru.didim99.tstu.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.math.MathStat;
import ru.didim99.tstu.utils.MyLog;

public class MathStatActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_StatAct";

  private EditText etX, etF;
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
    Button btnGo = findViewById(R.id.btnGo);
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
      stat.compute(xStr, fStr);



    } catch (MathStat.ProcessException e) {
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

  @Override
  protected void onSetupActionBar(ActionBar bar) {}
}
