package ru.didim99.tstu.ui.security;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.security.cipher.CryptoManager;
import ru.didim99.tstu.core.security.cipher.TCPServer;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 07.09.20.
 */

public class SimpleCipherActivity extends BaseActivity
  implements CryptoManager.EventListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_SCAct";

  // View elements
  private EditText etPort;
  private Button btnStartStop;
  private ImageView ivRefreshIP;
  private TextView tvIP, tvServerState;
  private TextView tvCipherName, tvCipherConfig;
  private TextView tvEnc, tvDec;
  // Workflow
  private CryptoManager manager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "SimpleCipherActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_simple_cipher);

    MyLog.d(LOG_TAG, "View components init...");
    btnStartStop = findViewById(R.id.btnStartServer);
    ivRefreshIP = findViewById(R.id.ivRefreshIP);
    tvIP = findViewById(R.id.tvIPAddress);
    etPort = findViewById(R.id.etPort);
    tvServerState = findViewById(R.id.tvServerState);
    tvCipherName = findViewById(R.id.tvCipherName);
    tvCipherConfig = findViewById(R.id.tvCipherConfig);
    tvEnc = findViewById(R.id.tvEncrypted);
    tvDec = findViewById(R.id.tvDecrypted);
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "Connecting CryptoManager...");
    manager = (CryptoManager) getLastCustomNonConfigurationInstance();
    if (manager != null) {
      MyLog.d(LOG_TAG, "Connected to: " + manager);
    } else {
      MyLog.d(LOG_TAG, "No existing CryptoManager found");
      manager = new CryptoManager(getApplicationContext());
    }

    etPort.setText(String.valueOf(TCPServer.DEFAULT_PORT));
    ivRefreshIP.setOnClickListener(v -> updateMyIP());
    onServerStateChanged();
    updateMyIP();

    MyLog.d(LOG_TAG, "SimpleCipherActivity created");
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    return manager;
  }

  @Override
  protected void onStart() {
    super.onStart();
    manager.setEventListener(this);
  }

  @Override
  protected void onStop() {
    manager.setEventListener(null);
    super.onStop();
  }

  private void onServerStateChanged() {
    boolean running = manager.getServer().isRunning();
    btnStartStop.setText(running ? R.string.is_network_stop
      : R.string.is_network_start);
    ivRefreshIP.setEnabled(!running);
    etPort.setEnabled(!running);

    switch (manager.getServer().getState()) {
      case DISABLED:
        tvServerState.setText(R.string.is_state_disabled);
        break;
      case STOPPED:
        tvServerState.setText(R.string.is_state_stopped);
        break;
      case WAITING:
        tvServerState.setText(R.string.is_state_waiting);
        break;
      case CONNECTED:
        InetSocketAddress client = manager.getServer().getClientAddress();
        tvServerState.setText(getString(R.string.is_state_connected,
          client.getAddress().getHostAddress(), client.getPort()));
        break;
    }
  }

  private void updateMyIP() {
    InetAddress ip = manager.getServer().getMyIP();
    if (ip != null) tvIP.setText(ip.getHostAddress());
  }
}
