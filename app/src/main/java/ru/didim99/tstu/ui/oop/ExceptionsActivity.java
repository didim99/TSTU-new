package ru.didim99.tstu.ui.oop;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.oop.ArithmeticTester;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 16.04.19.
 */
public class ExceptionsActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ISAct";
  private final static int S_MIN = 0;
  private final static int S_MAX = 100;

  private EditText etSize;
  private TextView tvOut;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "ExceptionsActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_exceptions);

    MyLog.d(LOG_TAG, "View components init...");
    etSize = findViewById(R.id.etSize);
    tvOut = findViewById(R.id.tvOut);
    findViewById(R.id.btnGo).setOnClickListener(v -> test());
    MyLog.d(LOG_TAG, "ExceptionsActivity created");
  }

  private void test() {
    try {
      int size = InputValidator.getInstance().checkInteger(etSize, S_MIN, S_MAX,
        R.string.errOOP_emptySize, R.string.errOOP_incorrectSize, "size");
      tvOut.setText(ArithmeticTester.test(size));
    } catch (InputValidator.ValidationException ignored) {}
  }
}
