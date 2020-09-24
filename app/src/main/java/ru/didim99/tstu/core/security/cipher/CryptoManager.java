package ru.didim99.tstu.core.security.cipher;

import android.content.Context;
import java.util.Arrays;
import ru.didim99.tstu.core.security.cipher.decryptor.ADFGVXDecryptor;
import ru.didim99.tstu.core.security.cipher.decryptor.Decryptor;
import ru.didim99.tstu.core.security.cipher.decryptor.GammaDecryptor;
import ru.didim99.tstu.core.security.cipher.decryptor.RSADecryptor;
import ru.didim99.tstu.core.security.cipher.decryptor.RotaryGridDecryptor;
import ru.didim99.tstu.core.security.cipher.decryptor.VigenereDecryptor;
import ru.didim99.tstu.core.security.cipher.network.TCPServer;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 09.09.20.
 */

public class CryptoManager implements TCPServer.MessageListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_CM";

  public enum Mode { CIPHER, HIDING, SIGNING }
  public enum Cipher { VIGENERE, ROTARY_GRID, GAMMA, ADFGVX, RSA }

  // Reply messages
  private static final String MSG_OK = "OK";
  private static final String MSG_ERR = "ERROR: %s";
  // Command structure
  private static final char CMD_MARKER = '/';
  private static final String CMD_SEPARATOR = " ";
  // Commands
  private static final String CMD_DISCONNECT = "goodbye";
  private static final String CMD_SET_MODE = "setmode";
  private static final String CMD_SET_CIPHER = "setcipher";
  private static final String CMD_CONFIGURE = "configure";
  // Possible mode constants
  private static final int MODE_CIPHER = 1;
  private static final int MODE_HIDING = 2;
  private static final int MODE_SIGNING = 3;
  // Possible ciphers constants
  private static final int CIPHER_VIGENERE = 1;
  private static final int CIPHER_ROTARY_GRID = 2;
  private static final int CIPHER_GAMMA = 3;
  private static final int CIPHER_ADFGVX = 4;
  private static final int CIPHER_RSA = 5;

  private Mode mode;
  private EventListener listener;
  private TCPServer server;
  private Decryptor decryptor;

  public CryptoManager(Context context) {
    server = new TCPServer(context, this);
    applyMode(MODE_CIPHER);
  }

  public void setEventListener(EventListener listener) {
    this.listener = listener;
  }

  public TCPServer getServer() {
    return server;
  }

  @Override
  public String onMessageReceived(String msg) {
    MyLog.v(LOG_TAG, "Message received: " + msg);
    try {
      if (msg.charAt(0) == CMD_MARKER) {
        String[] command = msg.substring(1).split(CMD_SEPARATOR);
        String[] args = command.length == 1 ? null :
          Arrays.copyOfRange(command, 1, command.length);
        String cmd = command[0];
        processCommand(cmd, args);
      } else {
        if (decryptor != null && decryptor.isConfigured()) {
          String message = decryptor.decrypt(msg);
          if (listener != null)
            listener.onDataReceived(msg, message);
        } else throw new IllegalStateException("Decryptor not initialized");
      }
      return MSG_OK;
    } catch (Exception e) {
      return getError(e.getMessage());
    }
  }

  private void processCommand(String cmd, String[] args) {
    switch (cmd) {
      case CMD_DISCONNECT:
        server.disconnect();
        break;
      case CMD_SET_MODE:
        applyMode(Integer.parseInt(args[0]));
        break;
      case CMD_SET_CIPHER:
        if (mode == Mode.CIPHER)
          setCipher(Integer.parseInt(args[0]));
        else throw new IllegalStateException("Incorrect mode: " + mode);
        break;
      case CMD_CONFIGURE:
        if (decryptor != null) decryptor.configure(args);
        else throw new IllegalStateException("Nothing to configure");
        if (listener != null)
          listener.onCipherConfigChanged(decryptor);
        break;
      default:
        throw new IllegalArgumentException("Unknown command: " + cmd);
    }
  }

  private void applyMode(int mode) {
    this.decryptor = null;
    switch (mode) {
      case MODE_CIPHER:
        this.mode = Mode.CIPHER;
        break;
      case MODE_HIDING:
        this.mode = Mode.HIDING;
        break;
      case MODE_SIGNING:
        this.mode = Mode.SIGNING;
        break;
      default:
        throw new IllegalArgumentException("unknown mode: " + mode);
    }
  }

  private void setCipher(int cipherId) {
    Cipher cipher;
    switch (cipherId) {
      case CIPHER_VIGENERE:
        decryptor = new VigenereDecryptor();
        cipher = Cipher.VIGENERE;
        break;
      case CIPHER_ROTARY_GRID:
        decryptor = new RotaryGridDecryptor();
        cipher = Cipher.ROTARY_GRID;
        break;
      case CIPHER_GAMMA:
        decryptor = new GammaDecryptor();
        cipher = Cipher.GAMMA;
        break;
      case CIPHER_ADFGVX:
        decryptor = new ADFGVXDecryptor();
        cipher = Cipher.ADFGVX;
        break;
      case CIPHER_RSA:
        decryptor = new RSADecryptor();
        cipher = Cipher.RSA;
        break;
      default:
        throw new IllegalArgumentException("unknown cipher: " + cipherId);
    }

    if (listener != null)
      listener.onCipherTypeChanged(cipher);
  }

  private String getError(String msg) {
    return String.format(MSG_ERR, msg);
  }

  public interface EventListener {
    void onCipherTypeChanged(Cipher cipher);
    void onCipherConfigChanged(Decryptor decryptor);
    void onDataReceived(String encrypted, String decrypted);
  }
}
