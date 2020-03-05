package ru.didim99.tstu.core.itheory.encryption;

import android.content.Context;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.itheory.encryption.rsa.RSAKey;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 28.02.20.
 */
class CryptoTask extends CallbackTask<Action, Action> {
  private CryptoManager manager;
  private String error;

  CryptoTask(Context context, CryptoManager manager) {
    super(context);
    this.manager = manager;
  }

  @Override
  protected Action doInBackgroundInternal(Action action) {
    try {
      switch (action.getType()) {
        case LOAD_FILE:
          String inPath = action.getPath();
          if (inPath.endsWith(CryptoManager.EXT_KEY))
            manager.setKey(new RSAKey(Utils.readFileRaw(inPath)));
          else if (inPath.endsWith(CryptoManager.EXT_TXT))
            manager.setMessage(Utils.joinStr("\n",
              Utils.readFile(inPath, true)));
          else if (inPath.endsWith(CryptoManager.EXT_ENC))
            manager.setEncryptedData(Utils.readFileRaw(inPath));
          else throw new IOException("Unsupported file type ("
              + inPath.substring(inPath.lastIndexOf(CryptoManager.EXT_SEP)) + ")");
          break;
        case SAVE_FILE:
          String outPath = action.getPath();
          if (outPath.endsWith(CryptoManager.EXT_KEY))
            Utils.writeFile(outPath, manager.getKey().serialize());
          else if (outPath.endsWith(CryptoManager.EXT_TXT)) {
            ArrayList<String> data = new ArrayList<>();
            data.add(manager.getDecryptedData());
            Utils.writeFile(outPath, data);
          } else
            Utils.writeFile(outPath, manager.getEncryptedData());
          break;
        case GEN_KEY:
          manager.setKey(RSAKey.generate(action.getKeyLength()));
          break;
        case PROCESS:
          manager.process();
          break;
      }
    } catch (IOException e) {
      error = e.toString();
    }

    return action;
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
