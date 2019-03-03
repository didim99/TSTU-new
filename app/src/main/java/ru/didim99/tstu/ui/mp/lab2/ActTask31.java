package ru.didim99.tstu.ui.mp.lab2;

import android.os.Bundle;
import ru.didim99.tstu.R;

public class ActTask31 extends CameraActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_l2_t3_p1);
    findViewById(R.id.mp_btn1).setOnClickListener(v -> cm.takePhoto());
    findViewById(R.id.mp_btn2).setOnClickListener(v -> cm.close());
    actNext = ActTask32.class;
    actPrev = ActTask33.class;
  }
}
