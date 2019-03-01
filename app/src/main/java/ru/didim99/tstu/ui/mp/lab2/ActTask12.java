package ru.didim99.tstu.ui.mp.lab2;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.mp.students.Action;
import ru.didim99.tstu.core.mp.students.StudentTask;
import ru.didim99.tstu.ui.BaseActivity;

public class ActTask12 extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_simple_list);

    StudentAdapter adapter = new StudentAdapter(this);
    RecyclerView rv = findViewById(R.id.rvList);
    rv.setLayoutManager(new LinearLayoutManager(this));
    rv.setHasFixedSize(true);
    rv.setAdapter(adapter);

    StudentTask task = new StudentTask(this);
    task.registerEventListener((e, d) -> {
      if (e == CallbackTask.Event.FINISH)
        adapter.refreshData(d);
    });

    task.execute(new Action(Action.Type.VIEW));
  }
}
