package ru.didim99.tstu.core.mp.notice;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 08.03.19.
 */
public class NoticeManager
  implements CallbackTask.EventListener<ArrayList<Notice>> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MPNotice";
  public static final String EXTRA_ID = "noticeID";

  private static final NoticeManager instance = new NoticeManager();

  public static NoticeManager getInstance() { return instance; }
  private NoticeManager() {}

  private boolean initCompleted;
  private WeakReference<Context> appContext;
  private ArrayList<Notice> notices;
  private SimpleDateFormat df;
  private int lastNotifyId;
  private int lastAction;
  private Toast toast;

  public NoticeManager init(Context appContext) {
    if (initCompleted) return this;
    df = new SimpleDateFormat("dd MMM yyyy HH:mm", new Locale("ru"));
    this.toast = Toast.makeText(appContext, "", Toast.LENGTH_LONG);
    this.appContext = new WeakReference<>(appContext);
    this.notices = new ArrayList<>();
    startTask(new Action(Action.Type.LOAD));
    this.initCompleted = true;
    this.lastNotifyId = 100;
    return this;
  }

  public SimpleDateFormat getDateFormat() {
    return df;
  }

  public boolean hasAnyNotices() {
    return initCompleted && !notices.isEmpty();
  }

  public String[] getList() {
    if (!initCompleted) return null;
    String[] list = new String[notices.size()];
    for (int i = 0; i < list.length; i++)
      list[i] = notices.get(i).toString(df);
    return list;
  }

  public Notice get(int id) {
    if (!initCompleted) return null;
    if (id >= notices.size()) return null;
    return notices.get(id);
  }

  public void add(Notice notice) {
    if (!initCompleted) return;
    startTask(new Action(Action.Type.ADD, notice));
  }

  public void delete(Notice notice) {
    if (!initCompleted) return;
    startTask(new Action(Action.Type.DELETE, notice));
  }

  public void send(Notice notice) {
    MyLog.d(LOG_TAG, "Creating notification...");
    Context ctx = appContext.get();

    Notification n = new NotificationCompat.Builder(ctx)
      .setColor(ctx.getResources().getColor(R.color.colorPrimaryDark))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setDefaults(Notification.DEFAULT_ALL)
      .setSmallIcon(R.drawable.ic_info_24dp)
      .setContentTitle(notice.getHeader())
      .setContentText(notice.getText())
      .setTicker(notice.getHeader())
      .setWhen(notice.getDatetime())
      .setAutoCancel(true)
      .build();

    MyLog.d(LOG_TAG, "Notification created");
    NotificationManager manager = (NotificationManager)
      ctx.getSystemService(Context.NOTIFICATION_SERVICE);
    if (manager != null) manager.notify(lastNotifyId++, n);
    MyLog.d(LOG_TAG, "Notification sent");
  }

  private void startTask(Action action) {
    lastAction = action.getType();
    NoticeTask task = new NoticeTask(appContext.get());
    task.registerEventListener(this);
    task.execute(action);
  }

  @Override
  public void onTaskEvent(CallbackTask.Event event, ArrayList<Notice> data) {
    if (event == CallbackTask.Event.START) return;
    MyLog.d(LOG_TAG, "Background task finished");
    switch (lastAction) {
      case Action.Type.LOAD:
        this.notices = data;
        break;
      case Action.Type.ADD:
        Notice newNotice = data.get(0);
        toast.setText(appContext.get().getString(
          R.string.mp_added, newNotice.getId()));
        notices.add(newNotice);
        toast.show();
        break;
      case Action.Type.DELETE:
        Notice notice = data.get(0);
        toast.setText(appContext.get().getString(
          R.string.mp_deleted, notice.getId()));
        notices.remove(notice);
        toast.show();
        break;
    }
  }
}
