package ru.didim99.tstu.ui.mp.lab2;

import android.os.Bundle;
import ru.didim99.tstu.R;

public class ActTask32 extends CameraActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_l2_t3_p2);
    findViewById(R.id.mp_btn1).setOnClickListener(v -> cm.savePhoto());
    findViewById(R.id.mp_btn2).setOnClickListener(v -> cm.saveToSD());
    actNext = ActTask33.class;
    actPrev = ActTask31.class;
  }
}
