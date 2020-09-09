package ru.didim99.tstu.ui.security;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.security.datatransfer.DataExchanger;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.ui.utils.SpinnerAdapter;
import ru.didim99.tstu.ui.view.RangeBar;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 16.04.19.
 */
public class TransmitActivity extends BaseActivity
  implements DataExchanger.EventListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ISAct";

  // View elements
  private Spinner spCodeType;
  private RangeBar rbBrokenBits;
  private CheckBox cbUseNoise;
  private RangeBar rbMsgLength;
  private RangeBar rbBrokenBlocks;
  private TextView tvConfig;
  private TextView tvTransmitter;
  private TextView tvReceiver;
  private Button btnStart;
  private Button btnPause;
  // Workflow
  private DataExchanger exchanger;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "TransmitActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_transmission);

    MyLog.d(LOG_TAG, "View components init...");
    spCodeType = findViewById(R.id.spCodeType);
    cbUseNoise = findViewById(R.id.cbUseNoise);
    rbMsgLength = findViewById(R.id.rbMsgLength);
    rbBrokenBlocks = findViewById(R.id.rbBrokenBlocks);
    rbBrokenBits = findViewById(R.id.rbBrokenBits);
    tvConfig = findViewById(R.id.tvConfig);
    tvTransmitter = findViewById(R.id.tvTransmitter);
    tvReceiver = findViewById(R.id.tvReceiver);
    btnStart = findViewById(R.id.btnStart);
    btnPause = findViewById(R.id.btnPause);
    btnStart.setOnClickListener(v -> onStartClicked());
    btnPause.setOnClickListener(v -> exchanger.togglePause());
    cbUseNoise.setOnCheckedChangeListener((v, c) -> onNoiseEnabled(c));
    spCodeType.setOnItemSelectedListener(
      new SpinnerAdapter(this::onCodeTypeChanged));
    rbMsgLength.setBounds(DataExchanger.MIN_ML, DataExchanger.MAX_ML);
    rbMsgLength.setOnValueChangedListener(
      value -> rbBrokenBlocks.setMaximum(value));
    rbBrokenBlocks.setMinimum(DataExchanger.MIN_BB);
    rbBrokenBits.setMinimum(DataExchanger.MIN_BB);
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "Connecting to background task...");
    exchanger = (DataExchanger) getLastCustomNonConfigurationInstance();
    if (exchanger != null) {
      MyLog.d(LOG_TAG, "Connected to: " + exchanger);
    } else {
      MyLog.d(LOG_TAG, "No existing background task found");
      exchanger = new DataExchanger();
    }

    onNoiseEnabled(exchanger.isUseNoise());
    spCodeType.setSelection(exchanger.getCodeType());
    rbMsgLength.setValue(exchanger.getMessageLength());
    rbBrokenBlocks.setMaximum(exchanger.getMessageLength());
    rbBrokenBlocks.setValue(exchanger.getBrokenBlocks());
    rbBrokenBits.setValue(exchanger.getBrokenBits());
    exchanger.setEventListener(this);

    MyLog.d(LOG_TAG, "TransmitActivity created");
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    if (exchanger != null) exchanger.setEventListener(null);
    return exchanger;
  }

  @Override
  protected void onResume() {
    super.onResume();
    exchanger.pause(false);
  }

  @Override
  protected void onPause() {
    exchanger.pause(true);
    super.onPause();
  }

  @Override
  public void OnExchangerStateChanged(int state) {
    MyLog.d(LOG_TAG, "Exchanger state changed: " + state);
    boolean running = state != DataExchanger.State.IDLE;
    boolean paused = state == DataExchanger.State.PAUSED;
    boolean noise = cbUseNoise.isChecked();
    btnStart.setText(running ? R.string.is_dt_stop : R.string.is_dt_start);
    btnPause.setText(paused ? R.string.is_dt_unPause : R.string.is_dt_pause);
    spCodeType.setEnabled(!running);
    cbUseNoise.setEnabled(!running);
    rbMsgLength.setEnabled(!running);
    btnPause.setEnabled(running);
    rbBrokenBlocks.setEnabled(!running && noise);
    rbBrokenBits.setEnabled(!running && noise);
    btnStart.setEnabled(true);
  }

  @Override
  public void onPackageTransferred(String txInfo, String rxInfo) {
    MyLog.v(LOG_TAG, "New package transferred");
    tvTransmitter.setText(txInfo);
    tvReceiver.setText(rxInfo);
  }

  private void onCodeTypeChanged(int type) {
    exchanger.onCodeTypeChanged(type);
    tvConfig.setText(exchanger.getCodeInfo());
    rbBrokenBits.setMaximum(exchanger.getDataBits());
    tvTransmitter.setText(null);
    tvReceiver.setText(null);
  }

  private void onStartClicked() {
    btnStart.setEnabled(false);
    if (exchanger.isRunning()) {
      exchanger.finish();
    } else {
      exchanger.configure(
        (int) rbMsgLength.getValue(),
        (int) rbBrokenBlocks.getValue(),
        (int) rbBrokenBits.getValue(),
        cbUseNoise.isChecked());
      exchanger.start();
    }
  }

  private void onNoiseEnabled(boolean enabled) {
    rbBrokenBlocks.setEnabled(enabled);
    rbBrokenBits.setEnabled(enabled);
  }
}
