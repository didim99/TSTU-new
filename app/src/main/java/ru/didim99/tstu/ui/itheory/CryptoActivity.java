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
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 27.02.20.
 */
public class CryptoActivity extends BaseActivity {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_CryptoAct";
  // internal intent request codes
  protected static final int REQUEST_LOAD_MSG = 1;
  protected static final int REQUEST_LOAD_KEY = 2;
  protected static final int REQUEST_STORE_KEY = 3;
  protected static final int REQUEST_STORE_ENC = 4;
  protected static final int REQUEST_STORE_DEC = 5;

  // View elements
  private EditText etMessage;
  private EditText etPublicE, etPublicN;
  private EditText etPrivateD, etPrivateN;
  private Button btnLoadMsg, btnStart;
  private Button btnGenKey, btnLoadKey, btnStoreKey;
  private Spinner spKeyLength;
  private TextView tvEnc, tvDec;
  private View pbMain;
  // Workflow

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "CryptoActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_encryption);

    MyLog.d(LOG_TAG, "View components init...");
    etMessage = findViewById(R.id.etMessage);
    etPublicE = findViewById(R.id.etPublicE);
    etPublicN = findViewById(R.id.etPublicN);
    etPrivateD = findViewById(R.id.etPrivateD);
    etPrivateN = findViewById(R.id.etPrivateN);
    btnLoadMsg = findViewById(R.id.btnLoadMsg);
    btnStart = findViewById(R.id.btnStart);
    btnGenKey = findViewById(R.id.btnGenKey);
    btnLoadKey = findViewById(R.id.btnLoadKey);
    btnStoreKey = findViewById(R.id.btnStoreKey);
    spKeyLength = findViewById(R.id.spKeyLength);
    tvEnc = findViewById(R.id.tvEncrypted);
    tvDec = findViewById(R.id.tvDecrypted);
    pbMain = findViewById(R.id.pbMain);
    btnLoadMsg.setOnClickListener(v -> openFile(REQUEST_LOAD_MSG));
    btnLoadKey.setOnClickListener(v -> openFile(REQUEST_LOAD_KEY));
    btnStoreKey.setOnClickListener(v -> storeKey());
    tvEnc.setOnLongClickListener(v -> saveMessage(v, true));
    tvDec.setOnLongClickListener(v -> saveMessage(v, false));
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "CryptoActivity created");
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent i) {
    if (resultCode == RESULT_OK) {
      Uri data = i.getData();
      if (data != null) {
        String path = data.getPath();
        if (path != null) {
          switch (requestCode) {
            case REQUEST_LOAD_MSG:
              MyLog.d(LOG_TAG, "Loading message from: " + path);
              break;
            case REQUEST_LOAD_KEY:
              MyLog.d(LOG_TAG, "Loading key from: " + path);
              break;
            case REQUEST_STORE_KEY:
            case REQUEST_STORE_ENC:
            case REQUEST_STORE_DEC:
              MyLog.d(LOG_TAG, "Saving item to: " + path);
              saveFileDialog(path, requestCode);
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

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    etMessage.setEnabled(!state);
    etPublicE.setEnabled(!state);
    etPublicN.setEnabled(!state);
    etPrivateD.setEnabled(!state);
    etPrivateN.setEnabled(!state);
    btnLoadMsg.setEnabled(!state);
    btnStart.setEnabled(!state);
    btnGenKey.setEnabled(!state);
    btnLoadKey.setEnabled(!state);
    btnStoreKey.setEnabled(!state);
    spKeyLength.setEnabled(!state);

    if (state) {
      pbMain.setVisibility(View.VISIBLE);
      MyLog.d(LOG_TAG, "UI locked");
    } else {
      pbMain.setVisibility(View.INVISIBLE);
      MyLog.d(LOG_TAG, "UI unlocked");
    }
  }

  protected boolean saveMessage(View v, boolean encrypted) {
    if (((TextView) v).getText().toString().isEmpty()) return false;
    openDir(encrypted ? REQUEST_STORE_ENC : REQUEST_STORE_DEC);
    return true;
  }

  private void storeKey() {
    if (etPublicE.getText().length() == 0) return;
    if (etPublicN.getText().length() == 0) return;
    if (etPrivateD.getText().length() == 0) return;
    if (etPrivateN.getText().length() == 0) return;
    openDir(REQUEST_STORE_KEY);
  }

  private void saveFileDialog(String path, int requestCode) {
    View view = getLayoutInflater().inflate(R.layout.dia_sf_text, null);
    EditText etInput = view.findViewById(R.id.etInput);
    //etInput.setText(manager.getInFileName());
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
        .setOnClickListener(v -> onSaveFile(d, path, requestCode));
    });
    dialog.show();
  }

  private void onSaveFile(AlertDialog dialog, String path, int requestCode) {
    try {
      InputValidator iv = InputValidator.getInstance();
      String name = iv.checkEmptyStr(dialog.findViewById(R.id.etInput),
        R.string.errTI_emptyFilename, "filename");
      /*path = path.concat(File.separator).concat(name).concat(
        compress ? Compressor.EXT_COMP : Compressor.EXT_UNC);
      manager.saveToFile(path);*/
      dialog.dismiss();
    } catch (InputValidator.ValidationException ignored) {}
  }
}
