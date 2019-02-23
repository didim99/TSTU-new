package ru.didim99.tstu.ui.mp.lab1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import ru.didim99.tstu.R;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.MyLog;

public class ActTask3 extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MP";

  private int state = 0;
  private TextView tv;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_l1_t3_p1);
    View root = findViewById(R.id.rootLayout);
    root.setOnClickListener(v -> switchState());
    tv = findViewById(R.id.mp_text);
  }

  private void switchState() {
    MyLog.d(LOG_TAG, "State: " + state);
    switch (state++) {
      case 0:
        tv.setTextColor(0xff009688);
        break;
      case 1:
        tv.setTextSize(16f);
        break;
      case 2:
        setContentView(R.layout.act_mp_l1_t3_p2);
        findViewById(R.id.rootLayout)
          .setOnClickListener(v1 -> switchState());
        tv = findViewById(R.id.mp_text);
        Button b1 = findViewById(R.id.mp_btn1);
        b1.setOnClickListener(v -> tv.setText(R.string.mp_sampleText2));
        tv.setOnClickListener(v -> tv.setText(R.string.mp_sampleText));
        break;
      case 3:
        finish();
        startActivity(getIntent());
        break;
    }
  }
}
