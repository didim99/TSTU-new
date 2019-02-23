package ru.didim99.tstu.ui.mp.lab1;

import android.content.Intent;
import android.os.Bundle;
import ru.didim99.tstu.R;
import ru.didim99.tstu.ui.BaseActivity;

public class ActTask51 extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_l1_t5_p1);

    findViewById(R.id.mp_btn1).setOnClickListener(
      v -> startActivity(new Intent(this, ActTask52.class)));
    findViewById(R.id.mp_btn2).setOnClickListener(
      v -> startActivity(new Intent(this, ActTask53.class)));
    findViewById(R.id.mp_btn3).setOnClickListener(
      v -> startActivity(new Intent(this, ActTask54.class)));
    findViewById(R.id.mp_btn4).setOnClickListener(v -> finish());
  }
}
