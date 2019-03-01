package ru.didim99.tstu.core.mp.students;

/**
 * Created by didim99 on 26.02.19.
 */
public class Student {
  private int id;
  private String name;
  private long datetime;


  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public long getDatetime() {
    return datetime;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setDatetime(long datetime) {
    this.datetime = datetime;
  }
}
