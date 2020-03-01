package ru.didim99.tstu.core.itheory.compression;

import android.content.Context;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 28.02.20.
 */
class CompressionTask extends CallbackTask<CompressionTask.Action, Boolean> {
  enum Action { LOAD_FILE, SAVE_FILE, PROCESS }

  private CompressionManager manager;
  private String error;

  CompressionTask(Context context, CompressionManager manager) {
    super(context);
    this.manager = manager;
  }

  @Override
  protected Boolean doInBackgroundInternal(CompressionTask.Action action) {
    try {
      switch (action) {
        case LOAD_FILE:
          String inFile = manager.getInFile();
          if (inFile.endsWith(Compressor.EXT_UNC))
            manager.setFileMessage(Utils.joinStr(
              "\n", Utils.readFile(inFile, true)));
          else if (inFile.endsWith(Compressor.EXT_COMP))
            manager.setFileData(Utils.readFileRaw(inFile));
          else throw new IOException("Unsupported file type ("
              + inFile.substring(inFile.lastIndexOf(Compressor.EXT_SEP)) + ")");
          break;
        case SAVE_FILE:
          String outFile = manager.getOutFile();
          if (outFile.endsWith(Compressor.EXT_UNC)) {
            ArrayList<String> data = new ArrayList<>();
            data.add(manager.getMessage());
            Utils.writeFile(outFile, data);
          } else Utils.writeFile(outFile, manager.getFileData());
          break;
        case PROCESS:
          manager.process();
          break;
      }
    } catch (IOException e) {
      error = e.toString();
      return false;
    }

    return true;
  }

  @Override
  protected void onPostExecute(Void res) {
    if (error != null) {
      Toast.makeText(appContext.get(),
        error, Toast.LENGTH_LONG).show();
    }

    super.onPostExecute(res);
  }
}
