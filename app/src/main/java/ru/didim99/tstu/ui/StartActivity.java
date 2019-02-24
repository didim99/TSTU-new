package ru.didim99.tstu.ui;

import android.content.Intent;
import android.support.annotation.ArrayRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import ru.didim99.tstu.R;
import ru.didim99.tstu.TSTU;
import ru.didim99.tstu.ui.graphich.HorizonActivity;
import ru.didim99.tstu.ui.graphich.LinesActivity;
import ru.didim99.tstu.ui.math.CAActivity;
import ru.didim99.tstu.ui.math.MathStatActivity;
import ru.didim99.tstu.ui.math.RV2Activity;
import ru.didim99.tstu.ui.mp.lab1.L1ActMain;
import ru.didim99.tstu.ui.oop.AbiturientActivity;
import ru.didim99.tstu.utils.MyLog;

public class StartActivity extends AppCompatActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_StartAct";
  private static final Class[] MATH_TARGET =
    { MathStatActivity.class, RV2Activity.class, CAActivity.class };
  private static final Class[] MP_TARGET =
    { L1ActMain.class };
  private static final Class[] OOP_TARGET =
    { AbiturientActivity.class };
  private static final Class[] GRAPH_TARGET =
    { LinesActivity.class, HorizonActivity.class };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_start);

    findViewById(R.id.startTranslator).setOnClickListener(v ->
      startActivity(new Intent(this, TranslatorActivity.class)));
    findViewById(R.id.startNumeric).setOnClickListener(v -> numericTypeDialog());
    findViewById(R.id.startMathStat).setOnClickListener(v -> activityTypeDialog(
      R.string.numeric_selectType, R.array.math_taskTypes, MATH_TARGET));
    findViewById(R.id.startMP).setOnClickListener(v -> activityTypeDialog(
      R.string.mp_selectType, R.array.mp_taskTypes, MP_TARGET));
    findViewById(R.id.startOOP).setOnClickListener(v -> activityTypeDialog(
      R.string.oop_selectType, R.array.oop_taskTypes, OOP_TARGET));
    findViewById(R.id.startGraphics).setOnClickListener(v -> activityTypeDialog(
      R.string.graphics_selectType, R.array.graphics_taskTypes, GRAPH_TARGET));
  }

  private void activityTypeDialog(@StringRes int titleId, @ArrayRes int listId,
                                  Class[] targetList) {
    MyLog.d(LOG_TAG, "Type dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(titleId).setItems(listId, (dialog, pos) ->
      startActivity(new Intent(this, targetList[pos])));
    MyLog.d(LOG_TAG, "Type dialog created");
    adb.create().show();
  }

  private void numericTypeDialog() {
    MyLog.d(LOG_TAG, "Type dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.numeric_selectType);
    adb.setItems(R.array.numeric_taskTypes, (dialog, pos) -> {
      Intent intent = new Intent(this, NumericActivity.class);
      intent.putExtra(TSTU.EXTRA_TYPE, pos + 1);
      startActivity(intent);
    });
    MyLog.d(LOG_TAG, "Type dialog created");
    adb.create().show();
  }
}
