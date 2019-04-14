package ru.didim99.tstu.ui.dirpicker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.MyLog;

/**
 * directory picker Activity
 * Created by didim99 on 26.01.18.
 */

public class DirPickerActivity extends AppCompatActivity
  implements DirListAdapter.OnItemClickListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_DirPicker";

  public static final class Mode {
    public static final int DIRECTORY = 1;
    public static final int FILE = 2;
    static final int UNDEFINED = 0;
  }

  public static final String KEY_MODE = "DirPickerActivity.mode";
  private static final String KEY_LAST_PATH = "dirPicker.lastPath";
  private static final String SEP = File.separator;

  private Context appContext;
  private SharedPreferences settings;
  private TextView tvPath, tvEmpty;
  private Button btnGo;
  private RecyclerView listDir;
  private DirListAdapter adapter;
  private ArrayList<DirEntry> arrayDir;
  private String path;
  private int mode;

  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "DirPickerActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_dir_picker);
    appContext = getApplicationContext();
    settings = PreferenceManager.getDefaultSharedPreferences(appContext);
    arrayDir = new ArrayList<>();

    //loading saved instance
    State savedState = (State) getLastCustomNonConfigurationInstance();
    if (savedState != null) path = savedState.path;

    //View components init
    tvPath = findViewById(R.id.dirPicker_tvPath);
    tvEmpty = findViewById(R.id.dirPicker_tvEmpty);
    btnGo = findViewById(R.id.dirPicker_go);
    btnGo.setOnClickListener(v -> onClickGo(null));
    adapter = new DirListAdapter(this, this, arrayDir);
    listDir = findViewById(R.id.dirPicker_listDir);
    listDir.setLayoutManager(new LinearLayoutManager(this));
    listDir.setHasFixedSize(true);
    listDir.setAdapter(adapter);

    mode = getIntent().getIntExtra(KEY_MODE, Mode.UNDEFINED);
    if (mode == Mode.FILE) {
      setTitle(R.string.actLabel_filePicker);
      btnGo.setVisibility(View.GONE);
      btnGo.setOnClickListener(null);
    }

    if (path == null)
      path = settings.getString(KEY_LAST_PATH, SEP);
    // Checking access to file system root directory
    if (path.equals(SEP) && !isDirOpened(path)) {
      Toast.makeText(appContext,
        R.string.dirPicker_fsRootUnreadable, Toast.LENGTH_LONG).show();
      path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
      //save new start path in SharedPreferences
      settings.edit().putString(KEY_LAST_PATH, path).apply();
    }

    MyLog.d(LOG_TAG, "DirPickerActivity started");
    updateListDir();
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    return new State(path);
  }

  @Override
  public void onItemClick(DirEntry item) {
    if (item.isLevelUp()) {
      onClickBack();
    } else if (item.isDir()) {
      path = path.concat(item.getName()).concat(SEP);
      updateListDir();
    } else if (mode == Mode.FILE) {
      onClickGo(item.getName());
    }
  }

  private void onClickBack() {
    if (path.equals(SEP)) {
      setResult(RESULT_CANCELED, new Intent());
      finish();
    } else {
      path = new File(path).getParent();
      if (!path.equals(SEP))
        path = path.concat(SEP);
      updateListDir();
    }
  }

  private void onClickGo(String fileName) {
    //save last picked path in SharedPreferences
    settings.edit().putString(KEY_LAST_PATH, path).apply();
    Intent intent = new Intent();
    if (fileName != null) path = path.concat(fileName);
    intent.setData(Uri.parse("file://".concat(path)));
    setResult(RESULT_OK, intent);
    finish();
  }

  private void updateListDir() {
    MyLog.d(LOG_TAG, "Curr path: " + path);
    arrayDir.clear();

    if (!path.equals(SEP)) arrayDir.add(new DirEntry());
    ArrayList<DirEntry> arrayFiles = new ArrayList<>();
    File[] files = new File(path).listFiles();
    boolean dirOpened = files != null;
    btnGo.setEnabled(dirOpened);

    if (dirOpened) {
      if (files.length > 0) {
        tvEmpty.setVisibility(TextView.INVISIBLE);
        Arrays.sort(files);

        for (File file : files) {
          boolean isDir = file.isDirectory();
          DirEntry entry = new DirEntry(file.getName(), isDir);
          if (isDir) arrayDir.add(entry);
          else arrayFiles.add(entry);
        }

        arrayDir.addAll(arrayFiles);
      } else
        tvEmpty.setVisibility(TextView.VISIBLE);
    } else {
      Toast.makeText(appContext,
        R.string.dirPicker_dirUnreadable, Toast.LENGTH_LONG).show();
    }

    adapter.notifyDataSetChanged();
    listDir.scrollToPosition(0);
    tvPath.setText(path);
  }

  private boolean isDirOpened(String dirName) {
    try {
      File[] files = new File(dirName).listFiles();
      for (File file : files) {}
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private static class State {
    private String path;

    State(String path) {
      this.path = path;
    }
  }
}
