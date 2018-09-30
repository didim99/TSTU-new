package ru.didim99.tstu.core;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.CallSuper;
import java.lang.ref.WeakReference;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 15.09.18.
 */
public abstract class CallbackTask<Config, Result> extends AsyncTask<Config, Void, Void> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_CallbackTask";

  public enum Event { START, FINISH }

  protected WeakReference<Context> appContext;
  private EventListener<Result> listener;
  private Result result;

  public CallbackTask(Context context) {
    appContext = new WeakReference<>(context);
  }

  public void registerEventListener(EventListener<Result> listener) {
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
  @CallSuper
  protected void onPreExecute() {
    super.onPreExecute();
    if (listener != null)
      listener.onTaskEvent(Event.START, null);
  }

  @Override
  protected final Void doInBackground(Config... configs) {
    if (configs == null) return null;
    MyLog.d(LOG_TAG, "Executing...");
    result = doInBackgroundInternal(configs[0]);
    return null;
  }

  @Override
  @CallSuper
  protected void onPostExecute(Void res) {
    super.onPostExecute(res);
    MyLog.d(LOG_TAG, "Executing completed");
    if (listener != null)
      listener.onTaskEvent(Event.FINISH, result);
    appContext.clear();
    listener = null;
  }

  protected abstract Result doInBackgroundInternal(Config config);

  public interface EventListener<I> {
    void onTaskEvent(Event event, I data);
  }
}
