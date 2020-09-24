package ru.didim99.tstu.core.security.cipher.network;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 20.09.20.
 */

class TCPReplySender extends Thread {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_RS";
  private static final long QUEUE_TIMEOUT = 5000;

  private BufferedWriter out;
  private BlockingQueue<String> queue;

  public TCPReplySender(Socket client) throws IOException {
    super("ReplySender-" + client.getInetAddress());
    this.out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
    this.queue = new LinkedBlockingQueue<>();
    start();
  }

  @Override
  public void run() {
    while (!isInterrupted()) {
      try {
        String reply = queue.poll(QUEUE_TIMEOUT, TimeUnit.MILLISECONDS);
        if (reply == null) continue;
        MyLog.d(LOG_TAG, "Send " + reply);
        out.write(reply + "\r\n");
        out.flush();
      } catch (InterruptedException ignored) {
      } catch (IOException e) {
        break;
      }
    }
  }

  void send(String reply) {
    queue.add(reply);
  }

  void disconnect() throws IOException {
    out.close();
    finish();
  }

  void finish() {
    queue.clear();
    interrupt();
  }
}
