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

/**
 * Created by didim99 on 16.09.20.
 */

class TCPServerTask extends AsyncTask<Void, Void, Void> {
  private static final int TIMEOUT = 5000;

  private int port;
  private boolean isRunning;
  private ServerSocket socket;
  private NetworkEventListener listener;

  TCPServerTask(int port, NetworkEventListener listener) {
    this.isRunning = false;
    this.listener = listener;
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
        BufferedReader reader = new BufferedReader(
          new InputStreamReader(client.getInputStream()));
        BufferedWriter writer = new BufferedWriter(
          new OutputStreamWriter(client.getOutputStream()));
        listener.onConnected(client.getInetAddress(), client.getPort());

        while (true) {
          String msg = reader.readLine();
          listener.onMessage(msg);
        }

      } catch (IOException e) {
        listener.onDisconnected();
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

  interface NetworkEventListener {
    void onConnected(InetAddress inetAddress, int port);
    void onMessage(String msg);
    void onDisconnected();
  }
}
