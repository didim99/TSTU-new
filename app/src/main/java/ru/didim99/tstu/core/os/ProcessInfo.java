package ru.didim99.tstu.core.os;

/**
 * Created by didim99 on 25.03.19.
 */
public class ProcessInfo {
  private long updated;
  private boolean isKernelThread;
  private int pid, ppid, tgid;
  private int threads, nice;
  private String name;
  private char state;
  private int mRes;

  ProcessInfo(int pid) {
    this.pid = pid;
  }

  public int getPid() {
    return pid;
  }

  public int getPpid() {
    return ppid;
  }

  public int getTgid() {
    return tgid;
  }

  public String getName() {
    return name;
  }

  public char getState() {
    return state;
  }

  public int getThreads() {
    return threads;
  }

  public int getNice() {
    return nice;
  }

  public int getMRes() {
    return mRes;
  }

  public boolean isKernelThread() {
    return isKernelThread;
  }

  public boolean isUserlandThread() {
    return pid != tgid;
  }

  public boolean isThread() {
    return isKernelThread() || isUserlandThread();
  }

  public boolean isRunning() {
    return state == 'R';
  }

  public long getUpdated() {
    return updated;
  }

  void setUpdated(long updated) {
    this.updated = updated;
  }

  void setPpid(int ppid) {
    this.ppid = ppid;
  }

  void setTgid(int tgid) {
    this.tgid = tgid;
  }

  void setName(String name) {
    this.name = name;
  }

  void setState(char state) {
    this.state = state;
  }

  void setThreads(int threads) {
    this.threads = threads;
  }

  void setNice(int nice) {
    this.nice = nice;
  }

  void setKernelThread(boolean kernelThread) {
    isKernelThread = kernelThread;
  }

  void setmRes(int mRes) {
    this.mRes = mRes;
  }
}
