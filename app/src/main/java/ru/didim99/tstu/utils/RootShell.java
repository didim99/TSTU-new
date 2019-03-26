package ru.didim99.tstu.utils;

import android.content.Context;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import ru.didim99.tstu.BuildConfig;
import ru.didim99.tstu.R;

/**
 * UNIX shell controller
 * Created by didim99 on 09.12.17.
 */

public class RootShell {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_shell";
  private static boolean available = true;
  private static Process shell;
  private static DataOutputStream stdin;
  private static InputStream stdout;
  private static InputStream stderr;
  private static String outBuf = null;
  private static String errBuf = null;

  public static void init(Context appContext) {
    MyLog.d(LOG_TAG, "Trying to get Root-access...");
    try {
      startSession();
      MyLog.d(LOG_TAG, "Root-access available");
    } catch (IOException e) {
      MyLog.w(LOG_TAG, "Root-access unavailable");
      Toast.makeText(appContext, R.string.rootUnavailable, Toast.LENGTH_LONG).show();
      available = false;
    }
  }

  private static void startSession() throws IOException {
    MyLog.d(LOG_TAG, "Starting new root shell session...");
    shell = Runtime.getRuntime().exec("su");
    stdin = new DataOutputStream(shell.getOutputStream());
    stderr = shell.getErrorStream();
    stdout = shell.getInputStream();
    MyLog.d(LOG_TAG, "Session started");
  }

  public static boolean hasRootAccess() {
    boolean flag = false;

    if (available) {
      MyLog.d(LOG_TAG, "checking root-access...");
      try {
        shell.exitValue();
      } catch (IllegalThreadStateException e) {
        if (e.getMessage().contains("Process has not yet terminated")) {
          exec("id");
          flag = outBuf.contains("uid=0(root)");
        }
      }

      if (flag)
        MyLog.d(LOG_TAG, "Root-access allowed");
      else
        MyLog.w(LOG_TAG, "Root-access denied");
    }

    return flag;
  }

  public static boolean exec(String command) {
    errBuf = null;
    outBuf = null;

    try {
      startSession();
      MyLog.d(LOG_TAG, "executing: " + command);
      stdin.writeBytes(String.format("exec %s\n", command));
      stdin.flush();

      BufferedReader br = new BufferedReader(new InputStreamReader(stderr));
      StringBuilder builder = new StringBuilder();
      String line;

      while ((line = br.readLine()) != null)
        builder.append(line).append("\n");
      br.close();
      errBuf = builder.toString();
      MyLog.v(LOG_TAG, "cmd err:\n  " + errBuf);

      br = new BufferedReader(new InputStreamReader(stdout));
      builder = new StringBuilder();

      while ((line = br.readLine()) != null)
        builder.append(line).append("\n");
      br.close();
      outBuf = builder.toString();
      MyLog.v(LOG_TAG, "cmd out:\n  " + outBuf);

      shell.destroy();
      return true;
    } catch (IOException e) {
      if (BuildConfig.DEBUG) e.printStackTrace();
      MyLog.e(LOG_TAG, e.getMessage());
      return false;
    }
  }

  public static String getOutput() {
    return outBuf;
  }

  public static String getError() {
    return errBuf;
  }
}
