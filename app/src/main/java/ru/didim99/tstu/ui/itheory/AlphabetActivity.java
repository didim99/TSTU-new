package ru.didim99.tstu.ui.itheory;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.itheory.InformationCounter;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.ui.SpinnerAdapter;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 13.02.20.
 */
public class AlphabetActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_AlphabetAct";

  // View elements
  private EditText etMessage;
  private ImageView ivCustomAlphabet;
  private TextView tvAlphabet;
  private TextView tvMessage;
  // Workflow
  private InformationCounter counter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "AlphabetActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_ti_alphabet);

    MyLog.d(LOG_TAG, "View components init...");
    Spinner spAlphabetType = findViewById(R.id.spAlphabetType);
    etMessage = findViewById(R.id.etMessage);
    ivCustomAlphabet = findViewById(R.id.ivCustomAlphabet);
    tvAlphabet = findViewById(R.id.tvAlphabet);
    tvMessage = findViewById(R.id.tvMessage);
    spAlphabetType.setOnItemSelectedListener(
      new SpinnerAdapter(this::onAlphabetTypeChanged));
    ivCustomAlphabet.setOnClickListener(v -> customAlphabetDialog());
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "Connecting InformationCounter...");
    counter = (InformationCounter) getLastCustomNonConfigurationInstance();
    if (counter != null) {
      MyLog.d(LOG_TAG, "Connected to: " + counter);
      spAlphabetType.setSelection(counter.getAlphabetType());
    } else {
      MyLog.d(LOG_TAG, "No existing InformationCounter found");
      counter = new InformationCounter(this);
    }

    onAlphabetTypeChanged(counter.getAlphabetType());
    etMessage.addTextChangedListener(textWatcher);

    MyLog.d(LOG_TAG, "AlphabetActivity created");
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    return counter;
  }

  private void onAlphabetTypeChanged(int id) {
    counter.setAlphabetType(id);
    tvAlphabet.setText(counter.getAlphabetInfo(this));
    tvMessage.setText(counter.getMessageInfo(etMessage.getText().toString()));
    ivCustomAlphabet.setVisibility(id == InformationCounter.AlphabetType.CUSTOM
      ? View.VISIBLE : View.GONE);
  }

  private void customAlphabetDialog() {
    View view = getLayoutInflater().inflate(R.layout.dia_ti_alphabet, null);
    ((EditText) view.findViewById(R.id.etInput)).setText(counter.getCustomAlphabet());

    AlertDialog.Builder adb = new AlertDialog.Builder(this)
      .setTitle(R.string.iTheory_customAlphabet).setView(view)
      .setPositiveButton(R.string.dialogButtonOk, null)
      .setNegativeButton(R.string.dialogButtonCancel, null);
    MyLog.d(LOG_TAG, "Custom alphabet dialog created");
    AlertDialog dialog = adb.create();
    dialog.setOnShowListener((di) -> {
      AlertDialog d = (AlertDialog) di;
      d.getButton(AlertDialog.BUTTON_POSITIVE)
        .setOnClickListener(v -> onCustomAlphabetChanged(d));
    });
    dialog.show();
  }

  private void onCustomAlphabetChanged(AlertDialog dialog) {
    try {
      InputValidator iv = InputValidator.getInstance();
      String alphabet = iv.checkEmptyStr(dialog.findViewById(R.id.etInput),
        R.string.errTI_emptyAlphabet, "alphabet");
      counter.setCustomAlphabet(this, alphabet);
      onAlphabetTypeChanged(InformationCounter.AlphabetType.CUSTOM);
      dialog.dismiss();
    } catch (InputValidator.ValidationException ignored) {}
  }

  private TextWatcher textWatcher = new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
      tvMessage.setText(counter.getMessageInfo(s.toString()));
    }
  };
}
