package ru.didim99.tstu.core.os.scheduler;

/**
 * Created by didim99 on 25.04.19.
 */
public class MemoryManager {
  private int total, used;

  void configure(int MiB) {
    total = MiB << 10;
    used = 0;
  }

  boolean alloc(int kiB) {
    if (total - used < kiB)
      return false;
    used += kiB;
    return true;
  }

  void free(int kiB) {
    used -= kiB < used ? kiB : used;
  }

  int getUsage() {
    return used;
  }

  int getFree() {
    return total - used;
  }

  @Override
  public String toString() {
    return "MemoryManager (" + used + "/" + total + " kiB)";
  }
}
