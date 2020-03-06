package ru.didim99.tstu.core.itheory.encryption;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.itheory.encryption.rsa.RSAKey;
import ru.didim99.tstu.core.itheory.encryption.rsa.RSAProcessor;

/**
 * Created by didim99 on 05.03.20.
 */
public class CryptoManager {
  static final String EXT_SEP = ".";
  public static final String EXT_TXT = ".txt";
  public static final String EXT_ENC = ".crypt";
  public static final String EXT_KEY = ".key";

  // Application level
  private Context context;
  private EventListener listener;
  // Config and sate
  private String filename;
  private String message;
  private byte[] encryptedData;
  private String decryptedData;
  private RSAKey key;

  public CryptoManager(Context context) {
    this.context = context;
  }

  public void setEventListener(EventListener listener) {
    this.listener = listener;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  void setEncryptedData(byte[] encryptedData) {
    this.encryptedData = encryptedData;
  }

  public void setKey(RSAKey key) {
    this.key = key;
  }

  public String getFilename() {
    return filename;
  }

  public String getMessage() {
    return message;
  }

  byte[] getEncryptedData() {
    return encryptedData;
  }

  public String getEncryptedDataStr() {
    if (encryptedData == null) return null;
    StringBuilder sb = new StringBuilder();
    for (byte b : encryptedData)
      sb.append(String.format("%02x ", b));
    return sb.toString().trim();
  }

  public String getDecryptedData() {
    return decryptedData;
  }

  public RSAKey getKey() {
    return key;
  }

  public void loadFile(String path) {
    if (!path.endsWith(EXT_KEY)) {
      filename = new File(path).getName();
      filename = filename.substring(0,
        filename.lastIndexOf(EXT_SEP));
    }

    message = null;
    encryptedData = null;
    decryptedData = null;
    startTask(new Action.Builder(Action.Type.LOAD_FILE)
      .setPath(path).build());
  }

  public void saveToFile(String path) {
    startTask(new Action.Builder(Action.Type.SAVE_FILE)
      .setPath(path).build());
  }

  public void generateKey(int length) {
    startTask(new Action.Builder(Action.Type.GEN_KEY)
      .setKeyLength(length).build());
  }

  public void start() {
    startTask(new Action.Builder(
      Action.Type.PROCESS).build());
  }

  void process() throws IOException {
    RSAProcessor processor = new RSAProcessor(key);
    if (message != null && !message.isEmpty())
      encryptedData = processor.encrypt(message);
    if (encryptedData != null)
      decryptedData = processor.decrypt(encryptedData);
  }

  private void startTask(Action action) {
    CryptoTask task = new CryptoTask(context, this);
    task.registerEventListener(this::onTaskEvent);
    task.execute(action);
  }

  private void onTaskEvent(CallbackTask.Event event, Action action) {
    if (listener == null) return;
    if (event == CallbackTask.Event.START) {
      listener.onOperationStateChanged(true);
    } else {
      listener.onOperationStateChanged(false);
      switch (action.getType()) {
        case GEN_KEY:
          listener.onKeyChanged(key);
          break;
        case LOAD_FILE:
          if (action.getPath().endsWith(EXT_KEY)) {
            if (key != null) listener.onKeyChanged(key);
            break;
          }
        case PROCESS:
          listener.onDataChanged();
          break;
      }
    }
  }

  public interface EventListener {
    void onOperationStateChanged(boolean state);
    void onKeyChanged(RSAKey key);
    void onDataChanged();
  }
}
