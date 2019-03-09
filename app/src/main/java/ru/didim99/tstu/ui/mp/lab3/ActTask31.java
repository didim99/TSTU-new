package ru.didim99.tstu.ui.mp.lab3;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.mp.notice.NoticeManager;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.MyLog;

public class ActTask31 extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MP";

  private NoticeManager manager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_l3_t3_p1);
    manager = NoticeManager.getInstance().init(getApplication());
    findViewById(R.id.mp_btnAdd).setOnClickListener(v ->
      startActivity(new Intent(this, ActTask32.class)));
    findViewById(R.id.mp_btnView).setOnClickListener(v -> viewDialog());
  }

  private void viewDialog() {
    if (!manager.hasAnyNotices()) return;
    MyLog.d(LOG_TAG, "View dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.mp_notice_selectToView);
    adb.setItems(manager.getList(), ((dialog, pos) -> onView(pos)));
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    MyLog.d(LOG_TAG, "View dialog created");
    adb.create().show();
  }

  private void onView(int id) {
    Intent intent = new Intent(this, ActTask33.class);
    intent.putExtra(NoticeManager.EXTRA_ID, id);
    startActivity(intent);
  }
}
