package ru.didim99.tstu.core.itheory.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 14.02.20.
 */
public class Image {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Image";
  private static final String[] EXT_IMG = {".png", ".jpg", ".bmp"};
  private static final String[] EXT_TXT = {".img", ".txt"};
  private static final String NAME_DEFAULT = "untitled.img";
  private static final String NAME_SUFFIX = "-new";
  public static final String EXT_SEP = ".";
  //Image file structure
  private static final String SIGNATURE = "RLE 1.0";
  private static final String SECTION_HEADER = "head";
  private static final String SECTION_DATA = "data";
  private static final String FIELD_TYPE = "t";
  private static final String FIELD_SIZE = "s";
  private static final String FIELD_COLOR = "c";
  private static final String VALUE_SEP = " ";
  private static final String FLAG_FALSE = "0";
  private static final String FLAG_TRUE = "1";
  //Other settings
  public static final int MIN_SIZE = 4;
  public static final int MAX_SIZE = 512;
  public static final int DEFAULT_SIZE = 32;
  private static final int DEFAULT_BG = 0xff6aa4f5;
  private static final int DEFAULT_FG = 0xff000000;
  private static final int LUMA_THRESHOLD = 128;

  private String name;
  private Bitmap bitmap;
  private int width, height;
  private int colorBg, colorFg;
  private Boolean compressed;

  private Image() { this(null); }

  private Image(Bitmap src) {
    this.colorBg = DEFAULT_BG;
    this.colorFg = DEFAULT_FG;
    this.name = NAME_DEFAULT;
    if (src != null) {
      this.compressed = false;
      this.width = src.getWidth();
      this.height = src.getHeight();
      this.bitmap = src;
    }
  }

