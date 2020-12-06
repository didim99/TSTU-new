package ru.didim99.tstu.ui.os.expanse;

import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.os.expanse.Expanse;
import ru.didim99.tstu.core.os.expanse.GameField;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.ui.UIManager;
import ru.didim99.tstu.ui.view.RangeBar;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 09.04.19.
 */
public class ExpanseActivity extends BaseActivity
  implements Expanse.OnStateChangeListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ExpAct";

  // View-elements
  private PlayerAdapter adapter;
  private GameFieldView fieldView;
  private ProgressBar pbGameState;
  private TextView tvFieldSize;
  private TextView tvFieldSizeX;
  private TextView tvPlayerCount;
  private TextView tvStatus;
  private EditText etW, etH;
  private Spinner spPCount;
  private RangeBar rbDistance;
  private Button btnInit;
  private Button btnStart;
  // View-elements styling
  private int colorNormal;
  private int colorGreen;
  private int colorRed;
  // Workflow
  private Expanse game;
  private int fw, fh;
  private int area;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "ExpanseActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_expanse);

    MyLog.d(LOG_TAG, "View components init...");
    RecyclerView playerList = findViewById(R.id.playerList);
    tvFieldSize = findViewById(R.id.tvFieldSize);
    tvFieldSizeX = findViewById(R.id.tvFieldSizeX);
    tvPlayerCount = findViewById(R.id.tvPlayerCount);
    pbGameState = findViewById(R.id.pbGameState);
    rbDistance = findViewById(R.id.rbDistance);
    fieldView = findViewById(R.id.gameField);
    btnInit = findViewById(R.id.btnInit);
    btnStart = findViewById(R.id.btnStart);
    tvStatus = findViewById(R.id.tvStatus);
    spPCount = findViewById(R.id.spPlayerCount);
    etW = findViewById(R.id.etSizeX);
    etH = findViewById(R.id.etSizeY);
    btnInit.setOnClickListener(v -> configureGame());
    btnStart.setOnClickListener(v -> onStartClicked());
    new ValueWatcher(etW, this::onFieldSizeChanged);
    new ValueWatcher(etH, this::onFieldSizeChanged);
    UIManager uiManager = UIManager.getInstance();
    Resources res = getResources();
    colorNormal = res.getColor(uiManager.resolveAttr(R.attr.clr_blue));
    colorGreen = res.getColor(uiManager.resolveAttr(R.attr.clr_green));
    colorRed = res.getColor(uiManager.resolveAttr(R.attr.clr_red));
    MyLog.d(LOG_TAG, "View components init completed");

    adapter = new PlayerAdapter(this);
    playerList.setLayoutManager(new LinearLayoutManager(this));
    playerList.setAdapter(adapter);

    ArrayAdapter<String> la = new ArrayAdapter<>(
      this, android.R.layout.simple_list_item_1);
    for (int i = Expanse.PLAYERS_MIN; i <= Expanse.PLAYERS_MAX; i++)
      la.add(String.valueOf(i));
    spPCount.setAdapter(la);

    MyLog.d(LOG_TAG, "Connecting to background task...");
    game = (Expanse) getLastCustomNonConfigurationInstance();
    if (game != null) {
      onGameInitialized();
      onGameStateChanged(game.getState());
      MyLog.d(LOG_TAG, "Connected to: " + game);
    } else {
      MyLog.d(LOG_TAG, "No existing background task found");
      game = new Expanse(this);
    }

    GameField field = onGameInitialized();
    etW.setText(String.valueOf(field.getWidth()));
    etH.setText(String.valueOf(field.getHeight()));
    spPCount.setSelection(game.getPlayerCount() - Expanse.PLAYERS_MIN);
    rbDistance.setBounds(Expanse.DISTANCE_MIN, Expanse.DISTANCE_MAX);
    rbDistance.setValue(game.getMinDistance());
    game.setOnStateChangeListener(this);
    onFieldSizeChanged(0, 0);

    MyLog.d(LOG_TAG, "ExpanseActivity created");
  }

  @Override
  protected void onResume() {
    super.onResume();
    game.pause(false);
  }

  @Override
  protected void onPause() {
    game.pause(true);
    super.onPause();
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    if (game != null) game.setOnStateChangeListener(null);
    return game;
  }

  @Override
  public void onGameStep() {
    if (game.getState() != Expanse.State.RUNNING) return;
    int count = game.getField().getProgress();
    double progress = count * 100.0 / area;
    tvStatus.setText(getString(
      R.string.os_expanse_progress, count, area, progress));
    adapter.refreshData(game.getPlayers());
    pbGameState.setProgress(count);
    fieldView.invalidate();
  }

  @Override
  public void onGameStateChanged(int state) {
    MyLog.d(LOG_TAG, "Game state changed: " + state);
    boolean running = state == Expanse.State.RUNNING;
    boolean ready = state == Expanse.State.READY;
    btnStart.setEnabled(running || ready);
    tvFieldSize.setEnabled(!running);
    tvFieldSizeX.setEnabled(!running);
    tvPlayerCount.setEnabled(!running);
    rbDistance.setEnabled(!running);
    spPCount.setEnabled(!running);
    btnInit.setEnabled(!running);
    etW.setEnabled(!running);
    etH.setEnabled(!running);

    int textID = 0;
    switch (state) {
      case Expanse.State.INITIAL:
        textID = R.string.os_expanse_initial;
        break;
      case Expanse.State.READY:
        textID = R.string.os_expanse_ready;
        pbGameState.getProgressDrawable().setColorFilter(
          colorNormal, PorterDuff.Mode.SRC_IN);
        break;
      case Expanse.State.RUNNING:
        textID = R.string.os_expanse_starting;
        pbGameState.getProgressDrawable().setColorFilter(
          colorNormal, PorterDuff.Mode.SRC_IN);
        break;
      case Expanse.State.STOPPED:
        textID = R.string.os_expanse_interrupted;
        pbGameState.getProgressDrawable().setColorFilter(
          colorRed, PorterDuff.Mode.SRC_IN);
        break;
      case Expanse.State.FINISHED:
        textID = R.string.os_expanse_gameOver;
        pbGameState.getProgressDrawable().setColorFilter(
          colorGreen, PorterDuff.Mode.SRC_IN);
        break;
    }

    btnStart.setText(running ? R.string.os_expanse_stop
      : R.string.os_expanse_start);
    tvStatus.setText(textID);
    adapter.refreshData(game.getPlayers());
    fieldView.invalidate();
  }

  private GameField onGameInitialized() {
    GameField field = game.getField();
    fieldView.setSource(field);
    fw = field.getWidth();
    fh = field.getHeight();
    area = field.getArea();
    pbGameState.setMax(area);
    pbGameState.setProgress(game.getState() ==
      Expanse.State.READY ? 0 : field.getProgress());
    return field;
  }

  private void onFieldSizeChanged(int viewId, int value) {
    switch (viewId) {
      case R.id.etSizeX: fw = value; break;
      case R.id.etSizeY: fh = value; break;
    }

    int newMax = Expanse.getMaxDistance(fw, fh);
    MyLog.v(LOG_TAG, "Max distance changed: " + newMax);
    rbDistance.setMaximum(newMax);
  }

  private void onStartClicked() {
    switch (game.getState()) {
      case Expanse.State.READY:   game.start(); break;
      case Expanse.State.RUNNING: game.stop(); break;
    }
  }

  private void configureGame() {
    try {
      MyLog.d(LOG_TAG, "Trying to configure game...");
      InputValidator iv = InputValidator.getInstance();
      int w = iv.checkInteger(etW, Expanse.FIELD_W_MIN, Expanse.FIELD_W_MAX,
        R.string.errExpanse_emptyW, R.string.errExpanse_invalidW, "field width");
      int h = iv.checkInteger(etH, Expanse.FIELD_H_MIN, Expanse.FIELD_H_MAX,
        R.string.errExpanse_emptyH, R.string.errExpanse_invalidH, "field height");
      int players = spPCount.getSelectedItemPosition() + Expanse.PLAYERS_MIN;
      int distance = (int) rbDistance.getValue();

      if (!Expanse.canSpawn(w, h, players)) {
        MyLog.e(LOG_TAG, "Too many players");
        Toast.makeText(this, R.string.errExpanse_tooManyPlayers,
          Toast.LENGTH_LONG).show();
        return;
      }

      game.configure(w, h, players);
      if (!game.spawnPlayers(distance)) {
        Toast.makeText(this, R.string.errExpanse_spawnFailed,
          Toast.LENGTH_LONG).show();
      } else onGameInitialized();
      MyLog.d(LOG_TAG, "Game configuration completed");
    } catch (InputValidator.ValidationException ignored) {}
  }
}
