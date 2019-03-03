package ru.didim99.tstu.core.mp.students;

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
public class StudentTask extends CallbackTask<Action, ArrayList<Student>> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_mpST";

  private boolean success;

  public StudentTask(Context context) {
    super(context);
    success = true;
  }

  @Override
  protected ArrayList<Student> doInBackgroundInternal(Action action) {
    ArrayList<Student> students = null;
    SQLiteDatabase db;

    MyLog.d(LOG_TAG, "Connecting to local database...");
    DBHelper dbHelper = new DBHelper(appContext.get());
    try {
      db = dbHelper.getWritableDatabase();
    } catch (SQLiteException ex) {
      MyLog.e(LOG_TAG, "Can't open local database for writing\n  " + ex.getMessage());
      if (action.getType() == Action.Type.VIEW)
        db = dbHelper.getReadableDatabase();
      else {
        success = false;
        return null;
      }
    }

    switch (action.getType()) {
      case Action.Type.VIEW:
        students = getAllRows(db);
        break;
      case Action.Type.ADD:
        students = addRow(db, action.getStudent());
        break;
      case Action.Type.REPLACE:
        students = rewriteLast(db);
        break;
    }

    MyLog.d(LOG_TAG, "Closing database...");
    dbHelper.close();
    return students;
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

  private ArrayList<Student> getAllRows(SQLiteDatabase db) {
    MyLog.d(LOG_TAG, "Getting all students");
    ArrayList<Student> students = new ArrayList<>();
    Cursor cursor = db.query(DBHelper.TABLE_STUDENTS,
      null, null, null,
      null, null, DBHelper.KEY_ID);
    if (cursor.moveToFirst()) {
      while (!cursor.isAfterLast()) {
        Student student = new Student();
        student.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_ID)));
        student.setName(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_NAME)));
        student.setDatetime(cursor.getLong(cursor.getColumnIndex(DBHelper.KEY_DATE)));
        students.add(student);
        cursor.moveToNext();
      }
    }

    cursor.close();
    return students;
  }

  private ArrayList<Student> addRow(SQLiteDatabase db, Student student) {
    MyLog.d(LOG_TAG, "Adding new student");
    ContentValues newRow = new ContentValues();
    newRow.put(DBHelper.KEY_NAME, student.getName());
    newRow.put(DBHelper.KEY_DATE, System.currentTimeMillis());
    db.insert(DBHelper.TABLE_STUDENTS, null, newRow);
    Cursor cursor = db.rawQuery(DBHelper.SQL_LAST_STUDENT, null);
    ArrayList<Student> students = new ArrayList<>();
    cursor.moveToFirst();
    student.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_ID)));
    students.add(student);
    cursor.close();
    return students;
  }

  private ArrayList<Student> rewriteLast(SQLiteDatabase db) {
    MyLog.d(LOG_TAG, "Updating last student");
    ArrayList<Student> students = new ArrayList<>();
    Cursor cursor = db.rawQuery(DBHelper.SQL_LAST_STUDENT, null);
    Student student = new Student();
    if (cursor.moveToFirst()) {
      student.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_ID)));
      cursor.close();
      ContentValues replacement = new ContentValues();
      replacement.put(DBHelper.KEY_NAME, appContext.get()
        .getString(R.string.mp_defaultName));
      db.update(DBHelper.TABLE_STUDENTS, replacement,
        DBHelper.SQL_ID_EQUALS, new String[] {String.valueOf(student.getId())});
    }
    students.add(student);
    return students;
  }
}
