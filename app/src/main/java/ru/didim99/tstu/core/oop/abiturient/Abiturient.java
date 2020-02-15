package ru.didim99.tstu.core.oop.abiturient;

/**
 * Created by didim99 on 23.02.19.
 */
public class Abiturient {
  private transient int id;
  private String surname;
  private String name;
  private String patronymic;
  private String address;
  private GradeList grades;

  public Abiturient() {
    grades = new GradeList();
  }

  public Abiturient init() {
    this.id = getFullName().concat(address).hashCode();
    return this;
  }

  public String getFullName() {
    return String.format("%s %s %s", surname, name, patronymic);
  }

  public int getId() {
    return id;
  }

  public String getSurname() {
    return surname;
  }

  public String getName() {
    return name;
  }

  public String getPatronymic() {
    return patronymic;
  }

  public String getAddress() {
    return address;
  }

  public GradeList getGrades() {
    return grades;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPatronymic(String patronymic) {
    this.patronymic = patronymic;
  }

  public void setAddress(String address) {
    this.address = address;
  }
}
