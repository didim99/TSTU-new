package ru.didim99.tstu.utils;

import android.util.Log;
import ru.didim99.tstu.BuildConfig;

/**
 * custom logger class
 * Created by didim99 on 06.08.17.
 */

public class MyLog {
  public static final String LOG_TAG_BASE = "TSTU_log";
  private static final boolean debugEnabled = BuildConfig.DEBUG;

  public static void v (String tag, String msg) {
    if (debugEnabled) Log.v(tag, msg);
  }

  public static void d (String tag, String msg) {
    if (debugEnabled) Log.d(tag, msg);
  }

  public static void w (String tag, String msg) {
    if (debugEnabled) Log.w(tag, msg);
  }

  public static void e (String tag, String msg) {
    if (debugEnabled) Log.e(tag, msg);
  }
}
