package ru.didim99.tstu.ui.os;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.os.MappingInfo;
import ru.didim99.tstu.core.os.ProcessInfo;
import ru.didim99.tstu.core.os.ProcessList;
import ru.didim99.tstu.core.os.ProcessScanner;
import ru.didim99.tstu.ui.BaseActivity;

/**
 * Created by didim99 on 25.03.19.
 */
public class ProcessActivity extends BaseActivity
  implements ProcessScanner.OnUpdateListener {
  private static final String KEY_SHOW_KT = "os.pi.showKT";

  private ProcessScanner scanner;
  private ProcessAdapter adapter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_simple_list);

    adapter = new ProcessAdapter(this,
      this::onProcessClicked, ProcessAdapter.Mode.PROCESS);
    RecyclerView list = findViewById(R.id.rvList);
    list.setLayoutManager(new LinearLayoutManager(this));
    list.setHasFixedSize(true);
    list.setAdapter(adapter);

    boolean showKT = PreferenceManager
      .getDefaultSharedPreferences(this)
      .getBoolean(KEY_SHOW_KT, false);

    scanner = new ProcessScanner(getApplication(), this);
    scanner.setShowKernelThreads(showKT);
    scanner.start();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_processes, menu);
    menu.findItem(R.id.act_showKT).setChecked(
      scanner.isShowKernelThreads());
    return true;
  }

  @Override
  protected void onResume() {
    super.onResume();
    scanner.pause(false);
  }

  @Override
  protected void onPause() {
    super.onPause();
    scanner.pause(true);
  }

  @Override
  public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    scanner.finish();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.act_showKT) {
      boolean showKT = !item.isChecked();
      PreferenceManager.getDefaultSharedPreferences(this)
        .edit().putBoolean(KEY_SHOW_KT, showKT).apply();
      scanner.setShowKernelThreads(showKT);
      item.setChecked(showKT);
      return true;
    } else return super.onOptionsItemSelected(item);
  }

  @Override
  public void onProcessListUpdated(ProcessList processList) {
    adapter.refreshData(processList.getList());
    ActionBar bar = getSupportActionBar();
    if (bar != null) {
      bar.setSubtitle(getString(R.string.os_proc_subtitle,
        processList.getProcessCount(), processList.getThreadCount(),
        processList.getRunningCount()));
    }
  }

  private void onProcessClicked(ProcessInfo process) {
    if (process.isKernelThread()) return;
    if (process.getThreads() < 2) {
      exeDialog(process);
      return;
    }

    scanner.pause(true);
    ArrayList<ProcessInfo> list = scanner.getThreadInfo(process);
    ProcessAdapter adapter = new ProcessAdapter(
      this, null, ProcessAdapter.Mode.THREAD);

    View view = getLayoutInflater().inflate(R.layout.act_simple_list, null);
    RecyclerView rv = view.findViewById(R.id.rvList);
    rv.setLayoutManager(new LinearLayoutManager(this));
    rv.setHasFixedSize(true);
    rv.setAdapter(adapter);

    new AlertDialog.Builder(this)
      .setTitle(getString(R.string.os_threadInfo, process.getPid()))
      .setNeutralButton(R.string.os_executable, (v, d) -> exeDialog(process))
      .setPositiveButton(R.string.dialogButtonOk, null)
      .setOnDismissListener((d) -> scanner.pause(false))
      .setView(view)
      .create()
      .show();

    adapter.refreshData(list);
  }

  private void exeDialog(ProcessInfo process) {
    ArrayList<MappingInfo> list;

    try {
      list = scanner.getMappingInfo(process);
    } catch (IOException e) {
      Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
      return;
    }

    scanner.pause(true);
    MappingAdapter adapter = new MappingAdapter(this);
    View view = getLayoutInflater().inflate(R.layout.act_simple_list, null);
    RecyclerView rv = view.findViewById(R.id.rvList);
    rv.setLayoutManager(new LinearLayoutManager(this));
    rv.setHasFixedSize(true);
    rv.setAdapter(adapter);

    new AlertDialog.Builder(this)
      .setTitle(getString(R.string.os_executableFor, process.getPid()))
      .setPositiveButton(R.string.dialogButtonOk, null)
      .setOnDismissListener((d) -> scanner.pause(false))
      .setView(view)
      .create()
      .show();

    adapter.refreshData(list);
  }
}
