package ru.didim99.tstu.ui.itheory;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.itheory.encryption.CryptoManager;
import ru.didim99.tstu.core.itheory.encryption.rsa.RSAKey;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 27.02.20.
 */
public class CryptoActivity extends BaseActivity
  implements CryptoManager.EventListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_CryptoAct";
  // internal intent request codes
  protected static final int REQUEST_LOAD_MSG = 1;
  protected static final int REQUEST_LOAD_KEY = 2;
  protected static final int REQUEST_STORE_KEY = 3;
  protected static final int REQUEST_STORE_ENC = 4;
  protected static final int REQUEST_STORE_DEC = 5;

  // View elements
  private EditText etMessage;
  private EditText etPublicE;
  private EditText etPrivateD;
  private EditText etKeyModule;
  private Button btnLoadMsg, btnStart;
  private Button btnGenKey, btnSetKey;
  private Button btnLoadKey, btnStoreKey;
  private Spinner spKeyLength;
  private TextView tvEnc, tvDec;
  private View pbMain;
  // Workflow
  private CryptoManager manager;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "CryptoActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_encryption);

    MyLog.d(LOG_TAG, "View components init...");
    etMessage = findViewById(R.id.etMessage);
    etPublicE = findViewById(R.id.etPublicE);
    etPrivateD = findViewById(R.id.etPrivateD);
    etKeyModule = findViewById(R.id.etKeyModule);
    btnLoadMsg = findViewById(R.id.btnLoadMsg);
    btnStart = findViewById(R.id.btnStart);
    btnGenKey = findViewById(R.id.btnGenKey);
    btnSetKey = findViewById(R.id.btnSetKey);
    btnLoadKey = findViewById(R.id.btnLoadKey);
    btnStoreKey = findViewById(R.id.btnStoreKey);
    spKeyLength = findViewById(R.id.spKeyLength);
    tvEnc = findViewById(R.id.tvEncrypted);
    tvDec = findViewById(R.id.tvDecrypted);
    pbMain = findViewById(R.id.pbMain);
    btnSetKey.setOnClickListener(v -> setKey());
    btnGenKey.setOnClickListener(v -> generateKey());
    btnStart.setOnClickListener(v -> startEncryption());
    btnLoadMsg.setOnClickListener(v -> openFile(REQUEST_LOAD_MSG));
    btnLoadKey.setOnClickListener(v -> openFile(REQUEST_LOAD_KEY));
    btnStoreKey.setOnClickListener(v -> openDir(REQUEST_STORE_KEY));
    tvEnc.setOnLongClickListener(v -> saveMessage(v, true));
    tvDec.setOnLongClickListener(v -> saveMessage(v, false));
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "Connecting ImageProcessor...");
    manager = (CryptoManager) getLastCustomNonConfigurationInstance();
    if (manager != null) {
      MyLog.d(LOG_TAG, "Connected to: " + manager);
    } else {
      MyLog.d(LOG_TAG, "No existing ImageProcessor found");
      manager = new CryptoManager(getApplicationContext());
    }

    spKeyLength.setAdapter(new ArrayAdapter<>(this,
      android.R.layout.simple_list_item_1, RSAKey.getPossibleLength()));
    manager.setEventListener(this);

    MyLog.d(LOG_TAG, "CryptoActivity created");
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
            case REQUEST_LOAD_MSG:
            case REQUEST_LOAD_KEY:
              MyLog.d(LOG_TAG, "Loading item from: " + path);
              etMessage.setText(null);
              manager.loadFile(path);
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

  @Override
  public void onOperationStateChanged(boolean state) {
    uiLock(state);
    if (state) {
      tvEnc.setText(null);
      tvDec.setText(null);
    }
  }

  @Override
  @SuppressLint("SetTextI18n")
  public void onKeyChanged(RSAKey key) {
    etPublicE.setText(key.getE().toString());
    etPrivateD.setText(key.getD().toString());
    etKeyModule.setText(key.getN().toString());
    etMessage.setText(manager.getMessage());
  }

  @Override
  public void onDataChanged() {
    etMessage.setText(manager.getMessage());
    tvEnc.setText(manager.getEncryptedDataStr());
    tvDec.setText(manager.getDecryptedData());
  }

  private void uiLock(boolean state) {
    if (state) MyLog.d(LOG_TAG, "Locking UI...");
    else MyLog.d(LOG_TAG, "Unlocking UI...");

    uiLocked = state;
    etMessage.setEnabled(!state);
    etPublicE.setEnabled(!state);
    etPrivateD.setEnabled(!state);
    etKeyModule.setEnabled(!state);
    btnLoadMsg.setEnabled(!state);
    btnStart.setEnabled(!state);
    btnGenKey.setEnabled(!state);
    btnSetKey.setEnabled(!state);
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

  private void generateKey() {
    int length = Integer.parseInt((String) spKeyLength.getSelectedItem());
    manager.generateKey(length);
  }

  private void setKey() {
    try {
      InputValidator iv = InputValidator.getInstance();
      manager.setKey(new RSAKey(
        iv.checkBigInteger(etPublicE, R.string.errTI_emptyKey,
          R.string.errTI_incorrectKey, "Public E"),
        iv.checkBigInteger(etPrivateD, R.string.errTI_emptyKey,
          R.string.errTI_incorrectKey, "Private D"),
        iv.checkBigInteger(etKeyModule, R.string.errTI_emptyKey,
          R.string.errTI_incorrectKey, "Module")));
    } catch (InputValidator.ValidationException ignored) {}
  }

  private void startEncryption() {
    if (manager.getKey() == null) return;
    if (etMessage.getText().length() > 0)
      manager.setMessage(etMessage.getText().toString());
    manager.start();
  }

  private boolean saveMessage(View v, boolean encrypted) {
    if (((TextView) v).getText().toString().isEmpty()) return false;
    openDir(encrypted ? REQUEST_STORE_ENC : REQUEST_STORE_DEC);
    return true;
  }

  private void saveFileDialog(String path, int requestCode) {
    View view = getLayoutInflater().inflate(R.layout.dia_sf_text, null);
    EditText etInput = view.findViewById(R.id.etInput);
    etInput.setText(manager.getFilename());
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

      String ext = "";
      switch (requestCode) {
        case REQUEST_STORE_DEC: ext = CryptoManager.EXT_TXT; break;
        case REQUEST_STORE_ENC: ext = CryptoManager.EXT_ENC; break;
        case REQUEST_STORE_KEY: ext = CryptoManager.EXT_KEY; break;
      }

      path = path.concat(File.separator)
        .concat(name).concat(ext);
      manager.saveToFile(path);
      dialog.dismiss();
    } catch (InputValidator.ValidationException ignored) {}
  }
}
