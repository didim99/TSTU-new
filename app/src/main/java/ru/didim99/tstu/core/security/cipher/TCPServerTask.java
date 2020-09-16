package ru.didim99.tstu.core.security.cipher;

import android.os.AsyncTask;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by didim99 on 16.09.20.
 */

class TCPServerTask extends AsyncTask<Void, Void, Void> {
  private static final int TIMEOUT = 5000;

  private int port;
  private boolean isRunning;
  private ServerSocket socket;

  TCPServerTask(int port) {
    this.isRunning = false;
    this.port = port;
  }

  @Override
  protected Void doInBackground(Void... voids) {
    try {
      socket = new ServerSocket(port);
      socket.setSoTimeout(TIMEOUT);
    } catch (IOException e) {
      isRunning = false;
      return null;
    }

    while (isRunning) {
      try {
        Socket client = socket.accept();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return null;
  }

  void start() {
    isRunning = true;
    execute();
  }

  void stop() {
    isRunning = false;
  }
}
