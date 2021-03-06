package ru.didim99.tstu.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.RawRes;
import android.widget.Toast;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.Series;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.math.common.PointRN;

/**
 * Created by didim99 on 10.09.18.
 */
public class Utils {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Utils";
  private static final String BYTES = "bKMGT";

  /* ======== STRING UTILS ========== */

  public static String joinStr(String delimiter, String... args) {
    if (args == null || args.length == 0)
      return null;
    StringBuilder builder = new StringBuilder();
    int size = args.length - 1;
    for (int i = 0; i < size; i++)
      builder.append(args[i]).append(delimiter);
    builder.append(args[size]);
    return builder.toString();
  }

  public static String joinStr(String delimiter, List<String> args) {
    if (args == null || args.isEmpty())
      return null;
    StringBuilder builder = new StringBuilder();
    int size = args.size() - 1;
    for (int i = 0; i < size; i++)
      builder.append(args.get(i)).append(delimiter);
    builder.append(args.get(size));
    return builder.toString();
  }

  public static String collapse(String str, int maxLength) {
    if (str == null) return null;
    int length = str.length();
    if (length <= maxLength) return str;

    int offset = maxLength / 2;
    int delta = offset > 10 ? (offset > 100 ? 10 : 5) : 2;
    delta = offset / delta;

    int startIndex = offset - 1;
    int endIndex = length - offset;
    boolean startFound = false, endFound = false;

    for (int i = 0; i < delta; i++) {
      if (startFound && endFound) break;
      if (!startFound && Character.isWhitespace(
        str.charAt(startIndex - i))) {
        startFound = true;
        startIndex -= i - 1;
      }

      if (!endFound && Character.isWhitespace(
        str.charAt(endIndex + i))) {
        endFound = true;
        endIndex += i;
      }
    }

    return str.substring(0, startIndex) + "…" + str.substring(endIndex);
  }

  public static String capitalizeFirst(String str) {
    return str.substring(0, 1).toUpperCase().concat(str.substring(1));
  }

  public static String formatBytes(int bytes) {
    int offset = 0;
    while (bytes > 1024) { bytes >>= 10; offset++; }
    return String.format(Locale.US, "%d%c",
      bytes, BYTES.charAt(offset));
  }

  /* ======== MATH UTILS ======== */

  public static BigInteger dec2bin(int n) {
    return new BigInteger(Integer.toBinaryString(n));
  }

  public static int randInRange(Random random, int min, int max) {
    return min + random.nextInt(max - min + 1);
  }

  public static double randInRangeD(Random random, double min, double max) {
    return min + random.nextDouble() * (max - min);
  }

  public static int bound(int i, int max) {
    return i < 0 ? 0 : (i > max ? max : i);
  }

  public static double bound(double d, double max) {
    return d < 0 ? 0 : (d > max ? max : d);
  }

  public static double norm(double v, double min, double max) {
    return (v - min) / (max - min);
  }

  public static double deNorm(double v, double min, double max) {
    return min + v * (max - min);
  }

  public static double map(double v, double fromMin, double fromMax, double toMin, double toMax) {
    return deNorm(norm(v, fromMin, fromMax), toMin, toMax);
  }

  public static double calcError(double actual, double expected) {
    return Math.abs(expected - actual) / expected * 100;
  }

  public static int combination(int n, int k) {
    if (k == 0 || k == n) return 1;
    return combination(n - 1, k) + combination(n - 1, k - 1);
  }

  /* ======== ARRAY UTILS =========== */

