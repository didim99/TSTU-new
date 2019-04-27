package ru.didim99.tstu.core.os.scheduler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by didim99 on 23.04.19.
 */
public class HWConfig {
  private static final String KEY_TICKS = "os.scheduler.ticks";
  private static final String KEY_RAM = "os.scheduler.ram";
  private static final String KEY_HDD = "os.scheduler.hdd";
  private static final int DEFAULT_TICKS = 100000;
  private static final int DEFAULT_RAM = 4096;
  private static final int DEFAULT_HDD = 150;
  // Hardware limits
  public static final int MIN_TICKS = 1000;
  public static final int MAX_TICKS = 1000000;
  public static final int MIN_RAM = 512;
  public static final int MAX_RAM = 65536;
  public static final int MIN_HDD = 25;
  public static final int MAX_HDD = 200;
  // Process limits
  public static final int MIN_PTICKS = DEFAULT_TICKS / 2;
  public static final int MAX_PTICKS = DEFAULT_TICKS * 50;
  public static final int MIN_BRAM = 1;
  public static final int MAX_BRAM = DEFAULT_RAM;
  public static final int MIN_DRAM = MIN_BRAM;
  public static final int MAX_DRAM = MAX_BRAM;
  public static final int MIN_IORATE = 1;
  public static final int MAX_IORATE = 50;

  private int ticks;
  private int totalRAM;
  private int maxHDDSpeed;
  private OnChangeListener listener;

  HWConfig(Context appContext, OnChangeListener listener) {
    SharedPreferences prefs = PreferenceManager
      .getDefaultSharedPreferences(appContext);
    ticks = prefs.getInt(KEY_TICKS, DEFAULT_TICKS);
    totalRAM = prefs.getInt(KEY_RAM, DEFAULT_RAM);
    maxHDDSpeed = prefs.getInt(KEY_HDD, DEFAULT_HDD);
    this.listener = listener;
  }

  public void saveConfig(Context appContext) {
    PreferenceManager.getDefaultSharedPreferences(appContext).edit()
      .putInt(KEY_TICKS, ticks)
      .putInt(KEY_RAM, totalRAM)
      .putInt(KEY_HDD, maxHDDSpeed)
      .apply();
  }

  public void update(int ticks, int totalRAM, int maxHDDSpeed) {
    this.ticks = ticks;
    this.totalRAM = totalRAM;
    this.maxHDDSpeed = maxHDDSpeed;
    listener.onHWConfigChanged();
  }

  public int getTicks() {
    return ticks;
  }

  public int getTotalRAM() {
    return totalRAM;
  }

  public int getMaxHDDSpeed() {
    return maxHDDSpeed;
  }

  interface OnChangeListener {
    void onHWConfigChanged();
  }
}
