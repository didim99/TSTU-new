package ru.didim99.tstu.core.mp.notice;

/**
 * Created by didim99 on 26.02.19.
 */
public class Action {
  public static final class Type {
    public static final int ADD = 1;
    public static final int LOAD = 2;
    public static final int DELETE = 3;
  }

  private int type;
  private Notice notice;

  public Action(int type) {
    this.type = type;
  }

  public Action(int type, Notice notice) {
    this.notice = notice;
    this.type = type;
  }

  public int getType() {
    return type;
  }

  public Notice getNotice() {
    return notice;
  }
}
