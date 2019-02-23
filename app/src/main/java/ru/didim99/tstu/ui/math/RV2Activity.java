package ru.didim99.tstu.ui.math;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.math.ProcessException;
import ru.didim99.tstu.core.math.RVProcessor;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 30.11.18.
 */
public class RV2Activity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_RV2Act";

  // view-elements
  private MenuItem actionType;
  private Button btnGo;
  private EditText etInput;
  private TextView tvOut;
  private Toast toast;
  private boolean screenLarge;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "RV2Activity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_rv2);

    screenLarge = (getResources().getConfiguration().screenLayout
      & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;

    MyLog.d(LOG_TAG, "View components init...");
    etInput = findViewById(R.id.etInput);
    btnGo = findViewById(R.id.btnGo);
    tvOut = findViewById(R.id.tvOut);
    btnGo.setOnClickListener(v -> compute());
    etInput.setText(RVProcessor.DEFAULT_TITLE);
    etInput.setSelection(etInput.getText().length());
    toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "MathStatActivity started");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MyLog.d(LOG_TAG, "Creating menu");
    getMenuInflater().inflate(R.menu.menu_rv2, menu);
    actionType = menu.findItem(R.id.act_type);
    if (uiLocked)
      actionType.setVisible(false);
    MyLog.d(LOG_TAG, "Menu created");
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.act_help:
        helpDialog();
        return true;
      case R.id.act_type:
        finish();
        startActivity(new Intent(this, CAActivity.class));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void compute() {
    String input = etInput.getText().toString();
    RVProcessor processor = new RVProcessor();

    try {
      uiLock(true);
      processor.compute(input);
      uiLock(false);
      tvOut.setText(processor.getTextResult(this, screenLarge));
    } catch (ProcessException e) {
      uiLock(false);
      switch (processor.getErrorCode()) {
        case RVProcessor.ErrorCode.EMPTY_INPUT:
          showToast(R.string.errRV_emptyInput);
          break;
        case RVProcessor.ErrorCode.INCORRECT_FORMAT:
          showToast(R.string.errRV_incorrectFormat);
          break;
        case RVProcessor.ErrorCode.TITLE_NOT_DEFINED:
          showToast(R.string.errRV_titleNotDefined);
          break;
        case RVProcessor.ErrorCode.TITLE_INCORRECT:
          showToast(R.string.errRV_titleIncorrect);
          break;
        case RVProcessor.ErrorCode.INCORRECT_X:
          showToast(R.string.errRV_incorrectX, processor.getBrokenX());
          break;
        case RVProcessor.ErrorCode.INCORRECT_Y:
          showToast(R.string.errRV_incorrectY, processor.getBrokenX());
          break;
        case RVProcessor.ErrorCode.INCORRECT_F:
          showToast(R.string.errRV_incorrectF,
            processor.getBrokenX(), processor.getBrokenY());
          break;
        case RVProcessor.ErrorCode.SUM_OUT_OF_RANGE:
          showToast(R.string.errRV_sumOutOfRange);
          break;
      }
    }
  }

  private void uiLock(boolean lock) {
    if (lock) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = lock;
    etInput.setEnabled(!lock);
    btnGo.setEnabled(!lock);
    if (actionType != null)
      actionType.setVisible(!lock);

    if (lock) {
      MyLog.d(LOG_TAG, "Clearing UI...");
      tvOut.setText(null);
      MyLog.d(LOG_TAG, "UI cleared");
      MyLog.d(LOG_TAG, "UI locked");
    } else {
      MyLog.d(LOG_TAG, "UI unlocked");
    }
  }

  private void helpDialog() {
    MyLog.d(LOG_TAG, "Help dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.help);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setMessage(Html.fromHtml(getString(R.string.rv_help)));
    MyLog.d(LOG_TAG, "Dialog created");
    adb.create().show();
  }

  private void showToast(int msgId) {
    toast.setText(msgId);
    toast.show();
  }

  private void showToast(int msgId, Object... formatArgs) {
    toast.setText(getString(msgId, formatArgs));
    toast.show();
  }
}
