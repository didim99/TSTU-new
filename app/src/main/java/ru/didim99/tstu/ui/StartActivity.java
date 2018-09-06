package ru.didim99.tstu.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import ru.didim99.tstu.R;

public class StartActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_start);

    findViewById(R.id.startTranslator).setOnClickListener(v ->
      startActivity(new Intent(this, TranslatorActivity.class)));
  }
}
