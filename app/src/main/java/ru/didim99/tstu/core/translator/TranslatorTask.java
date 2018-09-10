package ru.didim99.tstu.core.translator;

import android.content.Context;
import android.os.AsyncTask;
import java.lang.ref.WeakReference;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 10.09.18.
 */
public class TranslatorTask extends AsyncTask<Void, Void, Void> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_TranslatorTask";

  public static final class Event {
    public static final int START = 1;
    public static final int FINISH = 2;
  }

  private WeakReference<Context> appContext;
  private EventListener listener;
  private Translator.Config config;
  private Translator.Result result;

  public TranslatorTask(Context context, Translator.Config config) {
    appContext = new WeakReference<>(context);
    this.config = config;
  }

  public void registerEventListener(EventListener listener) {
    this.listener = listener;
    switch (getStatus()) {
      case RUNNING:
        listener.onTaskEvent(Event.START, null);
        break;
      case FINISHED:
        listener.onTaskEvent(Event.FINISH, result);
        break;
    }
  }

  public void unregisterEventListener() {
    this.listener = null;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    if (listener != null)
      listener.onTaskEvent(Event.START, null);
  }

  @Override
  protected Void doInBackground(Void... voids) {
    MyLog.d(LOG_TAG, "Executing...");
    result = new Translator(appContext.get(), config).translate();
    return null;
  }

  @Override
  protected void onPostExecute(Void res) {
    super.onPostExecute(res);
    MyLog.d(LOG_TAG, "Executing completed");
    if (listener != null)
      listener.onTaskEvent(Event.FINISH, result);
    listener = null;
  }

  public interface EventListener {
    void onTaskEvent(int event, Translator.Result result);
  }
}
