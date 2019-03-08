package ru.didim99.tstu.core.mp.radio;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.widget.Toast;
import com.google.gson.Gson;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.mp.DBHelper;
import ru.didim99.tstu.core.mp.WebAPI;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 01.03.19.
 */
public class RadioTask extends AsyncTask<Void, Void, Integer> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_mpRT";
  private static final int TIMEOUT = 22000;

  private final class Status {
    private static final int OK = 0;
    private static final int NO_INTERNET = 1;
    private static final int DB_UNAVAILANBLE = 2;
  }

  // Application level
  private boolean running;
  private WeakReference<Context> appContext;
  private OnStatUpdateListener listener;
  // DB & Network
  private DBHelper dbHelper;
  private SQLiteDatabase db;
  private WebAPI webAPI;
  private Gson gson;
  // State
  private ArrayList<Song> stat;
  private Song lastSong;

  public RadioTask(Context appContext) {
    this.appContext = new WeakReference<>(appContext);
    this.gson = new Gson();
    this.stat = new ArrayList<>();
    OkHttpClient client = new OkHttpClient.Builder()
      .connectTimeout(5, TimeUnit.SECONDS)
      .writeTimeout(5, TimeUnit.SECONDS).build();
    webAPI = new Retrofit.Builder().baseUrl(WebAPI.URL_RADIO)
      .client(client).build().create(WebAPI.class);
  }

  public void setOnStatUpdateListener(OnStatUpdateListener listener) {
    this.listener = listener;
    if (getStatus() == AsyncTask.Status.RUNNING)
      onProgressUpdate();
  }

  @Override
  protected void onPreExecute() {
    MyLog.d(LOG_TAG, "Service started");
    running = true;
  }

  @Override
  protected Integer doInBackground(Void... voids) {
    try {
      MyLog.d(LOG_TAG, "Connecting to local database...");
      dbHelper = new DBHelper(appContext.get());
      db = dbHelper.getWritableDatabase();
    } catch (SQLiteException e) {
      MyLog.e(LOG_TAG, "Can't open local database for writing\n  " + e);
      return Status.DB_UNAVAILANBLE;
    }

    loadSavedStat();
    if (lastSong != null)
      publishProgress();
    if (!checkInternetConnection())
      return Status.NO_INTERNET;

    try {
      Song newSong;
      RadioResponse response;
      Response<ResponseBody> rawResponse;
      ResponseBody body;

      while (running) {
        MyLog.d(LOG_TAG, "Updating statistics...");
        rawResponse = webAPI.getSong(WebAPI.API_LOGIN, WebAPI.API_PASSW).execute();
        body = rawResponse.body();
        if (!rawResponse.isSuccessful()) return Status.NO_INTERNET;
        if (body == null) return Status.NO_INTERNET;
        response = gson.fromJson(body.string(), RadioResponse.class);
        if (response.isSuccessful()) newSong = new Song(response);
        else return Status.NO_INTERNET;
        if (!newSong.equals(lastSong)) {
          cacheSong(newSong);
          publishProgress();
        } else
          MyLog.d(LOG_TAG, "Nothing to update");
        Thread.sleep(TIMEOUT);
      }

    } catch (IOException e) {
      return Status.NO_INTERNET;
    } catch (InterruptedException e) {
      return Status.OK;
    }

    return Status.OK;
  }

  @Override
  protected void onProgressUpdate(Void... values) {
    if (listener != null) listener.onStatUpdated(stat);
  }

  @Override
  protected void onPostExecute(Integer status) {
    MyLog.d(LOG_TAG, "Service stopped");
    if (db != null && db.isOpen()) dbHelper.close();
    if (status != Status.OK) {
      switch (status) {
        case Status.NO_INTERNET: status = R.string.mp_offlineMode; break;
        case Status.DB_UNAVAILANBLE: status = R.string.mp_dbError; break;
      }
      Toast.makeText(appContext.get(), status, Toast.LENGTH_LONG).show();
    }
  }

  private boolean checkInternetConnection() {
    MyLog.d(LOG_TAG, "Checking internet connection...");
    try {
      Response<ResponseBody> response =
        webAPI.getSong(WebAPI.API_LOGIN, WebAPI.API_PASSW).execute();
      return response.isSuccessful();
    } catch (IOException e) {
      return false;
    }
  }

  private void loadSavedStat() {
    MyLog.d(LOG_TAG, "Loading saved statistics...");
    Cursor cursor = db.query(DBHelper.TABLE_RADIO, null,
      null, null, null, null, DBHelper.KEY_DATE);
    if (cursor.moveToFirst()) {
      while (!cursor.isAfterLast()) {
        updateLast(cursor);
        cursor.moveToNext();
      }
    }
    cursor.close();
  }

  private void cacheSong(Song song) {
    MyLog.d(LOG_TAG, "Adding new song: " + song);
    ContentValues newRow = new ContentValues();
    newRow.put(DBHelper.KEY_SINGER,song.getSinger());
    newRow.put(DBHelper.KEY_NAME,song.getName());
    newRow.put(DBHelper.KEY_DATE, song.getDate());
    db.insert(DBHelper.TABLE_RADIO, null, newRow);
    Cursor cursor = db.rawQuery(DBHelper.SQL_LAST_SONG, null);
    cursor.moveToFirst();
    updateLast(cursor);
    cursor.close();
  }

  private void updateLast(Cursor cursor) {
    lastSong = new Song();
    lastSong.setId(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_ID)));
    lastSong.setSinger(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_SINGER)));
    lastSong.setName(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_NAME)));
    lastSong.setDate(cursor.getLong(cursor.getColumnIndex(DBHelper.KEY_DATE)));
    stat.add(lastSong);
  }

  public interface OnStatUpdateListener {
    void onStatUpdated(ArrayList<Song> songList);
  }
}
