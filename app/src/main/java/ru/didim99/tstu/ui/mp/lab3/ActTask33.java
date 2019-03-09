package ru.didim99.tstu.ui.mp.lab3;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.mp.notice.Notice;
import ru.didim99.tstu.core.mp.notice.NoticeManager;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.MyLog;

public class ActTask33 extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MP";

  private NoticeManager manager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_l3_t3_p3);
    manager = NoticeManager.getInstance();

    int id = getIntent().getIntExtra(
      NoticeManager.EXTRA_ID, 0);
    Notice notice = manager.get(id);
    SimpleDateFormat df = manager.getDateFormat();
    String text = String.format("%s: %s\n\n%s",
      df.format(new Date(notice.getDatetime())),
      notice.getHeader(), notice.getText());

    findViewById(R.id.mp_btnDelete)
      .setOnClickListener(v -> deleteDialog(notice));
    ((TextView) findViewById(R.id.tvFullText)).setText(text);
    manager.send(notice);
  }

  private void deleteDialog(Notice notice) {
    MyLog.d(LOG_TAG, "Delete dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(getString(R.string.mp_notice_confirmDelete, notice.getHeader()));
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    adb.setPositiveButton(R.string.dialogButtonOk,
      (dialog, which) -> manager.delete(notice.getId()));
    MyLog.d(LOG_TAG, "Delete dialog created");
    adb.create().show();
  }
}
