package ru.didim99.tstu.core.os.procinfo;

import android.content.Context;
import android.os.AsyncTask;
import java.io.IOException;
import java.util.ArrayList;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 25.03.19.
 */
public class ProcessScanner extends AsyncTask<Void, Void, Void> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_PScanner";
  private static final long INTERVAL = 2000;

  private boolean running, paused;
  private OnUpdateListener listener;
  private ProcessList processList;

  public ProcessScanner(Context context, OnUpdateListener listener) {
    this.processList = new ProcessList(context);
    this.listener = listener;
  }

  public void start() {
    executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  @Override
  protected void onPreExecute() {
    this.running = true;
    this.paused = true;
  }

  @Override
  protected Void doInBackground(Void... voids) {
    try {
      processList.update();
      publishProgress();
      while (running) {
        Thread.sleep(INTERVAL);
        if (paused) continue;
        processList.update();
        publishProgress();
      }
    } catch (InterruptedException e) {
      MyLog.e(LOG_TAG, "Interrupted by: " + e);
    }
    return null;
  }

  @Override
  protected void onProgressUpdate(Void... values) {
    listener.onProcessListUpdated(processList);
  }

  public void pause(boolean paused) {
    this.paused = paused;
  }

  public void finish() {
    running = false;
  }

  public void setShowKernelThreads(boolean showKernelThreads) {
    processList.setShowKernelThreads(showKernelThreads);
  }

  public boolean isShowKernelThreads() {
    return processList.isShowKernelThreads();
  }

  public ArrayList<ProcessInfo> getThreadInfo(ProcessInfo parent) {
    return processList.getThreadInfo(parent);
  }

  public ArrayList<MappingInfo> getMappingInfo(ProcessInfo process)
    throws IOException {
    return processList.getMappingInfo(process);
  }

  public interface OnUpdateListener {
    void onProcessListUpdated(ProcessList processList);
  }
}
