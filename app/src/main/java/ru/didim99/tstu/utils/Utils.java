package ru.didim99.tstu.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by didim99 on 10.09.18.
 */
public class Utils {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Utils";

  /* ======== STRING UTILS ========== */

  public static String joinStr(String delimiter, ArrayList<String> args) {
    if (args == null || args.isEmpty())
      return null;
    StringBuilder builder = new StringBuilder();
    int size = args.size() - 1;
    for (int i = 0; i < size; i++)
      builder.append(args.get(i)).append(delimiter);
    builder.append(args.get(size));
    return builder.toString();
  }

  /* ======== ARRAY UTILS =========== */

  public static double[] stringArrayToDoubleArray(String... args) {
    if (args == null || args.length == 0)
      return null;
    try {
      double[] result = new double[args.length];
      for (int i = 0; i < args.length; i++)
        result[i] = Double.parseDouble(args[i]);
      return result;
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  /* ======== FILE UTILS =========== */

  public static ArrayList<String> readFile(String fileName)
    throws IOException {
    MyLog.d(LOG_TAG, "Reading: " + fileName);
    File file = new File(fileName);
    MyLog.d(LOG_TAG, "File size: " + file.length());
    BufferedReader reader = new BufferedReader(new FileReader(file));
    ArrayList<String> data = new ArrayList<>();
    String line;
    while ((line = reader.readLine()) != null)
      if (!line.isEmpty()) data.add(line);
    MyLog.d(LOG_TAG, "Reading completed");
    return data;
  }

  public static void writeFile(String fileName, ArrayList<String> data)
    throws IOException {
    MyLog.d(LOG_TAG, "Writing: " + fileName);
    File file = new File(fileName);
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    for (String line : data) {
      writer.append(line);
      writer.newLine();
    }
    writer.close();
    MyLog.d(LOG_TAG, "Writing completed (" + file.length() + ")");
  }

  /* ======== ANDROID UTILS ======== */

  public static boolean isIntentSafe(Context context, Intent intent) {
    PackageManager packageManager = context.getPackageManager();
    List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
    return activities.size() > 0;
  }
}
