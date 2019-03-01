package ru.didim99.tstu.core.mp.students;

/**
 * Created by didim99 on 26.02.19.
 */
public class Action {
  public static final class Type {
    public static final int VIEW = 1;
    public static final int ADD = 2;
    public static final int REPLACE = 3;
  }

  private int type;
  private Student student;

  public Action(int type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }

  public Student getStudent() {
    return student;
  }

  public void setStudent(Student student) {
    this.student = student;
  }
}
