package ru.didim99.tstu.ui.itheory;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.itheory.Image;
import ru.didim99.tstu.core.itheory.ImageProcessor;
import ru.didim99.tstu.ui.BaseActivity;
import ru.didim99.tstu.ui.view.DrawerView;
import ru.didim99.tstu.utils.InputValidator;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 13.02.20.
 */
public class RLEActivity extends BaseActivity
  implements ImageProcessor.StateChangeListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_RLEAct";

  // View elements
  private EditText etSizeX;
  private EditText etSizeY;
  private Button btnLoad, btnSave;
  private Button btnCreate, btnClear;
  private Button btnGenerate;
  private Button btnStart, btnTest;
  private TextView tvState, tvLog;
  private DrawerView view;
  private View pbMain;
  // Workflow
  private ImageProcessor processor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MyLog.d(LOG_TAG, "RLEActivity creating...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.act_image_compression);

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
    btnLoad.setOnClickListener(v -> openFile());
    btnCreate.setOnClickListener(v -> createImage());
    btnSave.setOnClickListener(v -> saveImage());
    btnClear.setOnClickListener(v -> processor.clearImage());
    btnGenerate.setOnClickListener(v -> processor.randomizeImage());
    MyLog.d(LOG_TAG, "View components init completed");

    MyLog.d(LOG_TAG, "Connecting ImageProcessor...");
    processor = (ImageProcessor) getLastCustomNonConfigurationInstance();
    if (processor != null) {
      MyLog.d(LOG_TAG, "Connected to: " + processor);
      processor.setStateChangeListener(this);
    } else {
      MyLog.d(LOG_TAG, "No existing ImageProcessor found");
      processor = new ImageProcessor();
    }

    MyLog.d(LOG_TAG, "RLEActivity created");
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    return processor;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent i) {
    if (requestCode == REQUEST_GET_FILE && resultCode == RESULT_OK) {
      Uri data = i.getData();
      if (data != null) {
        String path = data.getPath();
        if (path != null) {
          MyLog.d(LOG_TAG, "Loading item: " + path);
          processor.loadImage(this, path);
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
      || state.equals(ImageProcessor.State.PROCESSING)
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

    if (!hasImage) btnStart.setText(R.string.buttonStart);
    else if (processor.getImage().isCompressed())
      btnStart.setText(R.string.iTheory_unpackImage);
    else btnStart.setText(R.string.iTheory_packImage);
  }

  @Override
  public void onImageDataChanged(Image image) {
    view.setSource(image.getBitmap(), false);
    etSizeX.setText(String.valueOf(image.getWidth()));
    etSizeY.setText(String.valueOf(image.getHeight()));
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

  private void saveImage() {

  }
}
