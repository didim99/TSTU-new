package ru.didim99.tstu.core.itheory.encryption;

/**
 * Created by didim99 on 05.03.20.
 */
public class Action {
  enum Type { LOAD_FILE, SAVE_FILE, GEN_KEY, PROCESS }

  private Type type;
  private String path;
  private int keyLength;

  private Action(Type type) {
    this.type = type;
  }

  Type getType() {
    return type;
  }

  String getPath() {
    return path;
  }

  int getKeyLength() {
    return keyLength;
  }

  static class Builder {
    private Action action;

    Builder(Type type) {
      action = new Action(type);
    }

    Builder setPath(String path) {
      action.path = path;
      return this;
    }

    Builder setKeyLength(int length) {
      action.keyLength = length;
      return this;
    }

    Action build() {
      return action;
    }
  }
}
