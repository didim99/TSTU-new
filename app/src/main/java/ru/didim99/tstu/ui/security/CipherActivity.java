package ru.didim99.tstu.ui.security;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.security.cipher.CryptoManager;
import ru.didim99.tstu.core.security.cipher.decryptor.Decryptor;
import ru.didim99.tstu.core.security.cipher.network.TCPServer;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 07.09.20.
 */

public class CipherActivity extends BaseActivity
  implements CryptoManager.EventListener, TCPServer.EventListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_SCAct";

  // View elements
  private EditText etPort;
  private Button btnStartStop;
  private ImageView ivRefreshIP;
  private TextView tvSectionCipher, tvCipher;
  private TextView tvSectionDecrypted;
  private TextView tvIP, tvServerState;
  private TextView tvCipherName, tvCipherConfig;
  private TextView tvEnc, tvDec;
  private WebView wvEnc;
  // Workflow
  private CryptoManager manager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "CipherActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_cipher);

    MyLog.d(LOG_TAG, "View components init...");
    tvSectionCipher = findViewById(R.id.tvSectionCipher);
    tvCipher = findViewById(R.id.tvCipher);
    tvSectionDecrypted = findViewById(R.id.tvSectionDecrypted);
    btnStartStop = findViewById(R.id.btnStartServer);
    ivRefreshIP = findViewById(R.id.ivRefreshIP);
    tvIP = findViewById(R.id.tvIPAddress);
    etPort = findViewById(R.id.etPort);
    tvServerState = findViewById(R.id.tvServerState);
    tvCipherName = findViewById(R.id.tvCipherName);
    tvCipherConfig = findViewById(R.id.tvCipherConfig);
    tvEnc = findViewById(R.id.tvEncrypted);
    wvEnc = findViewById(R.id.wvEncrypted);
    tvDec = findViewById(R.id.tvDecrypted);
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "Connecting CryptoManager...");
    manager = (CryptoManager) getLastCustomNonConfigurationInstance();
    if (manager != null) {
      MyLog.d(LOG_TAG, "Connected to: " + manager);
      onModeChanged(manager.getMode());
      if (manager.getMode() == CryptoManager.Mode.CIPHER) {
        onCipherTypeChanged(manager.getCipherType());
        onCipherConfigChanged(manager.getDecryptor());
      }
    } else {
      MyLog.d(LOG_TAG, "No existing CryptoManager found");
      manager = new CryptoManager(getApplicationContext());
      onModeChanged(manager.getMode());
    }

    etPort.setText(String.valueOf(TCPServer.DEFAULT_PORT));
    btnStartStop.setOnClickListener(v -> startStopServer());
    ivRefreshIP.setOnClickListener(v -> updateMyIP());
    updateMyIP();

    MyLog.d(LOG_TAG, "CipherActivity created");
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
    uiLocked = running;

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

    uiClear();
  }

  @Override
  public void onServerError(Throwable cause) {
    Toast.makeText(this, getString(R.string.errIS_anyNetwork, cause),
      Toast.LENGTH_LONG).show();
  }

  @Override
  public void onModeChanged(CryptoManager.Mode mode) {
    int titleId = 0;

    uiClear();
    switch (mode) {
      case CIPHER:
        titleId = R.string.is_simpleCipher;
        tvSectionCipher.setText(R.string.is_cipherSettings);
        tvSectionDecrypted.setText(R.string.is_decryptedMessage);
        tvCipher.setText(R.string.is_cipherName);
        wvEnc.setVisibility(View.GONE);
        tvEnc.setVisibility(View.VISIBLE);
        break;
      case HIDING:
        titleId = R.string.is_steganography;
        tvSectionCipher.setText(R.string.is_hidingSettings);
        tvSectionDecrypted.setText(R.string.is_detectedMessage);
        tvCipher.setText(R.string.is_methodName);
        tvCipherName.setText(R.string.is_hiding_bgColor);
        wvEnc.setVisibility(View.VISIBLE);
        tvEnc.setVisibility(View.GONE);
        break;
      case SIGNING:
        titleId = R.string.is_signing;
        break;
    }

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) actionBar.setTitle(titleId);
  }

  @Override
  public void onCipherTypeChanged(CryptoManager.Cipher cipher) {
    if (cipher == null) return;
    int cipherId = 0;
    switch (cipher) {
      case VIGENERE:    cipherId = R.string.is_cipher_vigenere; break;
      case ROTARY_GRID: cipherId = R.string.is_cipher_grid; break;
      case GAMMA:       cipherId = R.string.is_cipher_gamma; break;
      case ADFGVX:      cipherId = R.string.is_cipher_adfgvx; break;
      case RSA:         cipherId = R.string.is_cipher_rsa; break;
    }
    tvCipherName.setText(cipherId);
  }

  @Override
  public void onCipherConfigChanged(Decryptor decryptor) {
    if (decryptor == null) return;
    tvCipherConfig.setText(decryptor.getDescription(this));
  }

  @Override
  public void onDataReceived(String encrypted, String decrypted) {
    switch (manager.getMode()) {
      case CIPHER:
        tvEnc.setText(encrypted);
        break;
      case HIDING:
        wvEnc.loadData(encrypted, "text/html", "UTF-8");
        break;
    }

    tvDec.setText(decrypted);
  }

  private void startStopServer() {
    TCPServer server = manager.getServer();
    if (!server.isRunning()) {
      try {
        MyLog.d(LOG_TAG, "UI Action: server start");
        InputValidator iv = InputValidator.getInstance();
        int port = iv.checkInteger(etPort, TCPServer.MIN_PORT, TCPServer.MAX_PORT,
          R.string.errIS_emptyPort, R.string.errIS_incorrectPort, "server port");
        server.start(port);
      } catch (InputValidator.ValidationException ignored) {}
    } else {
      MyLog.d(LOG_TAG, "UI Action: server stop");
      server.stop();
    }
  }

  private void updateMyIP() {
    InetAddress ip = manager.getServer().getMyIP();
    if (ip != null) tvIP.setText(ip.getHostAddress());
  }

  private void uiClear() {
    if (manager.getMode() != CryptoManager.Mode.HIDING)
      wvEnc.setVisibility(View.GONE);
    if (manager.getMode() == CryptoManager.Mode.CIPHER)
      tvCipherName.setText(null);
    tvCipherConfig.setText(null);
    tvEnc.setText(null);
    tvDec.setText(null);
    wvEnc.clearView();
  }
}
