package ru.didim99.tstu.core.os;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseArray;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.RootShell;

/**
 * Created by didim99 on 25.03.19.
 */
public class ProcessList {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_PList";
  private static final String TMPFILE = "processStateCache";
  public  static final String DEVDIR = "/dev";
  private static final String PROCDIR = "/proc";
  private static final String PIDDIR = PROCDIR + "/%d";
  private static final String TASKDIR = PIDDIR + "/task";
  private static final String CMDFILE = PIDDIR + "/cmdline";
  private static final String COMMFILE = PIDDIR + "/comm";
  private static final String MAPSFILE = PIDDIR + "/maps";
  private static final String STATFILE = PIDDIR + "/stat";
  private static final String STATUSFILE = PIDDIR + "/status";
  private static final int SCAN_TRESHOLD = 800;

  private SparseArray<ProcessInfo> map;
  private ArrayList<ProcessInfo> list, old, view;
  private int processCount, threadCount, running;
  private boolean showKernelThreads, rootFailed;
  private long lastUpdateTime;
  private String tmpFile;

  ProcessList(Context context) {
    if (!RootShell.isInitCompleted()) RootShell.init(context);
    tmpFile = context.getCacheDir().getAbsolutePath()
      .concat(File.separator).concat(TMPFILE);
    rootFailed = !RootShell.hasRootAccess();
    old = new ArrayList<>();
    list = new ArrayList<>();
    view = new ArrayList<>();
    map = new SparseArray<>();
  }

  void update() {
    MyLog.d(LOG_TAG, "Updating process list");
    lastUpdateTime = System.currentTimeMillis();
    processCount = threadCount = running = 0;
    scanProcessTree(PROCDIR, null, null);
    removeOldProcesses();
    updateViewList();
    MyLog.d(LOG_TAG, "Process list updated");
  }

  @SuppressLint("DefaultLocale")
  ArrayList<ProcessInfo> getThreadInfo(ProcessInfo parent) {
    int pid = parent.getPid();
    MyLog.d(LOG_TAG, "Collecting threads info for PID " + pid);
    ArrayList<ProcessInfo> list = new ArrayList<>(parent.getThreads());
    String dirname = String.format(TASKDIR, pid);
    scanProcessTree(dirname, parent, list);
    Collections.sort(list, pidComparator);
    return list;
  }

  @SuppressLint("DefaultLocale")
  ArrayList<MappingInfo> getMappingInfo(ProcessInfo process)
    throws IOException {
    int pid = process.getPid();
    MyLog.d(LOG_TAG, "Collecting mapping info for PID " + pid);
    ArrayList<MappingInfo> list = new ArrayList<>();
    String name = String.format(MAPSFILE, pid);
    ArrayList<String> data;

    try {
      data = readFile(name);
    } catch (IOException e) {
      if (rootFailed) throw e;
      String cmd = String.format("cp %s %s", name, tmpFile);
      if (!RootShell.exec(cmd)) throw new IOException(
        "Command [cp] failed: ".concat(RootShell.getError()), e);
      cmd = String.format("chmod 606 %s", tmpFile);
      if (!RootShell.exec(cmd)) throw new IOException(
        "Command [chmod] failed: ".concat(RootShell.getError()), e);
      data = readFile(tmpFile);
    }

    for (String line : data) {
      String[] sub = line.split("\\s+");
      if (sub.length < 6) continue;
      if (sub[5].charAt(0) != '/') continue;
      MappingInfo info = new MappingInfo(sub[5]);
      if (list.contains(info)) continue;
      info.setExecutable(sub[1].charAt(2) == 'x');
      list.add(info);
    }

    return list;
  }

