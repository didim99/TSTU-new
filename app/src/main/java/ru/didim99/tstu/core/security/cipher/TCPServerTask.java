package ru.didim99.tstu.core.security.cipher;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 16.09.20.
 */

class TCPServerTask extends AsyncTask<Void, TCPServerTask.Event, Void> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_serverTask";
  private static final int TIMEOUT = 5000;

  public enum Event { STARTED, CONNECTED, DISCONNECTED, MESSAGE }

  private int port;
  private boolean isRunning;
  private NetworkEventListener listener;
  private String messageBuffer;
  private Socket client;

  TCPServerTask(int port, NetworkEventListener listener) {
    this.isRunning = false;
    this.listener = listener;
    this.port = port;
  }

  @Override
  protected Void doInBackground(Void... voids) {
    MyLog.d(LOG_TAG, "Initialization...");
    ServerSocket socket;

    try {
      socket = new ServerSocket(port);
      socket.setSoTimeout(TIMEOUT);
      MyLog.d(LOG_TAG, "Socket created successfully");
    } catch (IOException e) {
      MyLog.e(LOG_TAG, "Socket not created: " + e);
      isRunning = false;
      return null;
    }

    MyLog.d(LOG_TAG, "Listening on port: " + port);
    publishProgress(Event.STARTED);
    while (isRunning) {
      try {
        client = socket.accept();
        BufferedReader reader = new BufferedReader(
          new InputStreamReader(client.getInputStream()));
        BufferedWriter writer = new BufferedWriter(
          new OutputStreamWriter(client.getOutputStream()));
        publishProgress(Event.CONNECTED);

        while (isRunning) {
          String msg = reader.readLine();
          if (msg != null && !msg.isEmpty()) {
            messageBuffer = msg;
            publishProgress(Event.MESSAGE);
          }
        }

      } catch (SocketTimeoutException ignored) {
      } catch (IOException e) {
        MyLog.e(LOG_TAG, "I/O error:" + e);
        publishProgress(Event.DISCONNECTED);
      }
    }

    return null;
  }

  @Override
  protected void onProgressUpdate(Event... events) {
    switch (events[0]) {
      case STARTED:
        listener.onStarted();
      case CONNECTED:
        listener.onConnected(client.getInetAddress(), client.getPort());
        break;
      case DISCONNECTED:
        listener.onDisconnected();
        break;
      case MESSAGE:
        listener.onMessage(messageBuffer);
        break;
    }
  }

  void start() {
    MyLog.d(LOG_TAG, "Start command received");
    isRunning = true;
    execute();
  }

  void stop() {
    MyLog.d(LOG_TAG, "Stop command received");
    isRunning = false;
  }

  interface NetworkEventListener {
    void onStarted();
    void onConnected(InetAddress inetAddress, int port);
    void onMessage(String msg);
    void onDisconnected();
  }
}
