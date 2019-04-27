package ru.didim99.tstu.core.os.scheduler;

import android.content.Context;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 23.04.19.
 */
public class Scheduler
  implements IOManager.StateListener, Processor.TickListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Scheduler";
  // State
  private boolean running, paused;
  private ArrayList<Process.Info> sourceList;
  private EventListener listener;
  // Hardware
  private Random systemRandom;
  private HWConfig hwConfig;
  private MemoryManager mmu;
  private Processor cpu;
  private IOManager io;
  // Workflow
  private LinkedList<Process> ready;
  private LinkedList<Process> waiting;

  public Scheduler(Context appContext) {
    hwConfig = new HWConfig(appContext, this::reconfigure);
    waiting = new LinkedList<>();
    ready = new LinkedList<>();
    systemRandom = new Random();
    cpu = new Processor(this);
    mmu = new MemoryManager();
    io = new IOManager(this);
    reconfigure();
  }

  private void reconfigure() {
    cpu.detachProcess();
    cpu.configure(hwConfig.getTicks());
    mmu.configure(hwConfig.getTotalRAM());
    io.configure(hwConfig.getMaxHDDSpeed());
    ready.clear();
    waiting.clear();
    Process.resetCounter();
    publishGlobalState();
  }

  public void load(Context ctx, String path) {
    ProcessListLoader loader = new ProcessListLoader(ctx);
    loader.registerEventListener((e, d) ->
      { if (d != null) sourceList = d; });
    loader.execute(path);
  }

  public void togglePause() {
    pause(!paused);
  }

  public void onStartStop() {
    if (running) stop();
    else start();
  }

  private void start() {
    Process process = ready.poll();
    cpu.attachProcess(process);
    if (listener != null)
      listener.onCPUContextChanged(process);
    cpu.start();
    io.start();
    running = true;
    if (listener != null)
      listener.onCLKStateChanged(true, paused);
  }

  public void pause(boolean pause) {
    paused = pause;
    cpu.pause(pause);
    io.pause(pause);
    if (listener != null)
      listener.onCLKStateChanged(running, paused);
  }

  private void stop() {
    running = false;
    paused = false;
    cpu.stop();
    io.stop();
    if (listener != null)
      listener.onCLKStateChanged(running, paused);
  }

  public void setEventListener(EventListener listener) {
    this.listener = listener;
    publishGlobalState();
  }

  private void publishGlobalState() {
    if (listener != null) {
      listener.onHWConfigChanged(hwConfig);
      listener.onCLKStateChanged(running, paused);
      listener.onCPUContextChanged(cpu.getProcess());
      listener.onCPUTick(cpu.getProcess());
      listener.onIOStateChanged(io);
    }
  }

  public HWConfig getHWConfig() {
    return hwConfig;
  }

  public Processor getCPU() {
    return cpu;
  }

  public List<Process> getReadyList() {
    return ready;
  }

  public List<Process> getWaitingList() {
    return waiting;
  }

  public int getMemoryUsage() {
    return mmu.getUsage() >> 10;
  }

  public int getCPUUsage() {
    return cpu.getUsage();
  }

  public boolean isInterruptsManual() {
    return cpu.getIntManual();
  }

  public ArrayList<String> getProcessList() {
    ArrayList<String> list = new ArrayList<>();
    if (sourceList == null) return list;
    for (Process.Info info : sourceList)
      list.add(info.getName());
    return list;
  }

  public void setInterruptsManual(boolean manual) {
    cpu.setIntManual(manual);
  }

  public void createProcess(int id)
    throws AddProcessException {
    createProcess(sourceList.get(id));
  }

  public void createProcess(Process.Info info)
    throws AddProcessException {
    Process process = new Process(info, mmu);
    if (process.getRamUsage() > mmu.getFree())
      throw new AddProcessException("Out of memory");
    MyLog.d(LOG_TAG, "Created new process: " + process);
    process.init(systemRandom);
    ready.add(process);
    if (listener != null)
      listener.onProcessAdded(process);
    if (running && cpu.getProcess() == null)
      onCPUTimer(null);
  }

  public void externalInterrupt() {
    Process interrupt = new Process("Interrupt", systemRandom, mmu);
    MyLog.d(LOG_TAG, "Created new interrupt: " + interrupt);
    interrupt.init(systemRandom);
    ready.add(0, interrupt);
    if (listener != null)
      listener.onProcessAdded(interrupt);
    if (running && cpu.getProcess() == null)
      onCPUTimer(null);
    cpu.forceInterrupt();
  }

  @Override
  public void onCPUTick(Process process) {
    if (listener != null) listener.onCPUTick(process);
    if (process.isFinished()) {
      onCPUTimer(process);
      cpu.resetTimer();
    }
  }

  @Override
  public void onCPUTimer(Process process) {
    if (process != null) {
      if (process.isWaitingForIO()) {
        int id = process.getId();
        io.requestOperation(id);
        waiting.add(process);
        if (listener != null)
          listener.onIOOperation(false, id);
      } else if (process.isFinished()) {
        if (listener != null)
          listener.onProcessFinished(process);
        process.free();
      } else ready.add(process);
    }

    cpu.detachProcess();
    process = ready.poll();
    cpu.attachProcess(process);
    if (listener != null)
      listener.onCPUContextChanged(process);
  }

  @Override
  public void onIOProgressUpdate() {
    if (listener != null) listener.onIOStateChanged(io);
  }

  @Override
  public void onIOOperationFinished(int processID) {
    for (int i = 0; i < waiting.size(); i++) {
      Process process = waiting.get(i);
      if (process.getId() == processID) {
        waiting.remove(process);
        ready.add(process);
        if (cpu.getProcess() == null)
          onCPUTimer(null);
      }
    }

    if (listener != null)
      listener.onIOOperation(true, processID);
  }

  public interface EventListener {
    void onHWConfigChanged(HWConfig config);
    void onCLKStateChanged(boolean running, boolean paused);
    void onProcessAdded(Process process);
    void onProcessFinished(Process process);
    void onCPUTick(Process process);
    void onCPUContextChanged(Process process);
    void onIOStateChanged(IOManager manager);
    void onIOOperation(boolean finished, int processID);
  }

  public class AddProcessException extends IllegalStateException {
    private AddProcessException(String msg) { super(msg); }
  }
}
