package ru.didim99.tstu.ui.mp.lab2;

import android.os.Bundle;
import ru.didim99.tstu.R;

public class ActTask33 extends CameraActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_l2_t3_p3);
    findViewById(R.id.mp_btn1).setOnClickListener(v -> cm.switchFlash());
    findViewById(R.id.mp_btn2).setOnClickListener(v -> cm.switchFaceDetection());
    actNext = ActTask31.class;
    actPrev = ActTask32.class;
  }
}
