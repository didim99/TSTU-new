package ru.didim99.tstu.core.oop;

/**
 * Created by didim99 on 23.02.19.
 */
public class GradeList {
  private static final int BAD_THRESHOLD = 40;
  private int lang, math, physics;

  int getSum() {
    return lang + math + physics;
  }

  boolean hasBad() {
    return lang < BAD_THRESHOLD
      || math < BAD_THRESHOLD
      || physics < BAD_THRESHOLD;
  }

  public int getLang() {
    return lang;
  }

  public int getMath() {
    return math;
  }

  public int getPhysics() {
    return physics;
  }

  public void setLang(int lang) {
    this.lang = lang;
  }

  public void setMath(int math) {
    this.math = math;
  }

  public void setPhysics(int physics) {
    this.physics = physics;
  }
}
