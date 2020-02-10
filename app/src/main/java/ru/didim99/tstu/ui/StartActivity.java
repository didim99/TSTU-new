package ru.didim99.tstu.ui;

import android.content.Intent;
import android.support.annotation.ArrayRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import ru.didim99.tstu.R;
import ru.didim99.tstu.TSTU;
import ru.didim99.tstu.ui.graphics.FractalActivity;
import ru.didim99.tstu.ui.graphics.HorizonActivity;
import ru.didim99.tstu.ui.graphics.LinesActivity;
import ru.didim99.tstu.ui.graphics.ShadingActivity;
import ru.didim99.tstu.ui.graphics.TexturesActivity;
import ru.didim99.tstu.ui.graphics.TransformActivity;
import ru.didim99.tstu.ui.math.CAActivity;
import ru.didim99.tstu.ui.math.MathStatActivity;
import ru.didim99.tstu.ui.math.RV2Activity;
import ru.didim99.tstu.ui.mp.lab1.L1ActMain;
import ru.didim99.tstu.ui.mp.lab2.L2ActMain;
import ru.didim99.tstu.ui.mp.lab3.L3ActMain;
import ru.didim99.tstu.ui.oop.AbiturientActivity;
import ru.didim99.tstu.ui.oop.ExceptionsActivity;
import ru.didim99.tstu.ui.oop.MatrixActivity;
import ru.didim99.tstu.ui.oop.ShopActivity;
import ru.didim99.tstu.ui.os.expanse.ExpanseActivity;
import ru.didim99.tstu.ui.os.procinfo.ProcessActivity;
import ru.didim99.tstu.ui.os.scheduler.SchedulerActivity;
import ru.didim99.tstu.utils.MyLog;

public class StartActivity extends AppCompatActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_StartAct";
  private static final Class[] MATH_TARGET =
    { MathStatActivity.class, RV2Activity.class, CAActivity.class };
  private static final Class[] MP_TARGET =
    { L1ActMain.class, L2ActMain.class, L3ActMain.class };
  private static final Class[] OOP_TARGET =
    { AbiturientActivity.class, MatrixActivity.class,
      ShopActivity.class, ExceptionsActivity.class };
  private static final Class[] GRAPH_TARGET =
    { LinesActivity.class, HorizonActivity.class,
      TransformActivity.class, TexturesActivity.class,
      ShadingActivity.class, FractalActivity.class };
  private static final Class[] OS_TARGET =
    { ProcessActivity.class, ExpanseActivity.class,
      SchedulerActivity.class };
  private static final Class[] IS_TARGET =
    { TransmitActivity.class };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "StartActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_start);

    findViewById(R.id.startTranslator).setOnClickListener(v ->
      startActivity(new Intent(this, TranslatorActivity.class)));
    findViewById(R.id.startNumeric).setOnClickListener(v -> taskTypeDialog(
      R.string.numeric_selectType, R.array.numeric_taskTypes, NumericActivity.class));
    findViewById(R.id.startMathStat).setOnClickListener(v -> activityTypeDialog(
      R.string.numeric_selectType, R.array.math_taskTypes, MATH_TARGET));
    findViewById(R.id.startMP).setOnClickListener(v -> activityTypeDialog(
      R.string.mp_selectType, R.array.mp_taskTypes, MP_TARGET));
    findViewById(R.id.startOOP).setOnClickListener(v -> activityTypeDialog(
      R.string.oop_selectType, R.array.oop_taskTypes, OOP_TARGET));
    findViewById(R.id.startGraphics).setOnClickListener(v -> activityTypeDialog(
      R.string.graphics_selectType, R.array.graphics_taskTypes, GRAPH_TARGET));
    findViewById(R.id.startOS).setOnClickListener(v -> activityTypeDialog(
      R.string.sectionOS, R.array.os_taskTypes, OS_TARGET));
    findViewById(R.id.startIS).setOnClickListener(v -> activityTypeDialog(
      R.string.sectionIS, R.array.is_taskTypes, IS_TARGET));
    findViewById(R.id.startOpt).setOnClickListener(v -> taskTypeDialog(
      R.string.sectionOpt, R.array.opt_taskTypes, OptimizationActivity.class));
    findViewById(R.id.startModeling).setOnClickListener(v -> taskTypeDialog(
      R.string.sectionModeling, R.array.modeling_taskTypes, ModelingActivity.class));

    MyLog.d(LOG_TAG, "StartActivity created");
  }

  private void activityTypeDialog(@StringRes int titleId, @ArrayRes int listId,
                                  Class[] targetList) {
    MyLog.d(LOG_TAG, "Activity type dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(titleId).setItems(listId, (dialog, pos) ->
      startActivity(new Intent(this, targetList[pos])));
    MyLog.d(LOG_TAG, "Activity type dialog created");
    adb.create().show();
  }

  private void taskTypeDialog(@StringRes int titleId,
                              @ArrayRes int listId, Class target) {
    MyLog.d(LOG_TAG, "Task type dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(titleId).setItems(listId, (dialog, pos) -> {
      Intent intent = new Intent(this, target);
      intent.putExtra(TSTU.EXTRA_TYPE, pos + 1);
      startActivity(intent);
    });
    MyLog.d(LOG_TAG, "Task tialog created");
    adb.create().show();
  }
}
