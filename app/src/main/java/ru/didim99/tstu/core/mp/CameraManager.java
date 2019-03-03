package ru.didim99.tstu.core.mp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 03.03.19.
 */
public class CameraManager {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_mpCM";
  private static final String PHOTO_DIR_BASE = "mp_photos";
  private static final String PHOTO_DIR_SMALL = "small";
  private static final String PHOTO_DIR_BIG = "big";
  private static final int SMALL_SCALE_FACTOR = 4;
  private static final int SMALL_QUALITY = 75;

  // state
  private boolean initCompleted = false;
  private boolean initFailed = false;
  private Toast msg;
  // workflow
  private String dirBig, dirSmall;
  private int photoCount;
  private Camera camera;
  private byte[] pictureData;
  private boolean faceDetection;

  private static CameraManager instance = new CameraManager();
  public static CameraManager getInstance() {
    return instance;
  }
  private CameraManager() {}

  public CameraManager init(Context appContext) {
    if (initCompleted) return this;
    msg = Toast.makeText(appContext, null, Toast.LENGTH_LONG);
    MyLog.d(LOG_TAG, "Initialization");

    try {
      camera = Camera.open();
      Parameters p = camera.getParameters();

      Size size = camera.new Size(0, 0);
      for (Size s : p.getSupportedPictureSizes()) {
        if (s.width * s.height > size.width * size.height)
          size = s;
      }

      MyLog.d(LOG_TAG, String.format(Locale.ROOT,
        "Image size: %dx%d", size.width, size.height));
      p.setFocusMode(Parameters.FOCUS_MODE_AUTO);
      p.setFlashMode(Parameters.FLASH_MODE_OFF);
      p.setPictureSize(size.width, size.height);
      p.setJpegQuality(95);
      camera.setParameters(p);
    } catch (RuntimeException e) {
      MyLog.e(LOG_TAG, "Initialization failed: " + e);
      msg.setText(appContext.getString(
        R.string.mp_cm_initFailure, e.toString()));
      msg.show();
      initFailed = true;
      return this;
    }

    String dirName = appContext.getExternalCacheDir().getAbsolutePath()
      .concat(File.separator).concat(PHOTO_DIR_BASE);
    File baseDir = new File(dirName);
    if (!baseDir.exists()) {
      boolean created = new File(baseDir, PHOTO_DIR_SMALL).mkdirs();
      created &= new File(baseDir, PHOTO_DIR_BIG).mkdirs();
      if (!created) {
        MyLog.e(LOG_TAG, "Can;t create target dirs");
        msg.setText(R.string.mp_cm_cantCreateDirs);
        msg.show();
        initFailed = true;
        return this;
      }
    }

    faceDetection = false;
    camera.setFaceDetectionListener((faces, camera) -> takePhoto());
    dirBig = new File(dirName, PHOTO_DIR_BIG).getAbsolutePath();
    dirSmall = new File(dirName, PHOTO_DIR_SMALL).getAbsolutePath();
    photoCount = new File(dirName, PHOTO_DIR_BIG).list().length;
    MyLog.d(LOG_TAG, "Initialization completed");
    initCompleted = true;
    return this;
  }

  public void attachToView() {
    if (!initCompleted) return;
    camera.startPreview();
  }

  public void detachFromView() {
    if (!initCompleted) return;
    camera.stopPreview();
  }

  public void close() {
    if (invalidState()) return;
    MyLog.d(LOG_TAG, "Closing camera...");
    detachFromView();
    camera.release();
    initCompleted = false;
    MyLog.d(LOG_TAG, "Camera closed");
    msg.setText(R.string.mp_cm_cameraClosed);
    msg.show();
  }

  public void takePhoto() {
    if (invalidState()) return;
    MyLog.d(LOG_TAG, "Taking a photo...");
    camera.autoFocus((success, camera) ->
      camera.takePicture(null, null, this::onJpegCreated));
  }

  private void onJpegCreated(byte[] data, Camera camera) {
    MyLog.d(LOG_TAG, "New JPEG photo created");
    camera.cancelAutoFocus();
    this.pictureData = data;
    msg.setText(R.string.mp_cm_pictureCreated);
    msg.show();
  }

  public void switchFlash() {
    if (invalidState()) return;
    Parameters p = camera.getParameters();
    boolean enabled = p.getFlashMode().equals(Parameters.FLASH_MODE_ON);
    p.setFlashMode(enabled ? Parameters.FLASH_MODE_ON
      : Parameters.FLASH_MODE_OFF);
    camera.setParameters(p);
    msg.setText(enabled ? R.string.mp_cm_flashDisabled
      : R.string.mp_cm_flashEnabled);
    msg.show();
  }

  public void switchFaceDetection() {
    if (invalidState()) return;
    if (!faceDetection)
      camera.startFaceDetection();
    else
      camera.stopFaceDetection();
    faceDetection = !faceDetection;
    msg.setText(faceDetection ? R.string.mp_cm_fdEnabled
      : R.string.mp_cm_fdDisabled);
    msg.show();
  }

  public void savePhoto() {
    if (invalidState()) return;
  }

  public void saveToSD() {
    if (invalidState()) return;
    if (pictureData == null) {
      msg.setText(R.string.mp_cm_nothingToSave);
      msg.show();
      return;
    }

    MyLog.d(LOG_TAG, "Saving photos to sdcard");
    String name = String.format(Locale.ROOT, "%03d.jpg", photoCount);
    File outFile = new File(dirBig, name);
    FileOutputStream fos;

    try {
      fos = new FileOutputStream(outFile);
      MyLog.d(LOG_TAG, "Writing to: " + outFile.getAbsolutePath());
      fos.write(pictureData);
      fos.close();
    } catch (IOException e) {
      MyLog.e(LOG_TAG, "Saving failed: " + e);
      msg.setText(R.string.mp_cm_pictureSavedToSD);
      msg.show();
      return;
    }

    Bitmap src = BitmapFactory.decodeByteArray(
      pictureData, 0, pictureData.length);
    int width = src.getWidth() / SMALL_SCALE_FACTOR;
    int height = src.getHeight() / SMALL_SCALE_FACTOR;
    Bitmap dst = Bitmap.createScaledBitmap(src, width, height, false);
    src.recycle();

    try {
      outFile = new File(dirSmall, name);
      fos = new FileOutputStream(outFile);
      MyLog.d(LOG_TAG, "Writing to: " + outFile.getAbsolutePath());
      dst.compress(Bitmap.CompressFormat.JPEG, SMALL_QUALITY, fos);
      dst.recycle();
      fos.close();
    } catch (IOException e) {
      MyLog.e(LOG_TAG, "Saving failed: " + e);
      msg.setText(R.string.mp_cm_pictureSavedToSD);
      msg.show();
      return;
    }

    photoCount++;
    pictureData = null;
    MyLog.d(LOG_TAG, "Photos saved");
    msg.setText(R.string.mp_cm_pictureSavedToSD);
    msg.show();
  }

  private boolean invalidState() {
    if (initCompleted) return false;
    msg.setText(initFailed ? R.string.mp_cm_unavailable
      : R.string.mp_cm_initFirst);
    msg.show();
    return true;
  }
}
