package ru.didim99.tstu.ui.mp.lab1;

import android.os.Bundle;
import android.widget.TextView;
import ru.didim99.tstu.R;
import ru.didim99.tstu.ui.BaseActivity;

public class ActTask42 extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_l1_t4_p2);
    ((TextView) findViewById(R.id.mp_text))
      .setText(getIntent().getStringExtra("payload"));
  }
}
