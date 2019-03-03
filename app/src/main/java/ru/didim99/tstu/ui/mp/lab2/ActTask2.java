package ru.didim99.tstu.ui.mp.lab2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.mp.radio.RadioTask;
import ru.didim99.tstu.ui.BaseActivity;

public class ActTask2 extends BaseActivity {

  private RadioTask task;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_simple_list);

    RadioStatAdapter adapter = new RadioStatAdapter(this);
    RecyclerView rv = findViewById(R.id.rvList);
    rv.setLayoutManager(new LinearLayoutManager(this));
    rv.setHasFixedSize(true);
    rv.setAdapter(adapter);

    task = (RadioTask) getLastCustomNonConfigurationInstance();
    if (task == null) task = new RadioTask(getApplicationContext());
    task.setOnStatUpdateListener(adapter::refreshData);
    if (task.getStatus() == AsyncTask.Status.PENDING)
      task.execute();
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    if (task != null) task.setOnStatUpdateListener(null);
    return task;
  }
}
