package ru.didim99.tstu.core.translator;

import android.content.Context;
import ru.didim99.tstu.core.CallbackTask;

/**
 * Created by didim99 on 10.09.18.
 */
public class TranslatorTask extends CallbackTask<Translator.Config, Translator.Result> {

  public TranslatorTask(Context context) {
    super(context);
  }

  @Override
  protected Translator.Result doInBackgroundInternal(Translator.Config config) {
    return new Translator(appContext.get(), config).translate();
  }
}
