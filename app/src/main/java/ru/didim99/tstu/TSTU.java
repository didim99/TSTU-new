package ru.didim99.tstu;

import android.app.Application;
import ru.didim99.tstu.utils.InputValidator;

/**
 * Root Application class
 * Created by didim99 on 16.09.18.
 */
public class TSTU extends Application {
  public static final String EXTRA_TYPE = BuildConfig.APPLICATION_ID + ".Type";

  @Override
  public void onCreate() {
    super.onCreate();
    InputValidator.getInstance().init(this);
  }
}
