package ru.didim99.tstu.core.security.cipher;

import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 09.09.20.
 */

public class TCPServer implements TCPServerTask.NetworkEventListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_server";

  private static final char MSG_MARKER = '/';
  private static final String MSG_DISCONNECT = "/goodbye";

  public enum State { DISABLED, STOPPED, WAITING, CONNECTED }
  public static final int DEFAULT_PORT = 9853;
  public static final int MIN_PORT = 1000;
  public static final int MAX_PORT = 65535;

  private State state;
  private EventListener eventListener;
  private InetSocketAddress clientAddress;
  private WifiManager wifiManager;
  private TCPServerTask task;

  TCPServer(Context context) {
    wifiManager = (WifiManager) context
      .getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    applyState(State.STOPPED);
  }

  public void setEventListener(EventListener eventListener) {
    this.eventListener = eventListener;
    applyState(state);
  }

  @Override
  public void onConnected(InetAddress inetAddress, int port) {
    this.clientAddress = new InetSocketAddress(inetAddress, port);
    applyState(State.CONNECTED);
  }

  @Override
  public void onDisconnected() {
    this.clientAddress = null;
    applyState(State.WAITING);
  }

  @Override
  public void onMessage(String msg) {
    /*if (msg.charAt(0) == MSG_MARKER)
      onCommandReceived(msg);
    else*/
  }

  public State getState() {
    return state;
  }

  public InetSocketAddress getClientAddress() {
    return clientAddress;
  }

  public InetAddress getMyIP() {
    WifiInfo info = wifiManager.getConnectionInfo();
    if (info.getSupplicantState() != SupplicantState.COMPLETED) {
      MyLog.d(LOG_TAG, "No any Wi-Fi network connected");
      applyState(State.DISABLED);
      return null;
    }

    try {
      int intIP = info.getIpAddress();
      if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN))
        intIP = Integer.reverseBytes(intIP);
      InetAddress ip = InetAddress.getByAddress(
        BigInteger.valueOf(intIP).toByteArray());
      MyLog.d(LOG_TAG, "Wi-Fi connected. IP: " + ip);
      return ip;
    } catch (UnknownHostException e) {
      MyLog.e(LOG_TAG, "Unable to get local IP address");
      return null;
    }
  }

  public boolean isRunning() {
    switch (state) {
      case WAITING:
      case CONNECTED:
        return true;
      default:
        return false;
    }
  }

  public void start(int port) {
    task = new TCPServerTask(port, this);
    task.start();
  }

  public void stop() {
    applyState(State.DISABLED);
    task.stop();
  }

  private void applyState(State state) {
    this.state = state;
    if (eventListener != null)
      eventListener.onServerStateChanged(state);
  }

  public interface EventListener {
    void onServerStateChanged(State state);
  }

  interface MessageListener {
    void onMessageReceived(String msg);
  }
}
