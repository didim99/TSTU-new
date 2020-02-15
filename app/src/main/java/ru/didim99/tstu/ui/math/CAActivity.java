package ru.didim99.tstu.ui.math;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.math.CAProcessor;
import ru.didim99.tstu.core.math.ProcessException;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.ui.utils.SpinnerAdapter;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 14.12.18.
 */
public class CAActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_CAAct";

  // view-elements
  private MenuItem actionConfig;
  private MenuItem actionType;
  private EditText etX, etY;
  private TextView tvOut;
  private Button btnGo;
  private Toast toast;
  // workflow
  private SharedPreferences settings;
  private CAProcessor ca;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    MyLog.d(LOG_TAG, "CAActivity starting...");
    settings = PreferenceManager.getDefaultSharedPreferences(this);
    setContentView(R.layout.act_ca);

    MyLog.d(LOG_TAG, "View components init...");
    etX = findViewById(R.id.etInputX);
    etY = findViewById(R.id.etInputY);
    tvOut = findViewById(R.id.tvOut);
    btnGo = findViewById(R.id.btnGo);
    btnGo.setOnClickListener(v -> compute());
    toast = Toast.makeText(getBaseContext(), "", Toast.LENGTH_LONG);
    MyLog.d(LOG_TAG, "View components init completed");

    try {
      ca = new CAProcessor(this, settings);
    } catch (IOException e) {
      showToast(R.string.errCA_loading, e.toString());
      finish();
    }

    MyLog.d(LOG_TAG, "CAActivity started");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MyLog.d(LOG_TAG, "Creating menu");
    getMenuInflater().inflate(R.menu.menu_mathstat, menu);
    actionConfig = menu.findItem(R.id.act_config);
    actionType = menu.findItem(R.id.act_type);

    if (uiLocked) {
      actionConfig.setVisible(false);
      actionType.setVisible(false);
    }

    MyLog.d(LOG_TAG, "Menu created");
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.act_help:
        helpDialog();
        return true;
      case R.id.act_config:
        configDialog();
        return true;
      case R.id.act_type:
        finish();
        startActivity(new Intent(this, MathStatActivity.class));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void compute() {
    MyLog.d(LOG_TAG, "Reading values...");
    String xStr = etX.getText().toString();
    String yStr = etY.getText().toString();

    try {
      uiLock(true);
      ca.compute(xStr, yStr);
      uiLock(false);
      tvOut.setText(ca.getTextResult(this));
    } catch (ProcessException e) {
      uiLock(false);
      switch (ca.getErrorCode()) {
        case CAProcessor.ErrorCode.EMPTY_X:
          showToast(R.string.errMathStat_emptyX);
          break;
        case CAProcessor.ErrorCode.EMPTY_F:
          showToast(R.string.errCA_emptyY);
          break;
        case CAProcessor.ErrorCode.ELEMENTS_NOT_EQUALS:
          showToast(R.string.errCA_elementsNotEquals);
          break;
        case CAProcessor.ErrorCode.INCORRECT_FORMAT_X:
          showToast(R.string.errCA_incorrectFmtX,
            ca.getBrokenValue());
          break;
        case CAProcessor.ErrorCode.INCORRECT_FORMAT_Y:
          showToast(R.string.errCA_incorrectFmtY,
            ca.getBrokenValue());
          break;
      }
    }
  }

  private void uiLock(boolean lock) {
    if (lock) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = lock;
    btnGo.setEnabled(!lock);
    etX.setEnabled(!lock);
    etY.setEnabled(!lock);
    if (actionConfig != null)
      actionConfig.setVisible(!lock);
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
    adb.setMessage(Html.fromHtml(getString(R.string.ca_help)));
    MyLog.d(LOG_TAG, "Dialog created");
    adb.create().show();
  }

  private void configDialog() {
    if (ca == null) return;
    MyLog.d(LOG_TAG, "Config dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.config);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    adb.setView(R.layout.dia_ca_config);
    AlertDialog dialog = adb.create();
    dialog.setOnShowListener(configListener);
    MyLog.d(LOG_TAG, "Dialog created");
    dialog.show();
  }

  private DialogInterface.OnShowListener configListener = dialogInterface -> {
    MyLog.d(LOG_TAG, "Config dialog shown");
    CAProcessor.Config config = ca.getConfig();
    AlertDialog dialog = (AlertDialog) dialogInterface;
    EditText etAlpha = dialog.findViewById(R.id.etAlpha);
    Spinner regType = dialog.findViewById(R.id.spRegType);
    View rowRFactor = dialog.findViewById(R.id.rowRFactor);
    EditText etRFactor = dialog.findViewById(R.id.etRFactor);

    regType.setSelection(config.getRegType() - 1);
    etAlpha.setText(String.format(Locale.US, "%.2f", config.getAlpha()));
    etRFactor.setText(String.format(Locale.US, "%.2f", config.getRevFactor()));
    if (config.isRegressionReverse())
      rowRFactor.setVisibility(View.VISIBLE);

    regType.setOnItemSelectedListener(new SpinnerAdapter(position -> {
      config.setRegType(position + 1);
      rowRFactor.setVisibility(config.isRegressionReverse()
        ? View.VISIBLE : View.GONE);
    }));

    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
      try {
        InputValidator validator = InputValidator.getInstance();
        float alpha = validator.checkFloat(etAlpha,
          null, 0, 1, R.string.errCA_emptyAlpha,
          R.string.errCA_incorrectAlpha, "alpha");

        float rFactor = config.getRevFactor();
        if (config.isRegressionReverse()) {
          rFactor = validator.checkFloat(etRFactor,
            null, 0, null, R.string.errCA_emptyRFactor,
            R.string.errCA_incorrectRFactor, "rFactor");
        }

        config.update(alpha, rFactor);
        config.updateSettings(settings);
        dialogInterface.dismiss();
        if (!etX.getText().toString().isEmpty())
          compute();
      } catch (InputValidator.ValidationException ignored) {}
    });
  };

  private void showToast(int msgId) {
    toast.setText(msgId);
    toast.show();
  }

  private void showToast(int msgId, Object... formatArgs) {
    toast.setText(getString(msgId, formatArgs));
    toast.show();
  }
}
