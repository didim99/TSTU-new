package ru.didim99.tstu.core.os.expanse;

import android.content.Context;
import android.content.res.Resources;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 11.04.19.
 */
public class Expanse implements Player.OnStepListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Expanse";
  // Minimum and maximum values
  public static final int DISTANCE_MIN = 1;
  public static final int DISTANCE_MAX = 5;
  public static final int PLAYERS_MIN = 1;
  public static final int PLAYERS_MAX = 6;
  public static final int FIELD_W_MIN = 2;
  public static final int FIELD_H_MIN = 2;
  public static final int FIELD_W_MAX = 70;
  public static final int FIELD_H_MAX = 45;
  // Internal constants
  private static final int MAX_RETRY = 100;
  private static final int DPC = 2;
  private static final int DFW = 15;
  private static final int DFH = 10;
  private static int[] COLORS = {
    R.color.player0, R.color.player1,
    R.color.player2, R.color.player3,
    R.color.player4, R.color.player5 };

  public static final class State {
    public static final int INITIAL   = 0;
    public static final int READY     = 1;
    public static final int RUNNING   = 2;
    public static final int STOPPED   = 3;
    public static final int FINISHED  = 4;
  }

  private static boolean initCompleted;
  private OnStateChangeListener listener;
  private ArrayList<Player> players;
  private GameField field;
  private int minDistance;
  private int waiting;
  private int state;

  public Expanse(Context context) {
    if (!initCompleted) {
      Resources res = context.getResources();
      for (int i = 0; i < COLORS.length; i++)
        COLORS[i] = res.getColor(COLORS[i]);
      initCompleted = true;
    }

    minDistance = DISTANCE_MAX;
    players = new ArrayList<>(DPC);
    configure(DFW, DFH, DPC);
    applyState(State.INITIAL);
  }

  @Override
  public void onStep() {
    if (listener != null) listener.onGameStep();
  }

  @Override
  public void onFinish() {
    if (--waiting == 0)
      applyState(State.FINISHED);
  }

  public int getState() {
    return state;
  }

  public GameField getField() {
    return field;
  }

  public ArrayList<Player> getPlayers() {
    return players;
  }

  public int getPlayerCount() {
    return players.size();
  }

  public int getMinDistance() {
    return minDistance;
  }

  public void setOnStateChangeListener(OnStateChangeListener listener) {
    this.listener = listener;
    publishState();
  }

  public void configure(int w, int h, int playersCount) {
    if (playersCount < PLAYERS_MIN || playersCount > PLAYERS_MAX)
      throw new IllegalArgumentException("Invalid players count");
    MyLog.d(LOG_TAG, "Configuration updated: "
      + "w: " + w + ", h: " + h + ", players: " + playersCount);

    players.clear();
    field = new GameField(w, h);
    for (int i = 0; i < playersCount; i++) {
      Player player = new Player(i+1, COLORS[i], field);
      player.setOnStepListener(this);
      players.add(player);
    }
  }

  public boolean spawnPlayers(int minDistance) {
    MyLog.d(LOG_TAG, "Spawn players: " + players.size()
     + ", distance: " + minDistance);
    this.minDistance = minDistance;
    boolean done = false;
    int retry = 0;

    while (!done) {
      done = trySpawnPlayers();
      if (!done) {
        MyLog.w(LOG_TAG, "Spawn failed");
        if (retry++ > MAX_RETRY * 2) return false;
      }
    }

    MyLog.d(LOG_TAG, "All players spawned");
    applyState(State.READY);
    return true;
  }

  private boolean trySpawnPlayers() {
    Position pos = new Position();
    int w = field.getWidth();
    int h = field.getHeight();
    Random r = new Random();

    for (Player player : players)
      player.clearSpawn();
    field.clear();

    int retry = 0;
    for (Player player : players) {
      boolean spawned = false;
      while (!spawned) {
        pos.set(r.nextInt(w), r.nextInt(h));
        spawned = true;

        for (Player player2 : players) {
          int distance = player2.spawnDistance(pos);
          if (distance < 0) continue;
          if (distance < minDistance) {
            if (retry++ > MAX_RETRY)
              return false;
            spawned = false;
            break;
          }
        }
      }

      player.spawn(pos);
      MyLog.d(LOG_TAG, "Player spawned: " + player);
    }

    return true;
  }

  public void start() {
    applyState(State.RUNNING);
    waiting = getPlayerCount();
    CyclicBarrier barrier = new CyclicBarrier(waiting, this::step);
    for (Player player : players) player.start(barrier);
    MyLog.d(LOG_TAG, "Game started");
  }

  public void pause(boolean pause) {
    if (state != State.RUNNING) return;
    for (Player player : players) player.pause(pause);
    MyLog.d(LOG_TAG, "Game paused: " + pause);
  }

  public void stop() {
    applyState(State.STOPPED);
    for (Player player : players) player.stop(true);
    MyLog.d(LOG_TAG, "Game stopped");
  }

  private void step() {
    if (state != State.RUNNING) return;
    MyLog.d(LOG_TAG, "Game step");
    if (!field.hasEmptyCells()) {
      MyLog.d(LOG_TAG, "Game over");
      for (Player player : players) player.stop(false);
    }
  }

  private void applyState(int state) {
    this.state = state;
    publishState();
  }

  private void publishState() {
    if (listener != null) listener.onGameStateChanged(state);
  }

  @Override
  public String toString() {
    return "Expanse ("
      + "state: " + state + ", field: " + field
      + ", players: " + getPlayerCount() + ")";
  }

  public static int getMaxDistance(int w, int h) {
    int max = Math.min(w, h) / 2 - 1;
    if (max < 1) max = 1;
    return max;
  }

  public static boolean canSpawn(int w, int h, int players) {
    return players * 2 <= w * h;
  }

  public interface OnStateChangeListener {
    void onGameStateChanged(int state);
    void onGameStep();
  }
}
