package ru.didim99.tstu.core.os;

/**
 * Created by didim99 on 26.03.19.
 */
public class MappingInfo {
  private boolean executable;
  private String name;

  MappingInfo(String name) {
    this.name = name;
  }

  void setExecutable(boolean executable) {
    this.executable = executable;
  }

  public String getName() {
    return name;
  }

  public boolean isExecutable() {
    return executable;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof MappingInfo)) return false;
    MappingInfo other = (MappingInfo) obj;
    return name.equals(other.name);
  }
}