  Image(int width, int height) {
    this(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888));
    clear();
  }

  public String getName() {
    return name;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public Bitmap getBitmap() {
    return bitmap;
  }

  public boolean isCompressed() {
    return compressed;
  }

  void setCompressed(Boolean compressed) {
    this.compressed = compressed;
  }

  void toggleCompressedState() {
    compressed = !compressed;
  }

  void clear() {
    bitmap.eraseColor(colorBg);
  }

  void randomize() {
    Random random = new Random();
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++)
        bitmap.setPixel(x, y, random.nextBoolean() ? colorFg : colorBg);
    }
  }

  private void loadUncompressed(ArrayList<String> data) {
    int x = 0, y = 0;
    for (String line : data) {
      for (int i = 0; i < line.length(); i++) {
        bitmap.setPixel(x, y, line.charAt(i) == FLAG_FALSE.charAt(0)
          ? colorBg : colorFg);
        if (++x == width) { x = 0; y++; }
      }
    }
  }

  private void loadCompressed(String data) {
    String[] parts = data.split(VALUE_SEP);

    int x = 0, y = 0;
    for (int i = 0; i < parts.length; i += 2) {
      int color = parts[i].equals(FLAG_FALSE) ? colorBg : colorFg;
      int count = Integer.parseInt(parts[i + 1]);
      for (int j = 0; j < count; j++) {
        bitmap.setPixel(x, y, color);
        if (++x == width) { x = 0; y++; }
      }
    }
  }

  private ArrayList<String> writeUncompressed() {
    ArrayList<String> data = new ArrayList<>(height);

    for (int y = 0; y < height; y++) {
      StringBuilder sb = new StringBuilder();
      for (int x = 0; x < width; x++)
        sb.append(bitmap.getPixel(x, y) == colorBg ? FLAG_FALSE : FLAG_TRUE);
      data.add(sb.toString());
    }

    return data;
  }

  private String writeCompressed() {
    StringBuilder sb = new StringBuilder();
    int prev, next = 0, count = 0;

    prev = bitmap.getPixel(0, 0);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        next = bitmap.getPixel(x, y);
        if (next == prev) count++;
        else {
          sb.append(prev == colorFg ? FLAG_TRUE : FLAG_FALSE)
            .append(VALUE_SEP).append(count).append(VALUE_SEP);
          count = 1;
        }

        prev = next;
      }
    }

    sb.append(next == colorFg ? FLAG_TRUE : FLAG_FALSE)
      .append(VALUE_SEP).append(count);
    return sb.toString();
  }

  void save(String path) throws IOException {
    name = path.substring(path.lastIndexOf(File.separator) + 1);
    ArrayList<String> data = new ArrayList<>();
    data.add(SIGNATURE);
    data.add(SECTION_HEADER);
    data.add(String.format("%s %s", FIELD_TYPE,
      compressed ? FLAG_TRUE : FLAG_FALSE));
    data.add(String.format(Locale.ROOT, "%s %d %d",
      FIELD_SIZE, width, height));
    data.add(String.format(Locale.ROOT, "%s %06x %06x",
      FIELD_COLOR, colorBg & 0x00ffffff, colorFg & 0x00ffffff));
    data.add(SECTION_DATA);
    if (compressed) data.add(writeCompressed());
    else data.addAll(writeUncompressed());
    Utils.writeFile(path, data);
  }

  static Image load(String path) throws IOException, ParserException {
    String extension = path.substring(path.lastIndexOf(EXT_SEP));
    if (Arrays.asList(EXT_IMG).contains(extension)) return loadBitmap(path);
    if (Arrays.asList(EXT_TXT).contains(extension)) return loadText(path);
    throw new IOException("Unsupported file format");
  }

  private static Image loadBitmap(String path) throws IOException {
    MyLog.d(LOG_TAG, "Loading bitmap");
    Bitmap src = BitmapFactory.decodeFile(path);
    if (src == null) throw new IOException("Unable to decode file");

    MyLog.d(LOG_TAG, "Converting to grayscale");
    ColorMatrix matrix = new ColorMatrix();
    matrix.setSaturation(0);
    Bitmap dst = Bitmap.createBitmap(src.getWidth(),
      src.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(dst);
    Paint paint = new Paint();
    paint.setColorFilter(new ColorMatrixColorFilter(matrix));
    canvas.drawBitmap(src, 0, 0, paint);
    src.recycle();

    MyLog.d(LOG_TAG, "Indexing colors");
    for (int x = 0; x < dst.getWidth(); x++) {
      for (int y = 0; y < dst.getHeight(); y++) {
        dst.setPixel(x, y, Utils.luma(dst.getPixel(x, y)) > LUMA_THRESHOLD
          ? DEFAULT_BG : DEFAULT_FG);
      }
    }

    MyLog.d(LOG_TAG, "Bitmap data loaded");
    Image image = new Image(dst);
    image.name = path.substring(
      path.lastIndexOf(File.separator) + 1,
      path.lastIndexOf(EXT_SEP)).concat(EXT_TXT[0]);
    return image;
  }

  private static Image loadText(String path)
    throws IOException, ParserException {
    ArrayList<String> src = Utils.readFile(path);
    MyLog.d(LOG_TAG, "Loading image data");
    String signature = src.remove(0);
    if (!signature.equals(SIGNATURE))
      throw new ParserException("Invalid file structure: invalid signature");
    String header = src.remove(0);
    if (!header.equals(SECTION_HEADER))
      throw new ParserException("Invalid file structure: header section expected");

    try {
      Image image = new Image();
      while (!src.isEmpty()) {
        String line = src.remove(0);
        if (line.equals(SECTION_DATA)) break;
        String[] parts = line.split(VALUE_SEP);
        switch (parts[0]) {
          case FIELD_SIZE:
            image.width = Integer.parseInt(parts[1]);
            image.height = Integer.parseInt(parts[2]);
            break;
          case FIELD_TYPE:
            image.compressed = parts[1].equals(FLAG_TRUE);
            break;
          case FIELD_COLOR:
            image.colorBg = Color.parseColor("#".concat(parts[1]));
            image.colorFg = Color.parseColor("#".concat(parts[2]));
            break;
        }
      }

      if (image.width == 0 || image.height == 0)
        throw new IOException("Image size not defined");
      if (image.compressed == null)
        throw new IOException("Image type not defined");

      image.bitmap = Bitmap.createBitmap(image.width,
        image.height, Bitmap.Config.ARGB_8888);
      if (image.compressed) image.loadCompressed(src.get(0));
      else image.loadUncompressed(src);

      image.name = path.substring(path.lastIndexOf(File.separator) + 1,
        path.lastIndexOf(EXT_SEP)).concat(NAME_SUFFIX).concat(EXT_TXT[0]);
      return image;
    } catch (IllegalArgumentException e) {
      throw new ParserException("Invalid file structure: " + e.getMessage());
    }
  }

  static class ParserException extends Exception {
    private ParserException(String msg) { super(msg); }
  }
}
