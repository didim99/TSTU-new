package ru.didim99.tstu.ui.os.scheduler;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.jjoe64.graphview.GraphView;
import java.util.ArrayList;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.os.scheduler.HWConfig;
import ru.didim99.tstu.core.os.scheduler.IOManager;
import ru.didim99.tstu.core.os.scheduler.Process;
import ru.didim99.tstu.core.os.scheduler.Processor;
import ru.didim99.tstu.core.os.scheduler.Scheduler;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.ui.view.ValueBar;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 22.04.19.
 */
public class SchedulerActivity extends BaseActivity
  implements Scheduler.EventListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_SchedAct";
  private static final String LIST_FILE = "/scheduler/processList.json";
  private static final int CPU_LOG_SIZE = 40;

  // View-elements
  private ProcessAdapter readyAdapter;
  private ProcessAdapter waitingAdapter;
  private ImageButton clkStart, clkPause;
  private ValueBar vbRAM, vbHDD;
  private GraphLogWrapper cpuUsage;
  private TextView tvRegisters;
  private TextView tvPState;
  private TextView tvPID, tvPName;
  private TextView tvPTime, tvPRAM;
  private ProgressBar pbPState;
  private TextView tvIOState, tvIOQueue;
  private TextView tvIOSector, tvIOBytes;
  private ProgressBar pbIOState;
  private ScrollView logWrapper;
  private TextView tvLog;
  private MenuItem actConfig;
  private CheckBox cbIntManual;
  private Button btnTimer, btnExt;
  // View-elements styling
  private int colorPRegular;
  private int colorPResident;
  private int colorPInterrupt;
  // Workflow
  private Scheduler scheduler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "SchedulerActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_scheduler);

    readyAdapter = new ProcessAdapter(this);
    waitingAdapter = new ProcessAdapter(this);
    waitingAdapter.setProcessColor(R.color.os_yellow);

    MyLog.d(LOG_TAG, "View components init...");
    Resources res = getResources();
    colorPRegular = res.getColor(R.color.colorAccent);
    colorPResident = res.getColor(R.color.os_green);
    colorPInterrupt = res.getColor(R.color.os_red);
    RecyclerView rvReady = findViewById(R.id.rvReady);
    RecyclerView rvWaiting = findViewById(R.id.rvWaiting);
    GraphView graphCPU = findViewById(R.id.graphCPU);
    tvRegisters = findViewById(R.id.tvCPURegValues);
    tvPState = findViewById(R.id.tvProcessState);
    tvPID = findViewById(R.id.tvProcessID);
    tvPName = findViewById(R.id.tvProcessName);
    tvPTime = findViewById(R.id.tvProcessTime);
    tvPRAM = findViewById(R.id.tvProcessRAM);
    pbPState = findViewById(R.id.pbProcessState);
    tvIOState = findViewById(R.id.tvIOState);
    tvIOQueue = findViewById(R.id.tvIOQueue);
    tvIOSector = findViewById(R.id.tvIOSector);
    tvIOBytes = findViewById(R.id.tvIOBytes);
    pbIOState = findViewById(R.id.pbIOState);
    logWrapper = findViewById(R.id.logWrapper);
    tvLog = findViewById(R.id.tvLog);
    vbRAM = findViewById(R.id.vbRAM);
    vbHDD = findViewById(R.id.vbHDD);
    clkStart = findViewById(R.id.btnClkStart);
    clkPause = findViewById(R.id.btnClkPause);
    cbIntManual = findViewById(R.id.cbIntManual);
    btnTimer = findViewById(R.id.btnTimer);
    btnExt = findViewById(R.id.btnExternal);

    rvReady.setHasFixedSize(true);
    rvWaiting.setHasFixedSize(true);
    rvReady.setAdapter(readyAdapter);
    rvWaiting.setAdapter(waitingAdapter);
    rvReady.setLayoutManager(new LinearLayoutManager(this));
    rvWaiting.setLayoutManager(new LinearLayoutManager(this));
    graphCPU.getGridLabelRenderer().setHorizontalLabelsVisible(false);
    graphCPU.getViewport().setScalable(false);
    cpuUsage = new GraphLogWrapper(graphCPU, CPU_LOG_SIZE);
    cpuUsage.setColors(R.color.colorAccent, R.color.colorAccentBg);
    cpuUsage.setBoundsY(Processor.MIN_USAGE, Processor.MAX_USAGE);
    pbPState.setMax(Process.MAX_VIEW_TICKS);
    pbIOState.setMax(IOManager.MAX_PROGRESS);
    pbIOState.getProgressDrawable().setColorFilter(
      res.getColor(R.color.os_yellow), PorterDuff.Mode.SRC_IN);
    tvLog.setMovementMethod(new ScrollingMovementMethod());
    clkStart.setOnClickListener(v -> scheduler.onStartStop());
    clkPause.setOnClickListener(v -> scheduler.togglePause());
    cbIntManual.setOnCheckedChangeListener(
      (v, c) -> scheduler.setInterruptsManual(c));
    btnTimer.setOnClickListener(
      v -> scheduler.getCPU().requestInterrupt());
    btnExt.setOnClickListener(
      v -> scheduler.externalInterrupt());
    MyLog.d(LOG_TAG, "View components init completed");

    scheduler = (Scheduler) getLastCustomNonConfigurationInstance();
    if (scheduler == null) {
      String path = getExternalCacheDir()
        .getAbsolutePath().concat(LIST_FILE);
      Context context = getApplication();
      scheduler = new Scheduler(context);
      scheduler.load(context, path);
    }

    cbIntManual.setChecked(scheduler.isInterruptsManual());
    onHWConfigChanged(scheduler.getHWConfig());
    scheduler.setEventListener(this);
    MyLog.d(LOG_TAG, "SchedulerActivity created");
  }

  @Override
  protected void onResume() {
    super.onResume();
    scheduler.pause(false);
  }

  @Override
  protected void onPause() {
    scheduler.pause(true);
    super.onPause();
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    scheduler.setEventListener(null);
    return scheduler;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_scheduler, menu);
    actConfig = menu.findItem(R.id.act_config);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.act_add: addProcessDialog(); return false;
      case R.id.act_config: configDialog(); return false;
      default: return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onHWConfigChanged(HWConfig config) {
    vbRAM.setMaximum(config.getTotalRAM());
    vbHDD.setMaximum(config.getMaxHDDSpeed());
    tvLog.setText(null);
    cpuUsage.clear();
  }

  @Override
  public void onCLKStateChanged(boolean running, boolean paused) {
    if (actConfig != null)
      actConfig.setVisible(!running);
    clkPause.setEnabled(running);
    clkStart.setImageResource(running ?
      R.drawable.ic_restart_24dp : R.drawable.ic_play_24dp);
    clkPause.setImageResource(paused ?
      R.drawable.ic_play_24dp : R.drawable.is_pause_24dp);
    cbIntManual.setEnabled(!running);
    boolean manual = cbIntManual.isChecked();
    btnTimer.setEnabled(manual && running && !paused);
    btnExt.setEnabled(running && !paused);
  }

  @Override
  public void onProcessAdded(Process process) {
    readyAdapter.refreshData(scheduler.getReadyList());
    vbRAM.setValueInteger(scheduler.getMemoryUsage());
    if (process.getId() >= 0) {
      addLog(getString(R.string.os_scheduler_logAdded,
        process.getId(), process.getName()));
    } else {
      addLog(getString(R.string.os_scheduler_intAdded,
        process.getId()));
    }
  }

  @Override
  public void onProcessFinished(Process process) {
    addLog(getString(R.string.os_scheduler_logFinished,
      process.getId()));
  }

  @Override
  public void onCPUTick(Process process) {
    cpuUsage.append(scheduler.getCPUUsage());
    vbRAM.setValueInteger(scheduler.getMemoryUsage());
    tvRegisters.setText(scheduler.getCPU().getContext());

    if (process != null) {
      tvPTime.setText(String.valueOf(process.getCPUTime()));
      tvPRAM.setText(String.valueOf(process.getRamUsage()));
      pbPState.setProgress(process.getProgress());
      switch (process.getType()) {
        case Process.Type.REGULAR:
          tvPState.setText(getString(R.string.os_scheduler_progress,
            process.GetProgressPercent()));
          pbPState.getProgressDrawable().setColorFilter(
            colorPRegular, PorterDuff.Mode.SRC_IN);
          break;
        case Process.Type.RESIDENT:
          tvPState.setText(R.string.os_scheduler_resident);
          pbPState.getProgressDrawable().setColorFilter(
            colorPResident, PorterDuff.Mode.SRC_IN);
          break;
        case Process.Type.INTERRUPT:
          tvPState.setText(getString(R.string.os_scheduler_progress,
            process.GetProgressPercent()));
          pbPState.getProgressDrawable().setColorFilter(
            colorPInterrupt, PorterDuff.Mode.SRC_IN);
          break;
      }
    } else {
      pbPState.getProgressDrawable().setColorFilter(
        colorPRegular, PorterDuff.Mode.SRC_IN);
      pbPState.setProgress(0);
      tvPState.setText(null);
      tvPTime.setText(null);
      tvPRAM.setText(null);
    }
  }

  @Override
  public void onCPUContextChanged(Process process) {
    readyAdapter.refreshData(scheduler.getReadyList());
    waitingAdapter.refreshData(scheduler.getWaitingList());
    tvRegisters.setText(scheduler.getCPU().getContext());

    if (process != null) {
      addLog(getString(R.string.os_scheduler_logExecuting,
        process.getId()));
      tvPID.setText(String.valueOf(process.getId()));
      tvPName.setText(process.getName());
    } else {
      tvPID.setText(null);
      tvPName.setText(null);
    }

    onCPUTick(process);
  }

  @Override
  public void onIOStateChanged(IOManager manager) {
    vbHDD.setValueInteger(manager.getUsage());
    tvIOQueue.setText(String.valueOf(manager.getQueueLength()));
    tvIOSector.setText(String.format("0x%08X", manager.getCurrSector()));
    tvIOBytes.setText(Utils.formatBytes(manager.getBytesProcessed()));
    pbIOState.setProgress(manager.getProgress());
    if (manager.isIdle()) {
      tvIOState.setText(R.string.os_scheduler_IOIdle);
    } else {
      tvIOState.setText(getString(R.string.os_scheduler_progress,
        manager.getProgressPercent()));
    }
  }

  @Override
  public void onIOOperation(boolean finished, int processID) {
    addLog(getString(finished ? R.string.os_scheduler_logIOFinished
      : R.string.os_scheduler_logIOStarted, processID));
    if (finished) {
      readyAdapter.refreshData(scheduler.getReadyList());
      waitingAdapter.refreshData(scheduler.getWaitingList());
    }
  }

  private void addLog(String msg) {
    tvLog.append(msg.concat("\n"));
    logWrapper.fullScroll(View.FOCUS_DOWN);
  }

  private void addProcessDialog() {
    MyLog.d(LOG_TAG, "AddProcess dialog called");
    ArrayList<String> list = new ArrayList<>();
    list.add(getString(R.string.os_scheduler_createProcess));
    list.addAll(scheduler.getProcessList());

    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.os_scheduler_addProcess);
    adb.setItems(list.toArray(new String[0]), (d, pos) -> {
      if (pos == 0) {
        inputFieldDialog(R.string.os_scheduler_addProcess,
          R.layout.dia_scheduler_create, this::createProcess);
      } else try {
        scheduler.createProcess(pos - 1);
      } catch (Scheduler.AddProcessException e) {
        Toast.makeText(this, R.string.errScheduler_noMemory,
          Toast.LENGTH_LONG).show();
      }
    });
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    MyLog.d(LOG_TAG, "AddProcess dialog created");
    adb.create().show();
  }

  private void configDialog() {
    MyLog.d(LOG_TAG, "Config dialog called");
    HWConfig config = scheduler.getHWConfig();
    View view = getLayoutInflater().inflate(R.layout.dia_scheduler_config, null);
    ((EditText) view.findViewById(R.id.etTicks))
      .setText(String.valueOf(config.getTicks()));
    ((EditText) view.findViewById(R.id.etRAM))
      .setText(String.valueOf(config.getTotalRAM()));
    ((EditText) view.findViewById(R.id.etHDD))
      .setText(String.valueOf(config.getMaxHDDSpeed()));

    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.os_scheduler_hwConfig);
    adb.setView(view);
    adb.setPositiveButton(R.string.dialogButtonSave, null);
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    MyLog.d(LOG_TAG, "Config dialog created");
    AlertDialog dialog = adb.create();
    dialog.setOnShowListener((di) -> {
      AlertDialog d = (AlertDialog) di;
      d.getButton(AlertDialog.BUTTON_POSITIVE)
        .setOnClickListener(v -> applyHWConfig(d));
    });
    dialog.show();
  }

  private void applyHWConfig(AlertDialog d) {
    try {
      InputValidator iv = InputValidator.getInstance();
      EditText et = d.findViewById(R.id.etTicks);
      int ticks = iv.checkInteger(et, HWConfig.MIN_TICKS, HWConfig.MAX_TICKS,
        R.string.errScheduler_emptyTicks, R.string.errScheduler_incorrectTicks, "ticks");
      et = d.findViewById(R.id.etRAM);
      int ram = iv.checkInteger(et, HWConfig.MIN_RAM, HWConfig.MAX_RAM,
        R.string.errScheduler_emptyRAM, R.string.errScheduler_incorrectRAM, "RAM");
      et = d.findViewById(R.id.etHDD);
      int hdd = iv.checkInteger(et, HWConfig.MIN_HDD, HWConfig.MAX_HDD,
        R.string.errScheduler_emptyHDD, R.string.errScheduler_incorrectHDD, "HDD");

      HWConfig config = scheduler.getHWConfig();
      config.update(ticks, ram, hdd);
      config.saveConfig(getApplication());
      d.dismiss();
    } catch (InputValidator.ValidationException ignored) {}
  }

  private void createProcess(AlertDialog d) {
    try {
      InputValidator iv = InputValidator.getInstance();
      EditText et = d.findViewById(R.id.etName);
      String name = iv.checkEmptyStr(et, R.string.errScheduler_emptyPName, "name");
      et = d.findViewById(R.id.etTicks);
      Integer ticks = iv.checkInteger(et, HWConfig.MIN_PTICKS, HWConfig.MAX_PTICKS,
        0, R.string.errScheduler_incorrectTime, "total ticks");
      et = d.findViewById(R.id.etBaseRAM);
      int bRAM = iv.checkInteger(et, HWConfig.MIN_BRAM, HWConfig.MAX_BRAM,
        R.string.errScheduler_emptyBRAM,
        R.string.errScheduler_incorrectBRAM, "base RAM");
      et = d.findViewById(R.id.etDRAM);
      int dRAM = iv.checkInteger(et, HWConfig.MIN_DRAM, HWConfig.MAX_DRAM,
        R.string.errScheduler_emptyBRAM,
        R.string.errScheduler_incorrectBRAM, "dynamic RAM");
      et = d.findViewById(R.id.etHDD);
      Integer ioRate = iv.checkInteger(et, HWConfig.MIN_IORATE, HWConfig.MAX_IORATE,
        0, R.string.errScheduler_incorrectIO, "IO rate");

      scheduler.createProcess(new Process.Info(name, ticks,
        bRAM << 10, dRAM << 10, ioRate));
      d.dismiss();
    } catch (Scheduler.AddProcessException e) {
      Toast.makeText(this, R.string.errScheduler_noMemory,
        Toast.LENGTH_LONG).show();
    } catch (InputValidator.ValidationException ignored) {}
  }
}
