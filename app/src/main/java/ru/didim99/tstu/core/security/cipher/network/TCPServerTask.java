package ru.didim99.tstu.core.security.cipher.network;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 16.09.20.
 */

class TCPServerTask extends AsyncTask<Void, TCPServerTask.Event, Void> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_serverTask";
  private static final int TIMEOUT = 1000;

  public enum Event { STARTED, CONNECTED, DISCONNECTED, MESSAGE, ERROR }

  private int port;
  private boolean isRunning, isInterrupted;
  private NetworkEventListener listener;
  private Throwable globalError;
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
      globalError = e;
      publishProgress(Event.ERROR);
      isRunning = false;
      return null;
    }

    MyLog.d(LOG_TAG, "Listening on port: " + port);
    publishProgress(Event.STARTED);
    while (isRunning) {
      try {
        client = socket.accept();
        client.setSoTimeout(TIMEOUT);
        BufferedReader reader = new BufferedReader(
          new InputStreamReader(client.getInputStream()));
        MyLog.d(LOG_TAG, "Connection received: " + client);
        publishProgress(Event.CONNECTED);

        while (isRunning && !isInterrupted) {
          try {
            String msg = reader.readLine();
            if (msg == null) {
              client.close();
              MyLog.d(LOG_TAG, "Disconnected by client");
              publishProgress(Event.DISCONNECTED);
              break;
            }

            if (!msg.isEmpty()) {
              messageBuffer = msg;
              publishProgress(Event.MESSAGE);
            }
          } catch (SocketTimeoutException ignored) {}
        }

        if (!client.isClosed()) {
          client.close();
          client = null;
          MyLog.d(LOG_TAG, "Disconnected by server");
          publishProgress(Event.DISCONNECTED);
        }

        if (isInterrupted)
          isInterrupted = false;
      } catch (SocketTimeoutException ignored) {
      } catch (IOException e) {
        MyLog.e(LOG_TAG, "I/O error:" + e);
        publishProgress(Event.DISCONNECTED);
      }
    }

    try {
      socket.close();
      MyLog.d(LOG_TAG, "Stopped");
    } catch (IOException e) {
      MyLog.e(LOG_TAG, "Socket not closed: " + e);
      globalError = e;
      publishProgress(Event.ERROR);
      return null;
    }

    return null;
  }

  @Override
  protected void onProgressUpdate(Event... events) {
    switch (events[0]) {
      case STARTED:
        listener.onStarted();
        break;
      case CONNECTED:
        try {
          listener.onConnected(client);
        } catch (IOException e) {
          listener.onError(e);
        }
        break;
      case DISCONNECTED:
        try {
          listener.onDisconnected();
        } catch (IOException e) {
          listener.onError(e);
        }
        break;
      case MESSAGE:
        listener.onMessage(messageBuffer);
        break;
      case ERROR:
        listener.onError(globalError);
        globalError = null;
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

  public void disconnect() {
    if (!isInterrupted)
      isInterrupted = true;
  }

  interface NetworkEventListener {
    void onStarted();
    void onConnected(Socket client) throws IOException;
    void onDisconnected() throws IOException;
    void onMessage(String msg);
    void onError(Throwable cause);
  }
}
