package ru.didim99.tstu.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.translator.Translator;
import ru.didim99.tstu.core.translator.TranslatorTask;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

public class TranslatorActivity extends AppCompatActivity
  implements TranslatorTask.EventListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_TransAct";
  private static final int REQUEST_GET_FILE = 1;

  //view-elements
  private View dataLayout, pbMain;
  private Button btnPickFile, btnStart;
  private TextView tvSrc, tvOut;
  private EditText etStartPath;
  private Toast toastMsg;
  //main workflow
  private boolean uiLocked;
  private TranslatorTask task;
  private Translator.Result taskResult;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "TranslatorActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_translator);
    setupActionBar();

    MyLog.d(LOG_TAG, "View components init...");
    toastMsg = Toast.makeText(this, "", Toast.LENGTH_LONG);
    dataLayout = findViewById(R.id.dataLayout);
    etStartPath = findViewById(R.id.etInputPath);
    btnPickFile = findViewById(R.id.btnOpenFileExp);
    btnStart = findViewById(R.id.btnStart);
    tvSrc = findViewById(R.id.tvSrc);
    tvOut = findViewById(R.id.tvOut);
    pbMain = findViewById(R.id.pbMain);
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "Trying to connect with background task...");
    task = (TranslatorTask) getLastCustomNonConfigurationInstance();
    if (task == null) {
      MyLog.d(LOG_TAG, "No existing background task found");
    } else {
      task.registerEventListener(this);
      MyLog.d(LOG_TAG, "Connecting to background task completed "
        + "(" + task.hashCode() + ")");
    }

    btnPickFile.setOnClickListener(v -> openFileExp());
    btnStart.setOnClickListener(v -> startTask());
    MyLog.d(LOG_TAG, "TranslatorActivity started");
  }

  @Override
  public TranslatorTask onRetainCustomNonConfigurationInstance() {
    if (task != null) {
      task.unregisterEventListener();
      return task;
    } else
      return null;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent i) {
    if (requestCode == REQUEST_GET_FILE) {
      if (resultCode == RESULT_OK) {
        Uri data = i.getData();
        if (data != null) {
          String scheme = data.getScheme();
          String extPath = data.getPath();
          /*if (!scheme.equals("file") && !(new File(extPath).exists())) {
            MyLog.e(LOG_TAG, "Can't load file/dir. Unsupported scheme: " + scheme);
            if (++loadCount < MAX_LOAD_COUNT) {
              toastMsg.setText(R.string.unsupportedScheme);
              toastMsg.show();
              return;
            } else {
              internalExplorerDialog();
              loadCount = 0;
              return;
            }
          }*/

          etStartPath.setText(extPath);
          etStartPath.setSelection(extPath.length());
          MyLog.d(LOG_TAG, "File/dir was successfully loaded:" +
            "\n  Path: " + extPath);
        }
      }

      else if (resultCode == RESULT_CANCELED) {
        MyLog.d(LOG_TAG, "Choosing path aborted");
      }
    }
  }

  @Override
  public void onBackPressed() {
    if (!uiLocked)
      super.onBackPressed();
  }

  @Override
  public void onTaskEvent(int event, Translator.Result result) {
    switch (event) {
      case TranslatorTask.Event.START:
        uiLock(true);
        break;
      case TranslatorTask.Event.FINISH:
        this.taskResult = result;
        uiLock(false);
        break;
    }
  }

  private void startTask() {
    String path = etStartPath.getText().toString();
    if (checkPath(path)) {
      task = new TranslatorTask(getApplicationContext(),
        new Translator.Config(Translator.Mode.LEXICAL, path));
      task.registerEventListener(this);
      task.execute();
    }
  }

  private void openFileExp() {
    /*if (Settings.ResConverter.isUseInternalExplorer()) {
      MyLog.d(LOG_TAG, "Choose file from DirPicker...");
      Intent intent = new Intent(this, DirPickerActivity.class);
      intent.putExtra(DirPickerActivity.KEY_MODE, DirPickerActivity.MODE_FILE);
      startActivityForResult(intent, REQUEST_CHOOSE_DIR);
    } else {*/
      MyLog.d(LOG_TAG, "Choose file from external file manager");
      Intent getFileIntent = new Intent();
      getFileIntent.setAction(Intent.ACTION_GET_CONTENT);
      getFileIntent.setType("file/*");

      if (Utils.isIntentSafe(this, getFileIntent)) {
        MyLog.d(LOG_TAG, "Calling external file manager...");
        startActivityForResult(getFileIntent, REQUEST_GET_FILE);
      } else
        MyLog.e(LOG_TAG, "No any external file manager found");
    /*}*/
  }

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    btnPickFile.setEnabled(!state);
    btnStart.setEnabled(!state);
    etStartPath.setEnabled(!state);

    if (state) {
      MyLog.d(LOG_TAG, "Clearing UI...");
      tvSrc.setText(null);
      tvOut.setText(null);
      MyLog.d(LOG_TAG, "UI cleared");
      dataLayout.setVisibility(View.INVISIBLE);
      pbMain.setVisibility(View.VISIBLE);
      MyLog.d(LOG_TAG, "UI locked");
    }
    else {
      dataLayout.setVisibility(View.VISIBLE);
      pbMain.setVisibility(View.INVISIBLE);
      MyLog.d(LOG_TAG, "UI unlocked");
      uiSet();
    }
  }

  void uiSet() {
    MyLog.d(LOG_TAG, "Setting up UI");
    tvSrc.setText(taskResult.hasInputCode() ?
      taskResult.getInputCode() : taskResult.getInputErr());
    tvOut.setText(taskResult.hasOutputCode() ?
      taskResult.getOutputCode() : taskResult.getProcessErr());
    MyLog.d(LOG_TAG, "UI setup completed");
  }

  private boolean checkPath(String path) {
    MyLog.d(LOG_TAG, "Path checking: " + path);
    String logMsg = "Path check failed: ";
    String fileStatus = "";
    boolean flag = false;

    if (path.equals(""))
      fileStatus = getString(R.string.filePicker_emptyPath);
    else {
      File file = new File(path);

      if (!file.exists())
        fileStatus = getString(R.string.filePicker_fileNotExist);
      else if (file.isDirectory())
        fileStatus = getString(R.string.filePicker_fileIsDir);
      else if (!file.canRead()) {
        fileStatus = getString(R.string.filePicker_fileNotReadable);
      } else if (!file.canWrite())
        fileStatus = getString(R.string.filePicker_fileNotWritable);
      else flag = true;
    }

    if (flag)
      MyLog.d(LOG_TAG, "Path check done");
    else {
      MyLog.e(LOG_TAG, logMsg + fileStatus);
      toastMsg.setText(getString(R.string.filePicker_error, fileStatus));
      toastMsg.show();
    }
    return flag;
  }

  private void setupActionBar() {
    ActionBar bar = getSupportActionBar();
    if (bar != null) {
      bar.setDisplayShowHomeEnabled(true);
      bar.setDisplayHomeAsUpEnabled(true);
    }
  }
}
