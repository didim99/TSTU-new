package ru.didim99.tstu.core.os.scheduler;

import android.os.AsyncTask;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 25.04.19.
 */
public class IOManager {
  public static final int MAX_PROGRESS = 200;
  private static final int SPEED_STEP = 10;
  private static final int MIN_SECTOR = 0x00010000;
  private static final int MAX_SECTOR = 0x7FFF0000;
  private static final int IO_READ  = 0;
  private static final int IO_WRITE = 1;

  private Random random;
  private StateListener listener;
  private Executor executor;
  private int maxSpeed, currSpeed;
  private int speedStep;
  private Queue<IOOperation> queue;
  private IOOperation currOperation;
  private int bytesProcessed;
  private int currSector;

  IOManager(StateListener listener) {
    this.queue = new LinkedList<>();
    this.random = new Random();
    this.listener = listener;
  }

  void configure(int speed) {
    currOperation = null;
    maxSpeed = speed;
  }

  public int getUsage() {
    return currSpeed;
  }

  public int getQueueLength() {
    return queue.size();
  }

  public int getCurrSector() {
    return currSector;
  }

  public int getBytesProcessed() {
    return bytesProcessed;
  }

  public int getProgress() {
    if (currOperation == null) return 0;
    return Utils.bound(bytesProcessed * MAX_PROGRESS
      / currOperation.size, MAX_PROGRESS);
  }

  public double getProgressPercent() {
    if (currOperation == null) return 0;
    return Utils.bound(bytesProcessed * 100.0
      / currOperation.size, 100);
  }

  public boolean isIdle() {
    return currOperation == null;
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

  void requestOperation(int processID) {
    int start = Utils.randInRange(random, MIN_SECTOR, MAX_SECTOR);
    int type = random.nextBoolean() ? IO_READ : IO_WRITE;
    int size = Utils.randInRange(random, maxSpeed << 10, maxSpeed << 14);
    IOOperation next = new IOOperation(processID, start, type, size);
    if (currOperation != null) queue.add(next);
    else currOperation = next;
    pause(false);
  }

  private void operate() {
    if (speedStep-- == 0) {
      speedStep = SPEED_STEP;
      int maxDelta = currOperation.type == IO_READ
        ? maxSpeed / 2 : maxSpeed / 5;
      currSpeed += Utils.randInRange(random, -maxDelta, maxDelta);
      currSpeed = Utils.bound(currSpeed, maxSpeed);
    }

    bytesProcessed += currSpeed << 10;
    currSector += currSpeed;
  }

  private void publishState() {
    listener.onIOProgressUpdate();
    if (bytesProcessed >= currOperation.size) {
      listener.onIOOperationFinished(currOperation.processID);
      currOperation = queue.poll();
      speedStep = SPEED_STEP;
      bytesProcessed = 0;
      currSector = 0;
      currSpeed = 0;

      if (currOperation != null) {
        currSector = currOperation.startSector;
      } else pause(true);
      listener.onIOProgressUpdate();
    }
  }

  static class IOOperation {
    private int processID;
    private int startSector;
    private int type, size;

    IOOperation(int processID, int startSector, int type, int size) {
      this.processID = processID;
      this.startSector = startSector;
      this.type = type;
      this.size = size;
    }

    public boolean completed() {
      return size <= 0;
    }
  }

  private static class Executor extends AsyncTask<Void, Void, Void> {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_IOTask";
    private static final long PAUSED = 1000;
    private static final long TICK = 50;

    private IOManager manager;
    private boolean running, paused;

    private Executor(IOManager manager) {
      this.manager = manager;
    }

    private void start() {
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

    @Override
    protected Void doInBackground(Void... voids) {
      while (running) {
        try {
          if (paused || manager.currOperation == null) {
            Thread.sleep(PAUSED);
            if (!running) break;
            continue;
          }

          manager.operate();
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
      manager.publishState();
    }
  }

  interface StateListener {
    void onIOOperationFinished(int processID);
    void onIOProgressUpdate();
  }
}
