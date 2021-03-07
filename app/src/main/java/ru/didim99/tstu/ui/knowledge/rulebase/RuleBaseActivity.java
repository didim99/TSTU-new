package ru.didim99.tstu.ui.knowledge.rulebase;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.knowledge.rulebase.RuleBase;
import ru.didim99.tstu.core.knowledge.rulebase.RuleBaseLoader;
import ru.didim99.tstu.core.knowledge.rulebase.Variable;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 28.02.21.
 */

public class RuleBaseActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_RBAct";

  // View-elements
  private Button btnLoad;
  private Button btnStart;
  private Spinner spTarget;
  private Switch swBackward;
  private TextView tvReviewCount;
  private RuleAdapter ruleAdapter;
  private FactAdapter factAdapter;
  // Workflow
  private RuleBase ruleBase;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "RuleBaseActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_rule_base);

    MyLog.d(LOG_TAG, "View components init...");
    RecyclerView rvRuleList = findViewById(R.id.rvRules);
    RecyclerView rvFactList = findViewById(R.id.rvFacts);
    btnLoad = findViewById(R.id.btnLoad);
    btnStart = findViewById(R.id.btnStart);
    spTarget = findViewById(R.id.spTargetVar);
    swBackward = findViewById(R.id.swDirection);
    tvReviewCount = findViewById(R.id.tvReviewCount);

    ruleAdapter = new RuleAdapter(this);
    rvRuleList.setLayoutManager(new LinearLayoutManager(this));
    rvRuleList.setAdapter(ruleAdapter);
    factAdapter = new FactAdapter(this);
    rvFactList.setLayoutManager(new LinearLayoutManager(this));
    rvFactList.setAdapter(factAdapter);

    btnLoad.setOnClickListener(v -> openFile());
    btnStart.setOnClickListener(v -> startSearch());
    updateControlsState(false);
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "Loading saved instance...");
    ruleBase = (RuleBase) getLastCustomNonConfigurationInstance();
    if (ruleBase != null) {
      MyLog.d(LOG_TAG, "Instance loaded: " + ruleBase);
      onRuleBaseLoaded();
    } else {
      MyLog.d(LOG_TAG, "No existing instance found");
    }

    MyLog.d(LOG_TAG, "RuleBaseActivity started");
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent i) {
    if (requestCode == REQUEST_GET_FILE) {
      if (resultCode == RESULT_OK) {
        Uri data = i.getData();
        if (data != null) {
          String path = data.getPath();
          if (path != null && path.endsWith(RuleBase.FILE_MASK)) {
            MyLog.d(LOG_TAG, "Loading rule base: " + path);
            RuleBaseLoader loader = new RuleBaseLoader(this);
            loader.registerEventListener(this::onBaseLoadEvent);
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
    return ruleBase;
  }

  private void onBaseLoadEvent(CallbackTask.Event event, RuleBase ruleBase) {
    boolean lock = event == CallbackTask.Event.START;
    btnLoad.setText(lock ? R.string.loading : R.string.buttonLoad);
    btnLoad.setEnabled(!lock);
    if (lock) clearView();
    if (ruleBase != null) {
      this.ruleBase = ruleBase;
      onRuleBaseLoaded();
    }
  }

  private void onRuleBaseLoaded() {
    spTarget.setAdapter(new ArrayAdapter<>(
      this, android.R.layout.simple_list_item_1,
      ruleBase.getVarNames()));
    spTarget.setSelection(ruleBase.getTargetVarIndex());
    ruleAdapter.setSource(ruleBase);
    factAdapter.setSource(ruleBase);
    updateControlsState(false);
  }

  private void onSearchEvent(RuleBase.SearchEvent event) {
    switch (event) {
      case START:
        updateControlsState(true);
        break;
      case FINISH:
        updateControlsState(false);
        break;
      case STEP:
        updateView();
        break;
      case QUESTION:
        processQuestions();
        break;
    }
  }

  private void startSearch() {
    updateView();
    ruleBase.setTargetVariable(spTarget.getSelectedItemPosition());
    ruleBase.setDirection(swBackward.isChecked() ?
      RuleBase.Direction.BACKWARD : RuleBase.Direction.FORWARD);
    ruleBase.startSearch(this::onSearchEvent);
  }

  private void clearView() {
    tvReviewCount.setText(String.valueOf(0));
    spTarget.setAdapter(null);
  }

  private void updateControlsState(boolean lock) {
    boolean hasBase = ruleBase != null;
    spTarget.setEnabled(hasBase && !lock);
    swBackward.setEnabled(hasBase && !lock);
    btnStart.setEnabled(hasBase && !lock);
    btnLoad.setEnabled(!lock);
    updateView();
  }

  private void updateView() {
    if (ruleBase != null)
      tvReviewCount.setText(String.valueOf(ruleBase.getReviewCount()));
    ruleAdapter.notifyDataSetChanged();
    factAdapter.notifyDataSetChanged();
  }

  private void processQuestions() {
    Variable question = ruleBase.enqueueRequest();
    MyLog.d(LOG_TAG, "Processing question: " + question);
    if (question == null) return;

    AlertDialog.Builder adb = new AlertDialog.Builder(this);

    switch (question.getType()) {
      case BOOLEAN:
        adb.setTitle(getString(R.string.knowledge_question))
          .setMessage(Utils.capitalizeFirst(getString(
            R.string.knowledge_questionTpl, question.getName())))
          .setPositiveButton(R.string.dialogButtonYes, (d, w) -> onAnswerReceived(question, w))
          .setNegativeButton(R.string.dialogButtonNo, (d, w) -> onAnswerReceived(question, w));
        break;
      case ENUM:
        adb.setTitle(getString(R.string.knowledge_selectValue, question.getName()))
          .setItems(question.getValuesArray(), (d, w) -> onAnswerReceived(question, w));
        break;
      case FLOAT:
        adb.setTitle(getString(R.string.knowledge_enterValue, question.getName()))
          .setPositiveButton(R.string.dialogButtonOk, null)
          .setView(R.layout.dia_sf_decimal);
        break;
    }

    AlertDialog dialog = adb.create();

    if (question.getType() == Variable.Type.FLOAT) {
      dialog.setOnShowListener((di) -> {
        AlertDialog d = (AlertDialog) di;
        d.getButton(AlertDialog.BUTTON_POSITIVE)
          .setOnClickListener(v -> onTextAnswerReceived(question, d));
      });
    }

    dialog.show();
  }

  private void onAnswerReceived(Variable request, int answerId) {
    switch (request.getType()) {
      case BOOLEAN:
        if (answerId == DialogInterface.BUTTON_POSITIVE)
          request.setValue(Variable.BOOLEAN_TRUE);
        else if (answerId == DialogInterface.BUTTON_NEGATIVE)
          request.setValue(Variable.BOOLEAN_FALSE);
        break;
      case ENUM:
        request.setValueByIndex(answerId);
        break;
    }

    ruleBase.addFact(request);
    processQuestions();
  }

  private void onTextAnswerReceived(Variable request, AlertDialog d) {
    try {
      InputValidator iv = InputValidator.getInstance();
      float value = iv.checkFloat(d.findViewById(R.id.etInput),
        1, R.string.errGeneric_emptyValue,
        R.string.errGeneric_incorrectValue, request.getName());

      request.setValue(String.valueOf(value));
      ruleBase.addFact(request);
      processQuestions();
      d.dismiss();
    } catch (InputValidator.ValidationException ignored) {}
  }
}
