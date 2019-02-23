package ru.didim99.tstu.ui.mp.lab1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import ru.didim99.tstu.R;
import ru.didim99.tstu.ui.BaseActivity;

public class ActTask41 extends BaseActivity {

  private EditText input;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_l1_t4_p1);
    input = findViewById(R.id.mp_input);
    findViewById(R.id.mp_btn1)
      .setOnClickListener(v1 -> go());
  }

  private void go() {
    String str = input.getText().toString();
    Intent intent = new Intent(this, ActTask42.class);
    intent.putExtra("payload", str);
    startActivity(intent);
  }
}
