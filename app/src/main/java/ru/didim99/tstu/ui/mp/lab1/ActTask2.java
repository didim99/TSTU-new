package ru.didim99.tstu.ui.mp.lab1;

import android.os.Bundle;
import android.view.View;
import ru.didim99.tstu.R;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.MyLog;

public class ActTask2 extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MP";

  private int state = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_l1_t2_p1);
    View root = findViewById(R.id.rootLayout);
    root.setOnClickListener(v -> switchState());
  }

  private void switchState() {
    MyLog.d(LOG_TAG, "State: " + state);
    switch (state++) {
      case 0:
        setContentView(R.layout.act_mp_l1_t2_p2);
        findViewById(R.id.rootLayout)
          .setOnClickListener(v1 -> switchState());
        break;
      case 1:
        setContentView(R.layout.act_mp_l1_t2_p3);
        findViewById(R.id.rootLayout)
          .setOnClickListener(v1 -> switchState());
        break;
      case 2:
        setContentView(R.layout.act_mp_l1_t2_p4);
        findViewById(R.id.rootLayout)
          .setOnClickListener(v1 -> switchState());
        break;
      case 3:
        finish();
        startActivity(getIntent());
        break;
    }
  }
}