  private void scanProcessTree(String dirname, ProcessInfo parent,
                               ArrayList<ProcessInfo> list) {
    String[] names = new File(dirname).list();
    if (names == null) return;
    for (String name : names) {
      char c = name.charAt(0);
      if (c < '0' || c > '9') continue;
      int pid = Integer.parseInt(name);
      if (parent != null && pid == parent.getPid()) continue;
      if (pid <= 0) continue;
      ProcessInfo info;
      if (list == null) {
        info = getProcessOrCreate(pid);
        info.setUpdated(lastUpdateTime);
      } else {
        info = new ProcessInfo(pid);
        list.add(info);
      }

      try {
        if (parent == null)
          readCmdFile(info);
        readCommFile(info);
        readStatFile(info);
        readStatusFile(info);
        if (parent == null) {
          if (!info.isThread()) {
            threadCount += info.getThreads();
            processCount++;
          } else threadCount++;
          if (info.isRunning())
            running++;
        }
      } catch (IOException e) {
        if (parent == null)
          removeProcess(info);
        e.printStackTrace();
      }
    }
  }

  @SuppressLint("DefaultLocale")
  private void readCmdFile(ProcessInfo info) throws IOException {
    String filename = String.format(CMDFILE, info.getPid());
    String line = readFileStr(filename);
    if (line == null) info.setKernelThread(true);
    else info.setName(line.replace('\0', ' '));
  }

  @SuppressLint("DefaultLocale")
  private void readCommFile(ProcessInfo info) throws IOException {
    if (info.getName() != null) return;
    String filename = String.format(COMMFILE, info.getPid());
    String line = readFileStr(filename);
    if (line != null) line = line.trim();
    info.setName(line);
  }

  @SuppressLint("DefaultLocale")
  private void readStatFile(ProcessInfo info) throws IOException {
    String filename = String.format(STATFILE, info.getPid());
    String line = readFileStr(filename);
    if (line == null) return;
    line = line.replaceAll("\\(.+\\)", "-");
    String[] sub = line.split(" ");
    info.setState(sub[2].charAt(0));
    info.setPpid(Integer.parseInt(sub[3]));
    info.setNice(Integer.parseInt(sub[18]));
    info.setThreads(Integer.parseInt(sub[19]));
    info.setmRes(Integer.parseInt(sub[23]) << 2);
  }

  @SuppressLint("DefaultLocale")
  private void readStatusFile(ProcessInfo info) throws IOException {
    String filename = String.format(STATUSFILE, info.getPid());
    ArrayList<String> lines = readFile(filename);
    String[] sub;
    for (String line : lines) {
      sub = line.split(":\\s+");
      switch (sub[0]) {
        case "Tgid":
          info.setTgid(Integer.parseInt(sub[1]));
          return;
      }
    }
  }

  private ProcessInfo getProcessOrCreate(int pid) {
    ProcessInfo info = map.get(pid);
    if (info == null) {
      info = new ProcessInfo(pid);
      map.append(pid, info);
      list.add(info);
    }

    return info;
  }

  private void removeOldProcesses() {
    for (ProcessInfo info : list) {
      if (lastUpdateTime - info.getUpdated() > SCAN_TRESHOLD)
        old.add(info);
    }

    for (ProcessInfo info : old)
      removeProcess(info);
    old.clear();
  }

  private void removeProcess(ProcessInfo info) {
    map.remove(info.getPid());
    list.remove(info);
  }

  private void updateViewList() {
    view.clear();
    for (ProcessInfo info : list) {
      if (showKernelThreads || !info.isKernelThread())
        view.add(info);
    }
    Collections.sort(view, pidComparator);
  }

  void setShowKernelThreads(boolean showKernelThreads) {
    this.showKernelThreads = showKernelThreads;
  }

  public ArrayList<ProcessInfo> getList() {
    return view;
  }

  public int getProcessCount() {
    return processCount;
  }

  public int getThreadCount() {
    return threadCount;
  }

  public int getRunningCount() {
    return running;
  }

  boolean isShowKernelThreads() {
    return showKernelThreads;
  }

  private static Comparator<ProcessInfo> pidComparator =
    (p1, p2) -> Integer.compare(p1.getPid(), p2.getPid());

  private static ArrayList<String> readFile(String fileName)
    throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(fileName));
    ArrayList<String> data = new ArrayList<>();
    String line;
    while ((line = reader.readLine()) != null)
      data.add(line);
    reader.close();
    return data;
  }

  private static String readFileStr(String fileName)
    throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(fileName));
    String line = reader.readLine();
    reader.close();
    return line;
  }
}
