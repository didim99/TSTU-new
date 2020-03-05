package ru.didim99.tstu.ui.itheory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.itheory.compression.CompressionManager;
import ru.didim99.tstu.core.itheory.compression.Compressor;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.ui.utils.SpinnerAdapter;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 27.02.20.
 */
public class CompressionActivity extends BaseActivity
  implements CompressionManager.EventListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_CompAct";
  // internal intent request codes
  protected static final int REQUEST_SAVE_UNC = 3;
  protected static final int REQUEST_SAVE_COMP = 4;

  // View elements
  private Spinner spMethod;
  private EditText etMessage;
  private Button btnLoad, btnStart;
  private TextView tvIn, tvOut;
  private TextView tvInfo;
  private View pbMain;
  // Workflow
  private CompressionManager manager;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "CompressionActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_compression_text);

    MyLog.d(LOG_TAG, "View components init...");
    btnLoad = findViewById(R.id.btnLoad);
    btnStart = findViewById(R.id.btnStart);
    spMethod = findViewById(R.id.spMethod);
    etMessage = findViewById(R.id.etMessage);
    tvIn = findViewById(R.id.tvInMsg);
    tvOut = findViewById(R.id.tvOutMsg);
    tvInfo = findViewById(R.id.tvInfo);
    pbMain = findViewById(R.id.pbMain);
    btnLoad.setOnClickListener(v -> openFile());
    btnStart.setOnClickListener(v -> startCoder());
    tvIn.setOnLongClickListener(v -> saveToFile(v, false));
    tvOut.setOnLongClickListener(v -> saveToFile(v, true));
    tvInfo.setOnLongClickListener(v -> Utils.copyToClipboard(this, tvInfo.getText()));
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "Connecting CompressionManager...");
    manager = (CompressionManager) getLastCustomNonConfigurationInstance();
    if (manager != null) {
      MyLog.d(LOG_TAG, "Connected to: " + manager);
    } else {
      MyLog.d(LOG_TAG, "No existing CompressionManager found");
      manager = new CompressionManager(getApplicationContext());
    }

    manager.setEventListener(this);
    spMethod.setSelection(manager.getCompressionType());
    spMethod.setOnItemSelectedListener(new SpinnerAdapter(
      manager::setCompressionType));

    MyLog.d(LOG_TAG, "CompressionActivity created");
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    manager.setEventListener(null);
    return manager;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent i) {
    if (resultCode == RESULT_OK) {
      Uri data = i.getData();
      if (data != null) {
        String path = data.getPath();
        if (path != null) {
          switch (requestCode) {
            case REQUEST_GET_FILE:
              MyLog.d(LOG_TAG, "Loading item: " + path);
              etMessage.setText(null);
              manager.loadFile(path);
              break;
            case REQUEST_SAVE_UNC:
              MyLog.d(LOG_TAG, "Saving item to: " + path);
              saveFileDialog(path, false);
              break;
            case REQUEST_SAVE_COMP:
              MyLog.d(LOG_TAG, "Saving item to: " + path);
              saveFileDialog(path, true);
              break;
          }
        } else {
          Toast.makeText(this, R.string.errGeneric_unsupportedScheme,
            Toast.LENGTH_LONG).show();
        }
      }
    }

    else if (resultCode == RESULT_CANCELED)
      MyLog.d(LOG_TAG, "Choosing path aborted");
  }

  @Override
  public void onCompressionEvent(CompressionManager.Event event) {
    switch (event) {
      case OPERATION_START:
        uiLock(true);
      case TYPE_CHANGED:
        tvInfo.setText(null);
        tvIn.setText(null);
        tvOut.setText(null);
        break;
      case OPERATION_END:
        uiLock(false);
        tvIn.setText(manager.getMessage());
        tvOut.setText(manager.getCompressor().getCompressed());
        tvInfo.setText(manager.getCompressor().getInfo());
        break;
    }
  }

  private void startCoder() {
    manager.setMessage(etMessage.getText().toString());
    manager.start();
  }

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    spMethod.setEnabled(!state);
    etMessage.setEnabled(!state);
    btnLoad.setEnabled(!state);
    btnStart.setEnabled(!state);
    tvInfo.setEnabled(!state);
    tvIn.setEnabled(!state);
    tvOut.setEnabled(!state);

    if (state) {
      pbMain.setVisibility(View.VISIBLE);
      MyLog.d(LOG_TAG, "UI locked");
    } else {
      pbMain.setVisibility(View.INVISIBLE);
      MyLog.d(LOG_TAG, "UI unlocked");
    }
  }

  private boolean saveToFile(View v, boolean compressed) {
    if (((TextView) v).getText().toString().isEmpty()) return false;
    openDir(compressed ? REQUEST_SAVE_COMP : REQUEST_SAVE_UNC);
    return true;
  }

  private void saveFileDialog(String path, boolean compress) {
    View view = getLayoutInflater().inflate(R.layout.dia_sf_text, null);
    EditText etInput = view.findViewById(R.id.etInput);
    etInput.setText(manager.getInFileName());
    etInput.setSelection(etInput.getText().length());

    AlertDialog.Builder adb = new AlertDialog.Builder(this)
      .setTitle(R.string.iTheory_imageName).setView(view)
      .setPositiveButton(R.string.dialogButtonOk, null)
      .setNegativeButton(R.string.dialogButtonCancel, null);
    MyLog.d(LOG_TAG, "Save dialog created");
    AlertDialog dialog = adb.create();
    dialog.setOnShowListener((di) -> {
      AlertDialog d = (AlertDialog) di;
      d.getButton(AlertDialog.BUTTON_POSITIVE)
        .setOnClickListener(v -> onSaveFile(d, path, compress));
    });
    dialog.show();
  }

  private void onSaveFile(AlertDialog dialog, String path, boolean compress) {
    try {
      InputValidator iv = InputValidator.getInstance();
      String name = iv.checkEmptyStr(dialog.findViewById(R.id.etInput),
        R.string.errTI_emptyFilename, "filename");
      path = path.concat(File.separator).concat(name).concat(
        compress ? Compressor.EXT_COMP : Compressor.EXT_UNC);
      manager.saveToFile(path);
      dialog.dismiss();
    } catch (InputValidator.ValidationException ignored) {}
  }
}