  public static long[] stringArrayToLongArray(String... args) {
    if (args == null || args.length == 0)
      return null;
    try {
      long[] result = new long[args.length];
      for (int i = 0; i < args.length; i++)
        result[i] = Long.parseLong(args[i]);
      return result;
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

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

  public static String[] intArrayToStringArray (int[] args) {
    if (args == null || args.length == 0)
      return null;
    String[] result = new String[args.length];
    for (int i = 0; i < args.length; i++)
      result[i] = String.valueOf(args[i]);
    return result;
  }

  public static String[] doubleArrayToStringArray(double[] args, int precision) {
    if (args == null || args.length == 0)
      return null;
    String format = String.format(Locale.US, "%%.%df", precision);
    String[] result = new String[args.length];
    for (int i = 0; i < args.length; i++)
      result[i] = String.format(Locale.US, format, args[i]);
    return result;
  }

  public static void plainCopy(InputStream in, OutputStream out)
    throws IOException {
    byte[] buffer = new byte[1024]; int len;
    while ((len = in.read(buffer)) != -1)
      out.write(buffer, 0, len);
  }

  /* ======== FILE UTILS =========== */

  public static ArrayList<String> readFile(String fileName, boolean raw)
    throws IOException {
    MyLog.d(LOG_TAG, "Reading: " + fileName);
    File file = new File(fileName);
    MyLog.d(LOG_TAG, "File size: " + file.length());
    BufferedReader reader = new BufferedReader(new FileReader(file));
    ArrayList<String> data = new ArrayList<>();
    String line;
    while ((line = reader.readLine()) != null)
      if (!line.isEmpty() || raw) data.add(line);
    MyLog.d(LOG_TAG, "Reading completed");
    return data;
  }

  public static ArrayList<String> readFile(String fileName)
    throws IOException {
    return readFile(fileName, false);
  }

  public static byte[] readFileRaw(String fileName)
    throws IOException {
    MyLog.d(LOG_TAG, "Reading: " + fileName);
    File file = new File(fileName);
    byte[] buff = new byte[(int) file.length()];
    MyLog.d(LOG_TAG, "File size: " + file.length());
    DataInputStream src = new DataInputStream(new FileInputStream(file));
    src.readFully(buff);
    src.close();
    MyLog.d(LOG_TAG, "Reading completed");
    return buff;
  }

  public static void writeFile(String fileName, List<String> data)
    throws IOException {
    MyLog.d(LOG_TAG, "Writing: " + fileName);
    File file = new File(fileName);
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    int last = data.size() - 1;
    for (int i = 0; i <= last; i++) {
      writer.append(data.get(i));
      if (i < last) writer.newLine();
    }
    writer.close();
    MyLog.d(LOG_TAG, "Writing completed (" + file.length() + ")");
  }

  public static void writeFile(String fileName, byte[] data)
    throws IOException {
    MyLog.d(LOG_TAG, "Writing: " + fileName);
    File file = new File(fileName);
    DataOutputStream src = new DataOutputStream(new FileOutputStream(file));
    src.write(data);
    src.flush();
    src.close();
    MyLog.d(LOG_TAG, "Writing completed");
  }

  /* ======== ANDROID UTILS ======== */

  public static boolean isIntentSafe(Context context, Intent intent) {
    PackageManager packageManager = context.getPackageManager();
    List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
    return activities.size() > 0;
  }

  public static String resToFile(Context context, @RawRes int resId)
    throws IOException {
    String name = context.getResources().getResourceEntryName(resId);
    String dstPath = context.getFilesDir().getAbsolutePath()
      .concat(File.separator).concat(name);
    MyLog.d(LOG_TAG, "Raw resource ["
      + name + "] resolved to: " + dstPath);
    File file = new File(dstPath);

    if (!file.exists()) {
      MyLog.d(LOG_TAG, "Copying resource data to file: " + dstPath);
      InputStream src = context.getResources().openRawResource(resId);
      OutputStream dst = new FileOutputStream(file);
      byte[] buffer = new byte[1024]; int length;
      while ((length = src.read(buffer)) > 0)
        dst.write(buffer, 0, length);
      MyLog.d(LOG_TAG, "Resource data copied");
      dst.close();
      src.close();
    }

    return dstPath;
  }

  public static boolean copyToClipboard(Context context, CharSequence text) {
    ClipboardManager cbm = (ClipboardManager)
      context.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData data = ClipData.newPlainText(null, text);

    if (cbm != null) {
      cbm.setPrimaryClip(data);
      Toast.makeText(context, R.string.copiedToClipboard, Toast.LENGTH_SHORT).show();
      return true;
    } else
      return false;
  }

  /* ======== COLOR UTILS ======== */

  public static int lerp(int fg, int bg, double alpha) {
    double gamma = 1 - alpha;
    int res = (int) ((fg & 0xff) * alpha + (bg & 0xff) * gamma);
    res |= (int) (((fg >> 8) & 0xff) * alpha + ((bg >> 8) & 0xff) * gamma) << 8;
    res |= (int) (((fg >> 16) & 0xff) * alpha + ((bg >> 16) & 0xff) * gamma) << 16;
    res |= 0xff000000;
    return res;
  }

  public static int luma(int color) {
    return (int) (0.2126 * ((color >> 16) & 0xff)
      + 0.7152 * ((color >> 8) & 0xff) + 0.0722 * (color & 0xff));
  }

  /* ======== GRAPH UTILS ======== */

  private static final int MAX_SERIES_VISIBLE = 500;

  @SuppressWarnings("unchecked")
  public static <E extends PointRN> Series<E> buildSeries(List<E> data, String name) {
    LineGraphSeries<E> series = new LineGraphSeries<>();

    if (data.size() <= MAX_SERIES_VISIBLE) {
      E[] ref = (E[]) Array.newInstance(data.get(0).getClass(), 0);
      series.resetData(data.toArray(ref));
    } else {
      double step = data.size() / (double) MAX_SERIES_VISIBLE;
      for (int pos = 0; pos < MAX_SERIES_VISIBLE; pos++)
        series.appendData(data.get((int) Math.floor(pos * step)),
          false, MAX_SERIES_VISIBLE);
    }

    series.setTitle(name);
    return series;
  }
}
