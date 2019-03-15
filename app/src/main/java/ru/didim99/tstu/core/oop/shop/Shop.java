package ru.didim99.tstu.core.oop.shop;

import java.util.ArrayList;

/**
 * Created by didim99 on 13.03.19.
 */
public class Shop {
  private int type;
  private String name;
  private ArrayList<Division> divisions;
  private int currentDivision;

  public Shop(String name, int type) {
    divisions = new ArrayList<>();
    currentDivision = 0;
    this.name = name;
    this.type = type;
  }

  public Division selectDiv(int id) {
    return divisions.get(currentDivision = id);
  }

  public ArrayList<String> getDivList() {
    ArrayList<String> list = new ArrayList<>();
    for (Division d : divisions)
      list.add(d.getName());
    return list;
  }

  public int getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public int getCurrentDivId() {
    return currentDivision;
  }

  public ArrayList<Division> getDivisions() {
    return divisions;
  }
}
