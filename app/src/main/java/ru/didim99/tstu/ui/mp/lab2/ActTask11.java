package ru.didim99.tstu.ui.mp.lab2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.mp.students.Action;
import ru.didim99.tstu.core.mp.students.Student;
import ru.didim99.tstu.core.mp.students.StudentTask;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.ui.DialogEventListener;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;

public class ActTask11 extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MP";

  private Toast toast;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_mp_l2_t1);

    findViewById(R.id.mp_btnView).setOnClickListener(v ->
      startActivity(new Intent(this, ActTask12.class)));
    findViewById(R.id.mp_btnAdd).setOnClickListener(v -> editDialog(
      R.string.mp_enterName, R.layout.dia_mp_single_field, this::addStudent));
    findViewById(R.id.mp_btnReplace).setOnClickListener(v -> replaceTask());
    toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
  }

  private void editDialog(@StringRes int titleId, @LayoutRes int viewId,
                          DialogEventListener listener) {
    MyLog.d(LOG_TAG, "Edit dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(titleId);
    adb.setView(viewId);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    MyLog.d(LOG_TAG, "Edit dialog created");
    AlertDialog dialog = adb.create();
    dialog.setOnShowListener((di) -> {
      AlertDialog d = (AlertDialog) di;
      d.getButton(AlertDialog.BUTTON_POSITIVE)
        .setOnClickListener(v -> listener.onEvent(d));
    });
    dialog.show();
  }

  private void addStudent(AlertDialog dialog) {
    try {
      InputValidator iv = InputValidator.getInstance();
      Student student = new Student();
      EditText input = dialog.findViewById(R.id.etInput);
      student.setName(iv.checkEmptyStr(
        input, R.string.errMp_emptyName, "Name"));
      Action action = new Action(Action.Type.ADD);
      action.setStudent(student);

      StudentTask task = new StudentTask(this);
      task.registerEventListener((e, d) -> {
        if (e == CallbackTask.Event.FINISH) {
          toast.setText(getString(R.string.mp_added, d.get(0).getId()));
          toast.show();
        }
      });

      dialog.dismiss();
      task.execute(action);
    } catch (InputValidator.ValidationException ignored) {}
  }

  private void replaceTask() {
    StudentTask task = new StudentTask(this);
    task.registerEventListener((e, d) -> {
      if (e == CallbackTask.Event.FINISH) {
        toast.setText(getString(R.string.mp_replaced, d.get(0).getId()));
        toast.show();
      }
    });
    task.execute(new Action(Action.Type.REPLACE));
  }
}
