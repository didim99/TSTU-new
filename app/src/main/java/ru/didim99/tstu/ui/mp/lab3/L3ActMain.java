package ru.didim99.tstu.ui.mp.lab3;

import android.content.Intent;
import android.os.Bundle;
import ru.didim99.tstu.R;
import ru.didim99.tstu.ui.BaseActivity;

public class L3ActMain extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_task_list);

    findViewById(R.id.task1).setOnClickListener(v -> openTask(ActTask1.class));
    findViewById(R.id.task2).setOnClickListener(v -> openTask(ActTask2.class));
    findViewById(R.id.task3).setOnClickListener(v -> openTask(ActTask31.class));
  }

  private void openTask(Class target) {
    startActivity(new Intent(this, target));
  }
}
