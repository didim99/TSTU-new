package ru.didim99.tstu.core.mp.notice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.Toast;
import java.util.ArrayList;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.mp.DBHelper;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 26.02.19.
 */
public class NoticeTask extends CallbackTask<Action, ArrayList<Notice>> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_mpNT";

  private boolean success;

  NoticeTask(Context context) {
    super(context);
    success = true;
  }

  @Override
  protected ArrayList<Notice> doInBackgroundInternal(Action action) {
    ArrayList<Notice> notices = null;
    SQLiteDatabase db;

    MyLog.d(LOG_TAG, "Connecting to local database...");
    DBHelper dbHelper = new DBHelper(appContext.get());
    try {
      db = dbHelper.getWritableDatabase();
    } catch (SQLiteException ex) {
      MyLog.e(LOG_TAG, "Can't open local database for writing\n  " + ex.getMessage());
      if (action.getType() == Action.Type.LOAD)
        db = dbHelper.getReadableDatabase();
      else {
        success = false;
        return null;
      }
    }

    switch (action.getType()) {
      case Action.Type.LOAD:
        notices = getAllRows(db);
        break;
      case Action.Type.ADD:
        notices = addRow(db, action.getNotice());
        break;
      case Action.Type.DELETE:
        notices = deleteRow(db, action.getNotice());
        break;
    }

    MyLog.d(LOG_TAG, "Closing database...");
    dbHelper.close();
    return notices;
  }

  @Override
  protected void onPostExecute(Void res) {
    if (!success) {
      Toast.makeText(appContext.get(), R.string.mp_dbError,
        Toast.LENGTH_LONG).show();
      listener = null;
    }
    super.onPostExecute(res);
  }

  private ArrayList<Notice> getAllRows(SQLiteDatabase db) {
    MyLog.d(LOG_TAG, "Loading all notices");
    ArrayList<Notice> notices = new ArrayList<>();
    Cursor cursor = db.query(DBHelper.TABLE_NOTICE,
      null, null, null,
      null, null, DBHelper.KEY_ID);
    if (cursor.moveToFirst()) {
      while (!cursor.isAfterLast()) {
        Notice notice = new Notice();
        notice.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_ID)));
        notice.setHeader(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_HEADER)));
        notice.setText(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_TEXT)));
        notice.setDatetime(cursor.getLong(cursor.getColumnIndex(DBHelper.KEY_DATE)));
        notices.add(notice);
        cursor.moveToNext();
      }
    }

    MyLog.d(LOG_TAG, "Data loaded");
    cursor.close();
    return notices;
  }

  private ArrayList<Notice> addRow(SQLiteDatabase db, Notice notice) {
    MyLog.d(LOG_TAG, "Adding new notice");
    ContentValues newRow = new ContentValues();
    newRow.put(DBHelper.KEY_HEADER, notice.getHeader());
    newRow.put(DBHelper.KEY_TEXT, notice.getText());
    newRow.put(DBHelper.KEY_DATE, notice.getDatetime());
    db.insert(DBHelper.TABLE_NOTICE, null, newRow);
    Cursor cursor = db.rawQuery(DBHelper.SQL_LAST_NOTICE, null);
    ArrayList<Notice> notices = new ArrayList<>();
    cursor.moveToFirst();
    notice.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_ID)));
    MyLog.d(LOG_TAG, "New row added");
    notices.add(notice);
    cursor.close();
    return notices;
  }

  private ArrayList<Notice> deleteRow(SQLiteDatabase db, Notice notice) {
    MyLog.d(LOG_TAG, "Deleting notice: " + notice.getId());
    int count = db.delete(DBHelper.TABLE_NOTICE, DBHelper.SQL_ID_EQUALS,
      new String[] {String.valueOf(notice.getId())});
    MyLog.d(LOG_TAG, "Rows deleted: " + count);
    ArrayList<Notice> notices = new ArrayList<>();
    notices.add(notice);
    return notices;
  }
}
