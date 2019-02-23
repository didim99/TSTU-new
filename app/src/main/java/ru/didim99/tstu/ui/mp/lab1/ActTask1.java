package ru.didim99.tstu.ui.mp.lab1;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import ru.didim99.tstu.R;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.MyLog;

public class ActTask1 extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MP";

  private int state = 0;
  private LinearLayout root;
  private Button b1, b2, b3;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_l1_t1_p1);
    root = findViewById(R.id.rootLayout);
    root.setOnClickListener(v -> switchState());
    b1 = findViewById(R.id.mp_btn1);
    b2 = findViewById(R.id.mp_btn2);
    b3 = findViewById(R.id.mp_btn3);
  }

  private void switchState() {
    MyLog.d(LOG_TAG, "State: " + state);
    FrameLayout.LayoutParams params;
    switch (state++) {
      case 0:
        root.setOrientation(LinearLayout.VERTICAL);
        break;
      case 1:
        int p = b1.getPaddingLeft() + 20;
        b2.setPadding(p, p, p, p);
        p += 20;
        b3.setPadding(p, p, p, p);
        break;
      case 2:
        setContentView(R.layout.act_mp_l1_t1_p2);
        findViewById(R.id.rootLayout)
          .setOnClickListener(v1 -> switchState());
        b1 = findViewById(R.id.mp_btn1);
        b2 = findViewById(R.id.mp_btn2);
        b3 = findViewById(R.id.mp_btn3);
        break;
      case 3:
        params = (FrameLayout.LayoutParams) b1.getLayoutParams();
        params.gravity = Gravity.CENTER;
        b1.setLayoutParams(params);
        params = (FrameLayout.LayoutParams) b2.getLayoutParams();
        params.gravity = Gravity.CENTER;
        b2.setLayoutParams(params);
        params = (FrameLayout.LayoutParams) b3.getLayoutParams();
        params.gravity = Gravity.CENTER;
        b3.setLayoutParams(params);
        break;
      case 4:
        params = (FrameLayout.LayoutParams) b1.getLayoutParams();
        params.gravity = Gravity.CENTER_HORIZONTAL;
        b1.setLayoutParams(params);
        params = (FrameLayout.LayoutParams) b2.getLayoutParams();
        params.gravity = Gravity.START;
        b2.setLayoutParams(params);
        params = (FrameLayout.LayoutParams) b3.getLayoutParams();
        params.gravity = Gravity.END;
        b3.setLayoutParams(params);
        break;
      case 5:
        setContentView(R.layout.act_mp_l1_t1_p3);
        findViewById(R.id.rootLayout)
          .setOnClickListener(v1 -> switchState());
        break;
      case 6:
        finish();
        startActivity(getIntent());
        break;
    }
  }
}
