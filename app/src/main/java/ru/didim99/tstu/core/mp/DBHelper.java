package ru.didim99.tstu.core.mp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 26.02.19.
 */
public class DBHelper extends SQLiteOpenHelper {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_mpDB";

  // Internal DB info
  private static final int DB_VERSION = 3;
  private static final String DB_NAME = "mp";
  // Table names
  public static final String TABLE_STUDENTS = "students";
  public static final String TABLE_RADIO = "radio";
  public static final String TABLE_NOTICE = "notice";
  // Field names
  public static final String KEY_ID = "id";
  public static final String KEY_NAME = "name";
  public static final String KEY_SINGER = "singer";
  public static final String KEY_DATE = "date";
  public static final String KEY_HEADER = "header";
  public static final String KEY_TEXT = "text";
  // prepared queries
  public static final String SQL_ID_EQUALS = "id = ?";
  public static final String SQL_LAST_STUDENT =
    "SELECT " + KEY_ID + " FROM " + TABLE_STUDENTS
      + " ORDER BY " + KEY_DATE + " DESC LIMIT 1;";
  public static final String SQL_LAST_SONG =
    "SELECT * FROM " + TABLE_RADIO
      + " ORDER BY " + KEY_DATE + " DESC LIMIT 1;";
  public static final String SQL_LAST_NOTICE =
    "SELECT * FROM " + TABLE_NOTICE
      + " ORDER BY " + KEY_ID + " DESC LIMIT 1;";
  private static final String SQL_CREATE_RADIO =
    "CREATE TABLE " + TABLE_RADIO + " ("
      + KEY_ID + " integer PRIMARY KEY AUTOINCREMENT, "
      + KEY_SINGER + " varchar(64), "
      + KEY_NAME + " varchar(128), "
      + KEY_DATE + " long"
      + ");";
  private static final String SQL_CREATE_NOTICE =
    "CREATE TABLE " + TABLE_NOTICE + " ("
      + KEY_ID + " integer PRIMARY KEY AUTOINCREMENT, "
      + KEY_HEADER + " varchar(64), "
      + KEY_TEXT + " varchar(256), "
      + KEY_DATE + " long"
      + ");";

  public DBHelper(@Nullable Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    MyLog.d(LOG_TAG, "Creating new database...");
    db.execSQL(
      "CREATE TABLE " + TABLE_STUDENTS + " ("
        + KEY_ID + " integer PRIMARY KEY AUTOINCREMENT, "
        + KEY_NAME + " varchar(64), "
        + KEY_DATE + " long"
        + ");"
    );
    db.execSQL(SQL_CREATE_RADIO);
    db.execSQL(SQL_CREATE_NOTICE);
    MyLog.d(LOG_TAG, "Database created");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    MyLog.d(LOG_TAG, "Upgrading database: "+ oldVersion + " -> " + newVersion);
    if (newVersion == 2) db.execSQL(SQL_CREATE_RADIO);
    if (newVersion == 3) db.execSQL(SQL_CREATE_NOTICE);
  }
}
