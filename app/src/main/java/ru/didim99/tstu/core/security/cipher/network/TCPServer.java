package ru.didim99.tstu.core.security.cipher.network;

import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 09.09.20.
 */

public class TCPServer implements TCPServerTask.NetworkEventListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_server";

  public enum State { DISABLED, STOPPED, WAITING, CONNECTED }
  public static final int DEFAULT_PORT = 9853;
  public static final int MIN_PORT = 1000;
  public static final int MAX_PORT = 65535;

  private State state;
  private EventListener eventListener;
  private MessageListener messageListener;
  private InetSocketAddress clientAddress;
  private WifiManager wifiManager;
  private TCPReplySender replySender;
  private TCPServerTask task;

  public TCPServer(Context context, MessageListener messageListener) {
    this.messageListener = messageListener;
    wifiManager = (WifiManager) context
      .getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    applyState(State.STOPPED);
  }

  public void setEventListener(EventListener eventListener) {
    this.eventListener = eventListener;
    applyState(state);
  }

  @Override
  public void onStarted() {
    applyState(State.WAITING);
  }

  @Override
  public void onConnected(Socket client) throws IOException {
    this.clientAddress = new InetSocketAddress(
      client.getInetAddress(), client.getPort());
    replySender = new TCPReplySender(client);
    applyState(State.CONNECTED);
  }

  @Override
  public void onDisconnected() throws IOException {
    this.replySender.disconnect();
    this.replySender = null;
    this.clientAddress = null;
    applyState(task.isRunning() ?
      State.WAITING : State.STOPPED);
  }

  @Override
  public void onMessage(String msg) {
    String reply = messageListener.onMessageReceived(msg);
    if (reply != null) replySender.send(reply);
  }

  @Override
  public void onError(Throwable cause) {
    if (eventListener != null)
      eventListener.onServerError(cause);
    applyState(State.STOPPED);
  }

  public State getState() {
    return state;
  }

  public InetSocketAddress getClientAddress() {
    return clientAddress;
  }

  private WifiInfo checkWifiState() {
    WifiInfo info = wifiManager.getConnectionInfo();
    if (info.getSupplicantState() != SupplicantState.COMPLETED) {
      MyLog.d(LOG_TAG, "No any Wi-Fi network connected");
      applyState(State.DISABLED);
      return null;
    } else return info;
  }

  public InetAddress getMyIP() {
    WifiInfo info = checkWifiState();
    if (info == null) return null;

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
    applyState(State.STOPPED);
    task.stop();
  }

  public void disconnect() {
    task.disconnect();
  }

  private void applyState(State state) {
    if (state != this.state)
      MyLog.d(LOG_TAG, "New state: " + state);
    this.state = state;
    if (eventListener != null)
      eventListener.onServerStateChanged(state);
  }

  public interface EventListener {
    void onServerStateChanged(State state);
    void onServerError(Throwable cause);
  }

  public interface MessageListener {
    String onMessageReceived(String msg);
  }
}
