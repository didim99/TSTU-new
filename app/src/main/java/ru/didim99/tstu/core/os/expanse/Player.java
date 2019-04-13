package ru.didim99.tstu.core.os.expanse;

import android.os.AsyncTask;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 11.04.19.
 */
public class Player {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Player";
  private static final int STEP_SLEEP = 250;

  // Game logic
  private Random rnd;
  private Position spawn;
  private ArrayList<Position> area;
  private final GameField field;
  private final int id, color;
  private int score, maxStep;
  // multithreading
  private OnStepListener listener;
  private CyclicBarrier stepBarrier;
  private Task task;

  Player(int id, int color, GameField field) {
    this.maxStep = field.getArea() / 20 + 1;
    this.area = new ArrayList<>();
    this.rnd = new Random();
    this.score = 0;
    this.id = id;
    this.color = color;
    this.field = field;
  }

  public int getId() {
    return id;
  }

  public int getColor() {
    return color;
  }

  public int getScore() {
    return score;
  }

  void setOnStepListener(OnStepListener listener) {
    this.listener = listener;
  }

  void spawn(Position pos) {
    spawn = new Position(pos);
    acquire(pos);
  }

  void clearSpawn() {
    spawn = null;
    score = 0;
  }

  int spawnDistance(Position pos) {
    if (spawn == null) return -1;
    return spawn.distance(pos);
  }

  void start(CyclicBarrier stepBarrier) {
    this.stepBarrier = stepBarrier;
    task = new Task(this);
    task.start();
  }

  void pause(boolean pause) {
    if (task != null) task.pause(pause);
  }

  void stop(boolean force) {
    if (task != null) task.stop(force);
  }

  private void step() throws InterruptedException {
    int attempts = Math.min(score, maxStep);
    int flags, mask, i = 0;
    Position pos = null;

    while (attempts > 0) {
      synchronized (field) {
        flags = mask = 0;
        while (flags == 0 && attempts >= 0) {
          if (!field.hasEmptyCells()) return;
          i = score > 1 ? rnd.nextInt(score - 1) : 0;
          pos = new Position(area.get(i));
          flags = field.isBoundary(pos);
          attempts--;
        }

        while (mask == 0 && attempts >= 0) {
          i = rnd.nextInt(GameField.Direction.ANY.length);
          mask = flags & GameField.Direction.ANY[i];
        }

        if (attempts < 0) return;
        pos.offset(GameField.Direction.OFFSET[i]);
        MyLog.d(LOG_TAG, "Player " + id + " acquiring: " + pos);
        acquire(pos);
      }
    }

    MyLog.d(LOG_TAG, "Player " + id + " sleeping...");
    Thread.sleep(STEP_SLEEP);
  }

  private void acquire(Position pos) {
    pos = new Position(pos);
    field.acquire(pos, this);
    area.add(pos);
    score++;
  }

  @Override
  public String toString() {
    if (spawn == null) return "Player " + id + " [NOT SPAWNED]";
    return "Player " + id + " [" + spawn.x + ", " + spawn.y + "]";
  }

  static class Task extends AsyncTask<Void, Integer, Void> {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_PlayerTask";
    private static final int PAUSED = 1000;

    private Player player;
    private boolean running, paused;
    private boolean forceStop;

    private Task(Player player) {
      this.player = player;
    }

    private void start() {
      executeOnExecutor(ThreadPool.EXECUTOR);
      running = true;
      paused = false;
    }

    void pause(boolean pause) {
      paused = pause;
    }

    void stop(boolean force) {
      if (force) player.stepBarrier.reset();
      forceStop = force;
      running = false;
    }

    @Override
    protected Void doInBackground(Void... voids) {
      MyLog.d(LOG_TAG, "Starting: " + player);
      while (running) {
        try {
          if (paused) {
            Thread.sleep(PAUSED);
            if (!running) break;
            continue;
          }

          player.step();
          publishProgress();
          player.stepBarrier.await();
        } catch (InterruptedException e) {
          MyLog.e(LOG_TAG, "Interrupted: " + e);
        } catch (BrokenBarrierException e) {
          MyLog.e(LOG_TAG, "Broken stepBarrier: " + e);
        }
      }

      return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
      player.listener.onStep();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      if (!forceStop) player.listener.onFinish();
    }
  }

  interface OnStepListener {
    void onFinish();
    void onStep();
  }
}
