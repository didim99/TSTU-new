package ru.didim99.tstu.core.security.cipher;

import android.content.Context;

/**
 * Created by didim99 on 09.09.20.
 */

public class CryptoManager implements TCPServer.MessageListener {

  private static final char MSG_MARKER = '/';
  private static final String MSG_DISCONNECT = "/goodbye";

  private TCPServer server;
  private EventListener listener;

  public CryptoManager(Context context) {
    server = new TCPServer(context, this);
  }

  public void setEventListener(EventListener listener) {
    this.listener = listener;
  }

  @Override
  public void onMessageReceived(String msg) {

  }

  public TCPServer getServer() {
    return server;
  }

  public interface EventListener {

  }
}
