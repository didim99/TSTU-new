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
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 07.09.20.
 */

public class SimpleCipherActivity extends BaseActivity
  implements CryptoManager.EventListener, TCPServer.EventListener {
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
    btnStartStop.setOnClickListener(v -> startStopServer());
    ivRefreshIP.setOnClickListener(v -> updateMyIP());
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
    manager.getServer().setEventListener(this);
  }

  @Override
  protected void onStop() {
    manager.getServer().setEventListener(null);
    manager.setEventListener(null);
    super.onStop();
  }

  @Override
  public void onServerStateChanged(TCPServer.State state) {
    TCPServer server = manager.getServer();
    boolean running = server.isRunning();
    btnStartStop.setText(running ? R.string.is_network_stop
      : R.string.is_network_start);
    btnStartStop.setEnabled(server.getState() != TCPServer.State.DISABLED);
    ivRefreshIP.setEnabled(!running);
    etPort.setEnabled(!running);

    switch (state) {
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
        InetSocketAddress client = server.getClientAddress();
        tvServerState.setText(getString(R.string.is_state_connected,
          client.getAddress().getHostAddress(), client.getPort()));
        break;
    }
  }

  private void startStopServer() {
    TCPServer server = manager.getServer();
    if (!server.isRunning()) {
      try {
        InputValidator iv = InputValidator.getInstance();
        int port = iv.checkInteger(etPort, TCPServer.MIN_PORT, TCPServer.MAX_PORT,
          R.string.errIS_emptyPort, R.string.errIS_incorrectPort, "server port");
        server.start(port);
      } catch (InputValidator.ValidationException ignored) {}
    } else server.stop();
  }

  private void updateMyIP() {
    InetAddress ip = manager.getServer().getMyIP();
    if (ip != null) tvIP.setText(ip.getHostAddress());
  }
}
