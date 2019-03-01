package ru.didim99.tstu.ui.oop;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.io.IOException;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.oop.Abiturient;
import ru.didim99.tstu.core.oop.AbiturientManager;
import ru.didim99.tstu.core.oop.GradeList;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.ui.DialogEventListener;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 23.02.19.
 */
public class AbiturientActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_AbitAct";
  private static final String FILENAME = "/abiturients.dat";
  private static final String DIRNAME = "/oop";

  private AbiturientManager manager;
  private AbiturientAdapter adapter;
  private String cacheFile;
  private Toast toast;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_simple_list);

    cacheFile = getExternalCacheDir().getAbsolutePath()
      .concat(DIRNAME).concat(FILENAME);
    manager = (AbiturientManager) getLastCustomNonConfigurationInstance();
    if (manager == null) manager = new AbiturientManager();

    adapter = new AbiturientAdapter(this, this::editItemDialog);
    adapter.refreshData(manager.getAll());
    RecyclerView rv = findViewById(R.id.rvList);
    rv.setLayoutManager(new LinearLayoutManager(this));
    rv.setHasFixedSize(true);
    rv.setAdapter(adapter);

    toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_abiturient, menu);
    return true;
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    return manager;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.act_add:
        editDialog(R.string.oop_abit_add,
          R.layout.dia_oop_abit_add, this::addAbiturient);
        return false;
      case R.id.act_open:
        try {
          manager.load(cacheFile);
          toast.setText(getString(R.string.loadedFromFile, cacheFile));
          adapter.refreshData(manager.getAll());
        } catch (IOException e) {
          toast.setText(getString(R.string.errGeneric_cantRead, e.toString()));
        }
        toast.show();
        return false;
      case R.id.act_save:
        try {
          manager.save(cacheFile);
          toast.setText(getString(R.string.savedToFile, cacheFile));
        } catch (IOException e) {
          toast.setText(getString(R.string.errGeneric_cantWrite, e.toString()));
        }
        toast.show();
        return false;
      case R.id.act_filter:
        filterDialog();
        return false;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void filterDialog() {
    MyLog.d(LOG_TAG, "Filter dialog called");
    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.buttonMore);
    adb.setItems(R.array.oop_abit_filters, (dialog, pos) -> {
      switch (pos) {
        case 0:
          adapter.refreshData(manager.getAll());
          break;
        case 1:
          adapter.refreshData(manager.getBadGrades());
          break;
        case 2:
          editDialog(R.string.oop_abit_enterMinSum,
            R.layout.dia_oop_abit_single_field, this::filterMinSum);
          break;
        case 3:
          editDialog(R.string.oop_abit_enterCount,
            R.layout.dia_oop_abit_single_field, this::filterTop);
          break;
        case 4:
          editDialog(R.string.oop_abit_enterExact,
            R.layout.dia_oop_abit_single_field, this::filterExact);
          break;
      }
    });
    MyLog.d(LOG_TAG, "Filter dialog created");
    adb.create().show();
  }

  private void editDialog(int titleId, int viewId, DialogEventListener listener) {
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

  private void editItemDialog(int itemId) {
    MyLog.d(LOG_TAG, "Item clicked: " + itemId);
    Abiturient a = manager.get(itemId);
    GradeList grades = a.getGrades();
    View view = getLayoutInflater().inflate(R.layout.dia_oop_abit_add, null);
    ((EditText) view.findViewById(R.id.etSurname)).setText(a.getSurname());
    ((EditText) view.findViewById(R.id.etName)).setText(a.getName());
    ((EditText) view.findViewById(R.id.etPatronymic)).setText(a.getPatronymic());
    ((EditText) view.findViewById(R.id.etAddress)).setText(a.getAddress());
    ((EditText) view.findViewById(R.id.etLang)).setText(String.valueOf(grades.getLang()));
    ((EditText) view.findViewById(R.id.etMath)).setText(String.valueOf(grades.getMath()));
    ((EditText) view.findViewById(R.id.etPhysics)).setText(String.valueOf(grades.getPhysics()));

    AlertDialog.Builder adb = new AlertDialog.Builder(this);
    adb.setTitle(R.string.oop_abit_edit);
    adb.setView(view);
    adb.setPositiveButton(R.string.dialogButtonOk, null);
    adb.setNegativeButton(R.string.dialogButtonCancel, null);
    adb.setNeutralButton(R.string.dialogButtonDelete, (d, v) -> {
      manager.remove(itemId);
      adapter.refreshData(manager.getAll());
      toast.setText(R.string.oop_abit_deleted);
      toast.show();
    });
    MyLog.d(LOG_TAG, "Edit item dialog created");
    AlertDialog dialog = adb.create();
    dialog.setOnShowListener((di) -> {
      AlertDialog d = (AlertDialog) di;
      d.getButton(AlertDialog.BUTTON_POSITIVE)
        .setOnClickListener(v -> editAbiturient(d, itemId));
    });
    dialog.show();
  }

  private void addAbiturient(AlertDialog dialog) {
    try {
      Abiturient a = new Abiturient();
      manager.add(parseData(dialog, a));
      dialog.dismiss();
      adapter.refreshData(manager.getAll());
      toast.setText(R.string.oop_abit_added);
      toast.show();
    } catch (InputValidator.ValidationException ignored) {}
  }

  private void editAbiturient(AlertDialog dialog, int itemId) {
    try {
      Abiturient a = manager.get(itemId);
      parseData(dialog, a);
      dialog.dismiss();
      adapter.refreshData(manager.getAll());
      toast.setText(R.string.oop_abit_edited);
      toast.show();
    } catch (InputValidator.ValidationException ignored) {}
  }

  private Abiturient parseData(AlertDialog dialog, Abiturient a) {
    InputValidator iv = InputValidator.getInstance();

    EditText input = dialog.findViewById(R.id.etSurname);
    a.setSurname(iv.checkEmptyStr(
      input, R.string.errAbit_emptyInfo, "Surname"));
    input = dialog.findViewById(R.id.etName);
    a.setName(iv.checkEmptyStr(
      input, R.string.errAbit_emptyInfo, "Name"));
    input = dialog.findViewById(R.id.etPatronymic);
    a.setPatronymic(iv.checkEmptyStr(
      input, R.string.errAbit_emptyInfo, "Patronymic"));
    input = dialog.findViewById(R.id.etAddress);
    a.setAddress(iv.checkEmptyStr(
      input, R.string.errAbit_emptyInfo, "Address"));

    GradeList gradeList = a.getGrades();
    input = dialog.findViewById(R.id.etLang);
    int grage = iv.checkInteger(input, 0, 100,
      R.string.errAbit_emptyGrades, R.string.errAbit_incorrectGrades, "Lang grade");
    gradeList.setLang(grage);
    input = dialog.findViewById(R.id.etMath);
    grage = iv.checkInteger(input, 0, 100,
      R.string.errAbit_emptyGrades, R.string.errAbit_incorrectGrades, "Math grade");
    gradeList.setMath(grage);
    input = dialog.findViewById(R.id.etPhysics);
    grage = iv.checkInteger(input, 0, 100,
      R.string.errAbit_emptyGrades, R.string.errAbit_incorrectGrades, "Physics grade");
    gradeList.setPhysics(grage);
    return a.init();
  }

  private void filterMinSum(AlertDialog dialog) {
    try {
      EditText input = dialog.findViewById(R.id.etInput);
      int sum = InputValidator.getInstance().checkInteger(input, 1,
        R.string.errAbit_emptySum, R.string.errAbit_incorrectSum, "Grade Sum");
      dialog.dismiss();
      adapter.refreshData(manager.getForSum(sum));
    } catch (InputValidator.ValidationException ignored) {}
  }

  private void filterTop(AlertDialog dialog) {
    try {
      EditText input = dialog.findViewById(R.id.etInput);
      int count = InputValidator.getInstance().checkInteger(input, 1,
        R.string.errAbit_emptyCount, R.string.errAbit_incorrectCount, "Count");
      dialog.dismiss();
      adapter.refreshData(manager.getTop(count));
    } catch (InputValidator.ValidationException ignored) {}
  }

  private void filterExact(AlertDialog dialog) {
    try {
      EditText input = dialog.findViewById(R.id.etInput);
      int sum = InputValidator.getInstance().checkInteger(input, 1,
        R.string.errAbit_emptyCount, R.string.errAbit_incorrectCount, "Count");
      dialog.dismiss();
      adapter.refreshData(manager.getHalfPass(sum));
    } catch (InputValidator.ValidationException ignored) {}
  }
}
