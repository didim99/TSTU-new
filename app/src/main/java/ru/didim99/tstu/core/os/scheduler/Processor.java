package ru.didim99.tstu.core.os.scheduler;

import android.os.AsyncTask;
import java.util.Arrays;
import java.util.Random;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 23.04.19.
 */
public class Processor {
  static final int REG_COUNT = 10;
  public static final int MIN_USAGE = 0;
  public static final int MAX_USAGE = 100;
  private static final int DL_MIN = -20;
  private static final int DL_MAX = 20;
  private static final int RTDSC = 0;
  private static final int FLAGS = 1;
  private static final int EAX_STEP = 0x4000;
  private static final int EBX_STEP = 0x8000;
  private static final int ECX_STEP = 0x4;
  private static final int EDX_STEP = 0x40;
  private static final int ESP_STEP = 0x40;
  static final int EAX = 2;
  static final int EBX = 3;
  static final int ECX = 4;
  static final int EDX = 5;
  static final int ESP = 6;
  static final int EBP = 7;
  static final int CS = 8;
  static final int DS = 9;

  private Random random;
  private Executor executor;
  private TickListener listener;
  private boolean intManual;
  private boolean intRequested;
  private long ticksPerStep;
  private Process process;
  private int[] registers;
  private int load;

  Processor(TickListener listener) {
    random = new Random();
    registers = new int[REG_COUNT];
    this.listener = listener;
  }

  void configure(int ticks) {
    ticksPerStep = ticks / Executor.STEPS;
    Arrays.fill(registers, 0);
    load = 0;
  }

  void setIntManual(boolean manual) {
    this.intManual = manual;
  }

  void start() {
    executor = new Executor(this);
    executor.start();
  }

  void pause(boolean pause) {
    if (executor != null)
      executor.pause(pause);
  }

  void stop() {
    if (executor != null)
      executor.stop();
  }

  int getUsage() {
    return load;
  }

  Process getProcess() {
    return process;
  }

  boolean getIntManual() {
    return intManual;
  }

  void attachProcess(Process process) {
    if (process == null) return;
    System.arraycopy(process.registers(), EAX,
      registers, EAX, REG_COUNT - EAX);
    process.onAttachedToCPU(random);
    this.process = process;
  }

  void detachProcess() {
    if (process == null) return;
    System.arraycopy(registers, EAX, process.registers(),
      EAX, REG_COUNT - EAX);
    process = null;
  }

  void resetTimer() {
    if (executor != null)
      executor.resetTimer();
  }

  void forceInterrupt() {
    if (intManual) requestInterrupt();
    else executor.timerSteps = 0;
  }

  public void requestInterrupt() {
    intRequested = true;
  }

  public String getContext() {
    StringBuilder sb = new StringBuilder();
    for (int reg : registers)
      sb.append(String.format("0x%08X\n", reg));
    return sb.toString().trim();
  }

  private void doTick() {
    registers[RTDSC] += ticksPerStep;
    registers[FLAGS] ^= 1 << random.nextInt(Integer.SIZE);
    registers[EAX] += Utils.randInRange(random, -EAX_STEP, EAX_STEP);
    registers[EBX] += Utils.randInRange(random, -EBX_STEP, EBX_STEP);
    registers[EBX] += Utils.randInRange(random, -ECX_STEP, ECX_STEP);
    registers[EBX] += Utils.randInRange(random, -EDX_STEP, EDX_STEP);
    load += Utils.randInRange(random, DL_MIN, DL_MAX);
    load = Utils.bound(load, 100);

    if (random.nextInt(3) == 0) {
      registers[EBP] = registers[ESP];
      registers[ESP] += random.nextInt(ESP_STEP);
    } else {
      registers[ESP] += Utils.randInRange(random, -ECX_STEP, ECX_STEP);
      if (registers[ESP] >= registers[EBP])
        registers[ESP] -= ESP_STEP << 1;
    }

    process.doTick(random, ticksPerStep);
  }

  private static class Executor extends AsyncTask<Void, Void, Void> {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_CPUTask";
    private static final long PAUSED = 1000;
    private static final long TICK = 200;
    private static final int STEPS = 10;

    private Processor processor;
    private boolean running, paused;
    private int timerSteps;

    private Executor(Processor processor) {
      this.processor = processor;
    }

    private void start() {
      resetTimer();
      executeOnExecutor(THREAD_POOL_EXECUTOR);
      running = true;
      paused = false;
    }

    void pause(boolean pause) {
      paused = pause;
    }

    void stop() {
      running = false;
    }

    private void resetTimer() {
      timerSteps = STEPS;
    }

    @Override
    protected Void doInBackground(Void... voids) {
      while (running) {
        try {
          if (paused || processor.process == null) {
            Thread.sleep(PAUSED);
            if (!running) break;
            continue;
          }

          processor.doTick();
          publishProgress();
          Thread.sleep(TICK);
        } catch (InterruptedException e) {
          MyLog.e(LOG_TAG, "Interrupted: " + e);
        }
      }

      return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
      processor.listener.onCPUTick(processor.process);
      if (processor.process == null) return;
      boolean needSwitch = false;
      if (!processor.intManual) {
        if (processor.process.getId() >= 0)
          needSwitch = timerSteps-- == 0;
      } else if (processor.intRequested) {
        processor.intRequested = false;
        needSwitch = true;
      }

      if (needSwitch) {
        processor.listener.onCPUTimer(processor.process);
        resetTimer();
      }
    }
  }

  interface TickListener {
    void onCPUTick(Process process);
    void onCPUTimer(Process process);
  }
}
