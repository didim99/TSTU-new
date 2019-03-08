package ru.didim99.tstu.ui.mp.lab1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import ru.didim99.tstu.R;
import ru.didim99.tstu.ui.BaseActivity;

public class L1ActMain extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_task_list);

    findViewById(R.id.mp_extended).setVisibility(View.VISIBLE);
    findViewById(R.id.task1).setOnClickListener(v -> openTask(ActTask1.class));
    findViewById(R.id.task2).setOnClickListener(v -> openTask(ActTask2.class));
    findViewById(R.id.task3).setOnClickListener(v -> openTask(ActTask3.class));
    findViewById(R.id.task4).setOnClickListener(v -> openTask(ActTask41.class));
    findViewById(R.id.task5).setOnClickListener(v -> openTask(ActTask51.class));
  }

  public void openTask(Class target) {
    startActivity(new Intent(this, target));
  }
}
