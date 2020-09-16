package ru.didim99.tstu.core.security.cipher;

import android.content.Context;

/**
 * Created by didim99 on 09.09.20.
 */

public class CryptoManager {
  private TCPServer server;
  private EventListener listener;

  public CryptoManager(Context context) {
    server = new TCPServer(context);
  }

  public void setEventListener(EventListener listener) {
    this.listener = listener;
  }

  public TCPServer getServer() {
    return server;
  }

  public interface EventListener {

  }
}
