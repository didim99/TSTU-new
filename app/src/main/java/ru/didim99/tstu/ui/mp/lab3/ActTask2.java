package ru.didim99.tstu.ui.mp.lab3;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import ru.didim99.tstu.R;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.MyLog;

public class ActTask2 extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MP";
  private static final String KEY_NOT_SHOW = "mp.l3.notShow";

  private CheckBox cbNotShow;
  private PopupWindow window;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_l3_t2);

    boolean state = PreferenceManager
      .getDefaultSharedPreferences(this)
      .getBoolean(KEY_NOT_SHOW, false);

    MyLog.d(LOG_TAG, "Loaded preferences: state = " + state);
    findViewById(R.id.mp_btn1).setOnClickListener(v -> updatePrefs());
    cbNotShow = findViewById(R.id.cbNotShowMore);
    cbNotShow.setChecked(state);
    if (state) return;

    MyLog.d(LOG_TAG, "Creating popup window");
    window = new PopupWindow(getLayoutInflater()
      .inflate(R.layout.view_mp_popup, null),
      ViewGroup.LayoutParams.WRAP_CONTENT,
      ViewGroup.LayoutParams.WRAP_CONTENT);
  }

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (window != null) {
      MyLog.d(LOG_TAG, "Showing popup window");
      window.showAtLocation(findViewById(
        R.id.rootLayout), Gravity.END, 25, 0);
    }
  }

  private void updatePrefs() {
    boolean state = cbNotShow.isChecked();
    MyLog.d(LOG_TAG, "Updating preferences: state = " + state);
    PreferenceManager.getDefaultSharedPreferences(this)
      .edit().putBoolean(KEY_NOT_SHOW, state).apply();
  }
}
