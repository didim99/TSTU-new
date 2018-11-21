package ru.didim99.tstu.ui;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.jjoe64.graphview.GraphView;

import java.io.IOException;
import java.util.Locale;

import ru.didim99.tstu.R;
import ru.didim99.tstu.core.math.MathStat;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;

public class MathStatActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_StatAct";
  private static final int TAP_MAX = 2;

  // view-elements
  private MenuItem actionConfig;
  private EditText etX, etF;
  private GraphView graph;
  private TextView tvOut;
  private Button btnGo;
  private Toast toast;
  // workflow
  private SharedPreferences settings;
  private MathStat stat;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    MyLog.d(LOG_TAG, "MathStatActivity starting...");
    settings = PreferenceManager.getDefaultSharedPreferences(this);
    setContentView(R.layout.act_mathstat);

    try {
      stat = new MathStat(this, settings);
    } catch (IOException e) {
      e.printStackTrace();
    }

    setupActionBar();
    MyLog.d(LOG_TAG, "View components init...");
    etX = findViewById(R.id.etInputX);
    etF = findViewById(R.id.etInputF);
    graph = findViewById(R.id.graph);
    tvOut = findViewById(R.id.tvOut);
    btnGo = findViewById(R.id.btnGo);
    btnGo.setOnClickListener(v -> compute());
    graph.setOnLongClickListener(v -> switchGraphType());
    MultiTapCatcher mtc = new MultiTapCatcher();
    mtc.attachToView(graph, TAP_MAX, v -> splitGroups());
    toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "MathStatActivity started");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MyLog.d(LOG_TAG, "Creating menu");
    getMenuInflater().inflate(R.menu.menu_mathstat, menu);
    actionConfig = menu.findItem(R.id.act_config);

    if (uiLocked)
      actionConfig.setVisible(false);

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
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void compute() {
    MyLog.d(LOG_TAG, "Reading values...");
    String xStr = etX.getText().toString();
    String fStr = etF.getText().toString();

    try {
      uiLock(true);
      stat.compute(xStr, fStr);
      uiLock(false);
      stat.drawGraph(graph);
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
        case MathStat.ErrorCode.TOTAL_N_NOT_DEFINED:
          toast.setText(R.string.errMathStat_emptyTotalN);
          toast.show();
          break;
      }
    }
  }

  private boolean switchGraphType() {
    if (stat != null && stat.hasResult()) {
      stat.switchGraphType();
      stat.drawGraph(graph);
    }

    return true;
  }

  private void splitGroups() {
    if (stat != null && stat.hasResult())
      stat.splitGroups(graph);
  }

  private void uiLock(boolean lock) {
    if (lock) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = lock;
    btnGo.setEnabled(!lock);
    etX.setEnabled(!lock);
    etF.setEnabled(!lock);
    graph.setClickable(!lock);
    if (actionConfig != null)
      actionConfig.setVisible(!lock);

    if (lock) {
      MyLog.d(LOG_TAG, "Clearing UI...");
      tvOut.setText(null);
      graph.setTitle(null);
      graph.removeAllSeries();
      MyLog.d(LOG_TAG, "UI cleared");
      graph.setVisibility(View.INVISIBLE);
      MyLog.d(LOG_TAG, "UI locked");
    } else {
      graph.setVisibility(View.VISIBLE);
      MyLog.d(LOG_TAG, "UI unlocked");
    }
  }

  private void helpDialog() {
    MyLog.d(LOG_TAG, "Help dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.help);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setMessage(Html.fromHtml(getString(R.string.mathStat_help)));
    MyLog.d(LOG_TAG, "Dialog created");
    adb.create().show();
  }

  private void configDialog() {
    if (stat == null) return;
    MyLog.d(LOG_TAG, "Config dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.config);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    adb.setView(R.layout.mathstat_config);
    AlertDialog dialog = adb.create();
    dialog.setOnShowListener(configListener);
    MyLog.d(LOG_TAG, "Dialog created");
    dialog.show();
  }

  private DialogInterface.OnShowListener configListener = dialogInterface -> {
    MyLog.d(LOG_TAG, "Config dialog shown");
    MathStat.Config config = stat.getConfig();
    AlertDialog dialog = (AlertDialog) dialogInterface;
    CheckBox cbCorrectDelta = dialog.findViewById(R.id.cbCorrectDelta);
    CheckBox cbGeneralStat = dialog.findViewById(R.id.cbGeneralStat);
    CheckBox cbReturnX = dialog.findViewById(R.id.cbReturnX);
    EditText etGamma = dialog.findViewById(R.id.etGamma);
    EditText etTotalN = dialog.findViewById(R.id.etTotalN);

    if (config.getTotalN() > 0)
      etTotalN.setText(String.valueOf(config.getTotalN()));
    etGamma.setText(String.format(Locale.US, "%.2f", config.getGamma()));
    cbCorrectDelta.setChecked(config.isDeltaCorrection());
    cbGeneralStat.setChecked(config.isGeneralStatEnabled());
    cbReturnX.setChecked(config.isReturnX());
    cbReturnX.setEnabled(cbGeneralStat.isChecked());
    etGamma.setEnabled(cbGeneralStat.isChecked());
    etTotalN.setEnabled(!cbReturnX.isChecked());

    cbReturnX.setOnCheckedChangeListener((view, isChecked)
      -> etTotalN.setEnabled(!isChecked));
    cbGeneralStat.setOnCheckedChangeListener((view, isChecked) -> {
      etTotalN.setEnabled(isChecked && !cbReturnX.isChecked());
      cbReturnX.setEnabled(isChecked);
      etGamma.setEnabled(isChecked);
    });

    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
      try {
        InputValidator validator = InputValidator.getInstance();
        float gamma = validator.checkFloat(etGamma,
          null, 0, 1, R.string.errMathStat_emptyGamma,
          R.string.errMathStat_incorrectGamma, "gamma");
        config.update(cbCorrectDelta.isChecked(),
          cbGeneralStat.isChecked(), cbReturnX.isChecked(), gamma);
        if (config.isGeneralStatEnabled() && !config.isReturnX()) {
          config.setTotalN(validator.checkInteger(etTotalN,
            1, R.string.errMathStat_emptyTotalN,
            R.string.errMathStat_incorrectTotalN, "totalN"));
        }

        config.updateSettings(settings);
        dialogInterface.dismiss();
        if (!etX.getText().toString().isEmpty())
          compute();
      } catch (InputValidator.ValidationException ignored) {}
    });
  };

  @Override
  protected void onSetupActionBar(ActionBar bar) {}
}
