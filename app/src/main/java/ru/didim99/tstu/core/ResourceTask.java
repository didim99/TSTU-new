package ru.didim99.tstu.core;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 07.06.19.
 */
public class ResourceTask extends CallbackTask<String, Boolean> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ResTask";

  public ResourceTask(Context context) {
    super(context);
  }

  @Override
  protected Boolean doInBackgroundInternal(String path) {
    MyLog.d(LOG_TAG, "Unpacking external resources...");
    ZipInputStream src = new ZipInputStream(appContext
      .get().getResources().openRawResource(R.raw.cache));

    int read;
    byte[] buffer = new byte[8192];
    ZipEntry entry;

    try {
      while ((entry = src.getNextEntry()) != null) {
        File newFile = new File(path, entry.getName());
        if (entry.isDirectory() && !newFile.mkdirs())
          throw new IOException("Can't create target directory: " + entry.getName());
        else if (!entry.isDirectory()) {
          MyLog.d(LOG_TAG, "Unpacking: " + entry.getName());
          FileOutputStream dst = new FileOutputStream(newFile);
          while ((read = src.read(buffer)) > 0)
            dst.write(buffer, 0, read);
        }
      }
    } catch (IOException e) {
      MyLog.e(LOG_TAG, "Unable to unpack cache data: " + e);
      return false;
    }

    MyLog.d(LOG_TAG, "Resources unpacked successfully");
    return true;
  }
}
