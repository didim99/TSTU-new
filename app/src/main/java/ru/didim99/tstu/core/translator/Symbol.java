package ru.didim99.tstu.core.translator;

import java.util.Locale;

/**
 * Created by didim99 on 05.11.18.
 */
public class Symbol {
  private final String name;
  private int type;

  public Symbol(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setType(int type) {
    this.type = type;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return String.format(Locale.US, "[%d] %s", type, name);
  }
}
