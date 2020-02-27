package ru.didim99.tstu.ui.itheory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.itheory.image.Image;
import ru.didim99.tstu.core.itheory.image.ImageProcessor;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.ui.view.DrawerView;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 13.02.20.
 */
public class RLEActivity extends BaseActivity
  implements ImageProcessor.EventListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_RLEAct";

  // View elements
  private EditText etSizeX;
  private EditText etSizeY;
  private Button btnLoad, btnSave;
  private Button btnCreate, btnClear;
  private Button btnGenerate;
  private Button btnStart, btnTest;
  private TextView tvState, tvLog;
  private ScrollView logWrapper;
  private DrawerView view;
  private View pbMain;
  // Workflow
  private ImageProcessor processor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "RLEActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_compression_image);

    MyLog.d(LOG_TAG, "View components init...");
    etSizeX = findViewById(R.id.etSizeX);
    etSizeY = findViewById(R.id.etSizeY);
    btnLoad = findViewById(R.id.btnLoad);
    btnSave = findViewById(R.id.btnSave);
    btnCreate = findViewById(R.id.btnCreate);
    btnClear = findViewById(R.id.btnClear);
    btnGenerate = findViewById(R.id.btnGenerate);
    btnStart = findViewById(R.id.btnStart);
    btnTest = findViewById(R.id.btnTest);
    tvState = findViewById(R.id.tvState);
    tvLog = findViewById(R.id.tvLog);
    view = findViewById(R.id.view);
    pbMain = findViewById(R.id.pbMain);
    logWrapper = findViewById(R.id.logWrapper);
    btnLoad.setOnClickListener(v -> openFile());
    btnSave.setOnClickListener(v -> openDir());
    btnCreate.setOnClickListener(v -> createImage());
    btnClear.setOnClickListener(v -> processor.clearImage());
    btnGenerate.setOnClickListener(v -> processor.randomizeImage());
    btnStart.setOnClickListener(v -> processor.toggleImageState());
    btnTest.setOnClickListener(v -> processor.testCoder());
    etSizeX.setText(String.valueOf(Image.DEFAULT_SIZE));
    etSizeY.setText(String.valueOf(Image.DEFAULT_SIZE));
    tvLog.setMovementMethod(new ScrollingMovementMethod());
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "Connecting ImageProcessor...");
    processor = (ImageProcessor) getLastCustomNonConfigurationInstance();
    if (processor != null) {
      MyLog.d(LOG_TAG, "Connected to: " + processor);
    } else {
      MyLog.d(LOG_TAG, "No existing ImageProcessor found");
      processor = new ImageProcessor(getApplicationContext());
    }

    processor.setStateChangeListener(this);
    MyLog.d(LOG_TAG, "RLEActivity created");
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    return processor;
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
              processor.loadImage(path);
              break;
            case REQUEST_GET_DIR:
              MyLog.d(LOG_TAG, "Saving item to: " + path);
              saveImageDialog(path);
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
  public void onStateChanged(ImageProcessor.State state) {
    MyLog.d(LOG_TAG, "Processor state changed: " + state);
    boolean hasImage = !state.equals(ImageProcessor.State.INITIAL);
    uiLocked = state.equals(ImageProcessor.State.LOADING)
      || state.equals(ImageProcessor.State.TESTING)
      || state.equals(ImageProcessor.State.SAVING);

    btnLoad.setEnabled(!uiLocked);
    btnSave.setEnabled(!uiLocked && hasImage);
    btnCreate.setEnabled(!uiLocked);
    btnClear.setEnabled(!uiLocked && hasImage);
    btnGenerate.setEnabled(!uiLocked && hasImage);
    btnStart.setEnabled(!uiLocked && hasImage);
    btnTest.setEnabled(!uiLocked && hasImage);
    etSizeX.setEnabled(!uiLocked);
    etSizeY.setEnabled(!uiLocked);
    pbMain.setVisibility(uiLocked ? View.VISIBLE : View.INVISIBLE);
    if (uiLocked) view.recycle();

    if (!hasImage) {
      tvLog.setText(null);
      btnStart.setText(R.string.buttonStart);
    } else if (!uiLocked) {
      if (processor.getImage().isCompressed())
        btnStart.setText(R.string.iTheory_unpackImage);
      else btnStart.setText(R.string.iTheory_packImage);
    }

    switch (state) {
      case INITIAL: tvState.setText(R.string.iTheory_state_initial); break;
      case LOADING: tvState.setText(R.string.iTheory_state_loading); break;
      case READY: tvState.setText(R.string.iTheory_state_ready); break;
      case SAVING: tvState.setText(R.string.iTheory_state_saving); break;
      case TESTING: tvState.setText(R.string.iTheory_state_testing); break;
    }
  }

  @Override
  public void onImageDataChanged(Image image) {
    view.setSource(image.getBitmap(), false);
    etSizeX.setText(String.valueOf(image.getWidth()));
    etSizeY.setText(String.valueOf(image.getHeight()));
  }

  @Override
  public void onError(ImageProcessor.State state, String err) {
    String message = null;
    switch (state) {
      case LOADING: message = getString(R.string.errTI_imageLoading); break;
      case SAVING: message = getString(R.string.errTI_imageSaving); break;
    }

    if (message != null) Toast.makeText(this,
      String.format(message, err), Toast.LENGTH_LONG).show();
  }

  @Override
  public void logWrite(String msg) {
    tvLog.append(msg.concat("\n"));
    logWrapper.fullScroll(View.FOCUS_DOWN);
  }

  private void createImage() {
    try {
      InputValidator iv = InputValidator.getInstance();
      int width = iv.checkInteger(etSizeX, Image.MIN_SIZE, Image.MAX_SIZE,
        R.string.errTI_emptySizeX, R.string.errTI_incorrectSizeX, "image width");
      int height = iv.checkInteger(etSizeY, Image.MIN_SIZE, Image.MAX_SIZE,
        R.string.errTI_emptySizeY, R.string.errTI_incorrectSizeY, "image height");
      processor.createImage(width, height);
    } catch (InputValidator.ValidationException ignored) {}
  }

  private void saveImageDialog(String path) {
    View view = getLayoutInflater().inflate(R.layout.dia_sf_text, null);
    EditText etInput = view.findViewById(R.id.etInput);
    etInput.setText(processor.getImage().getName());
    etInput.setSelection(etInput.getText().toString()
      .lastIndexOf(Image.EXT_SEP));

    AlertDialog.Builder adb = new AlertDialog.Builder(this)
      .setTitle(R.string.iTheory_imageName).setView(view)
      .setPositiveButton(R.string.dialogButtonOk, null)
      .setNegativeButton(R.string.dialogButtonCancel, null);
    MyLog.d(LOG_TAG, "Save image dialog created");
    AlertDialog dialog = adb.create();
    dialog.setOnShowListener((di) -> {
      AlertDialog d = (AlertDialog) di;
      d.getButton(AlertDialog.BUTTON_POSITIVE)
        .setOnClickListener(v -> saveImage(d, path));
    });
    dialog.show();
  }

  private void saveImage(AlertDialog dialog, String path) {
    try {
      InputValidator iv = InputValidator.getInstance();
      String name = iv.checkEmptyStr(dialog.findViewById(R.id.etInput),
        R.string.errTI_emptyFilename, "filename");
      path = path.concat(File.separator).concat(name);
      processor.saveImage(path);
      dialog.dismiss();
    } catch (InputValidator.ValidationException ignored) {}
  }
}
