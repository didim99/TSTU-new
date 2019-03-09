package ru.didim99.tstu.core.mp;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 08.03.19.
 */
public class JournalTask extends CallbackTask<Integer, JournalTask.Result> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_mpJT";
  private static final String FILE_MANE = "%03d.pdf";

  private String dirName;

  public JournalTask(Context context, String dirName) {
    super(context);
    this.dirName = dirName;
  }

  @Override
  protected Result doInBackgroundInternal(Integer id) {
    try {
      MyLog.d(LOG_TAG, "Downloading journal: " + id);
      OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS).build();
      Response<ResponseBody> response = new Retrofit.Builder()
        .baseUrl(WebAPI.URL_JOURNAL).client(client).build()
        .create(WebAPI.class).getJournal(id).execute();
      if (!response.isSuccessful()) {
        MyLog.w(LOG_TAG, "Request failed: " + response);
        return new Result(Status.NOT_FOUND);
      }

      ResponseBody data = response.body();
      if (data == null || data.contentLength() == 0) {
        MyLog.w(LOG_TAG, "Request failed: server return null");
        return new Result(Status.NOT_FOUND);
      }

      MyLog.d(LOG_TAG, "Data loaded: " + data.contentType()
        + " [" + data.contentLength() + "]");
      String fileName = String.format(Locale.US, FILE_MANE, id);
      File target = new File(dirName, fileName);
      fileName = target.getAbsolutePath();

      try {
        MyLog.d(LOG_TAG, "Writing: " + fileName);
        FileOutputStream fos = new FileOutputStream(target);
        fos.write(data.bytes());
        fos.close();
      } catch (IOException e) {
        MyLog.w(LOG_TAG, "Can;t write file: " + e);
        return new Result(Status.SAVE_FAILED);
      }

      MyLog.d(LOG_TAG, "Download finished");
      return new Result(Status.SUCCESS, fileName);
    } catch (IOException e) {
      MyLog.w(LOG_TAG, "Download failed: " + e);
      return new Result(Status.NO_INTERNET);
    }
  }

  public static final class Status {
    public static final int SUCCESS = 0;
    public static final int NO_INTERNET = 1;
    public static final int NOT_FOUND = 2;
    public static final int SAVE_FAILED = 3;
  }

  public static class Result {
    private int status;
    private String path;

    private Result(int status) {
      this(status, null);
    }

    private Result(int status, String path) {
      this.status = status;
      this.path = path;
    }

    public int getStatus() { return status; }
    public String getPath() { return path; }
  }
}
