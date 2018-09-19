package ru.didim99.tstu.ui;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import ru.didim99.tstu.R;
import ru.didim99.tstu.TSTU;
import ru.didim99.tstu.utils.MyLog;

public class StartActivity extends AppCompatActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_StartAct";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_start);

    findViewById(R.id.startTranslator).setOnClickListener(v ->
      startActivity(new Intent(this, TranslatorActivity.class)));
    findViewById(R.id.startNumeric).setOnClickListener(v -> selectTypeDialog());
  }

  private void selectTypeDialog() {
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
