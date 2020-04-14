package ru.didim99.tstu.ui.mp.lab3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.File;
import java.util.Arrays;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.mp.JournalTask;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

public class ActTask1 extends BaseActivity
  implements CallbackTask.EventListener<JournalTask.Result> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MP";
  private static final String JOURNAL_DIR = "mp_journal";

  // View-elements
  private Button btnDownload;
  private Button btnDelete;
  private Button btnView;
  private Toast toast;
  // Workflow
  private JournalTask task;
  private boolean hasFiles;
  private String[] files;
  private String dirName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "JournalActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_l3_t1);

    MyLog.d(LOG_TAG, "View components init...");
    toast = Toast.makeText(getApplication(), "", Toast.LENGTH_LONG);
    btnDownload = findViewById(R.id.mp_btnDownload);
    btnDelete = findViewById(R.id.mp_btnDelete);
    btnView = findViewById(R.id.mp_btnView);

    btnDownload.setOnClickListener(v -> inputFieldDialog(
      R.string.mp_enterJournalId, R.layout.dia_sf_number, this::onDownload));
    btnDelete.setOnClickListener(v -> fileDialog(
      R.string.mp_journal_selectForDelete, this::deleteDialog));
    btnView.setOnClickListener(v -> fileDialog(
      R.string.mp_journal_selectFile, this::onOpen));
    MyLog.d(LOG_TAG, "View components init completed");

    dirName = getExternalCacheDir().getAbsolutePath()
      .concat(File.separator).concat(JOURNAL_DIR);
    File baseDir = new File(dirName);
    if (!baseDir.exists() && !baseDir.mkdirs()) {
      MyLog.e(LOG_TAG, "Can't create target dir");
      toast.setText(R.string.mp_journal_cantCreateDirs);
      toast.show();
      finish();
    }

    MyLog.d(LOG_TAG, "Trying to connect with background task...");
    task = (JournalTask) getLastCustomNonConfigurationInstance();
    if (task == null) {
      MyLog.d(LOG_TAG, "No existing background task found");
      updateLocalState();
      uiLock(false);
    } else {
      task.registerEventListener(this);
      MyLog.d(LOG_TAG, "Connecting to background task completed "
        + "(" + task.hashCode() + ")");
    }

    MyLog.d(LOG_TAG, "JournalActivity started");
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    if (task != null)
      task.unregisterEventListener();
    return task;
  }

  @Override
  public void onTaskEvent(CallbackTask.Event event, JournalTask.Result result) {
    switch (event) {
      case START:
        uiLock(true);
        toast.setText(R.string.mp_downloadStarted);
        toast.show();
        break;
      case FINISH:
        switch (result.getStatus()) {
          case JournalTask.Status.SUCCESS:
            updateLocalState();
            toast.setText(getString(
              R.string.mp_downloadFinished, result.getPath()));
            break;
          case JournalTask.Status.NO_INTERNET:
            toast.setText(R.string.mp_downloadFailed_noInternet);
            break;
          case JournalTask.Status.NOT_FOUND:
            toast.setText(R.string.mp_downloadFailed_notFound);
            break;
          case JournalTask.Status.SAVE_FAILED:
            toast.setText(R.string.mp_downloadFailed_localIO);
            break;
        }
        uiLock(false);
        toast.show();
        break;
    }
  }

  private void fileDialog(@StringRes int titleId, ListEventListener listener) {
    MyLog.d(LOG_TAG, "File dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(titleId).setItems(files,
      ((dialog, pos) -> listener.onEvent(pos)));
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    MyLog.d(LOG_TAG, "File dialog created");
    adb.create().show();
  }

  private void deleteDialog(int id) {
    MyLog.d(LOG_TAG, "Delete dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(getString(R.string.mp_journal_confirmDelete, files[id]));
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    adb.setPositiveButton(R.string.dialogButtonOk,
      (dialog, which) -> onDelete(id));
    MyLog.d(LOG_TAG, "Delete dialog created");
    adb.create().show();
  }

  private void onDownload(AlertDialog dialog) {
    try {
      EditText input = dialog.findViewById(R.id.etInput);
      int id = InputValidator.getInstance().checkInteger(input, 1,
        R.string.errJournal_emptyId, R.string.errJournal_incorrectId, "ID");
      task = new JournalTask(this, dirName);
      task.registerEventListener(this);
      task.execute(id);
      dialog.dismiss();
    } catch (InputValidator.ValidationException ignored) {}
  }

  private void onOpen(int id) {
    File file = new File(dirName, files[id]);
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setDataAndType(Uri.fromFile(file), "application/pdf");
    if (Utils.isIntentSafe(this, intent))
      startActivity(intent);
    else {
      toast.setText(R.string.mp_journal_noViewerFound);
      toast.show();
    }
  }

  private void onDelete(int id) {
    File file = new File(dirName, files[id]);
    MyLog.d(LOG_TAG, "Deleting: " + file);
    if (file.delete()) {
      updateLocalState();
      uiLock(false);
      toast.setText(R.string.mp_journal_deleted);
      toast.show();
    }
  }

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    btnDownload.setEnabled(!state);
    btnDelete.setEnabled(!state & hasFiles);
    btnView.setEnabled(!state & hasFiles);

    if (state) MyLog.d(LOG_TAG, "UI locked");
    else MyLog.d(LOG_TAG, "UI unlocked");
  }

  private void updateLocalState() {
    String[] list = new File(dirName).list();
    hasFiles = list != null && list.length > 0;
    if (!hasFiles) return;
    Arrays.sort(list);
    files = list;
  }

  private interface ListEventListener {
    void onEvent(int pos);
  }
}
