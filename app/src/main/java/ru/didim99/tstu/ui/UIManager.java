package ru.didim99.tstu.ui;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.AnyRes;
import android.support.annotation.AttrRes;
import android.util.TypedValue;

/**
 * Created by didim99 on 09.05.18.
 */

public class UIManager {
  private static final UIManager ourInstance = new UIManager();
  public static UIManager getInstance() {
    return ourInstance;
  }
  private UIManager() {}

  private Resources.Theme theme;
  private TypedValue typedValue;

  public void init(Context appContext) {
    this.theme = appContext.getTheme();
    this.typedValue = new TypedValue();
  }

  public void applyTheme(Resources.Theme theme) {
    this.theme = theme;
  }

  @AnyRes
  public int resolveAttr(@AttrRes int attr) {
    theme.resolveAttribute(attr, typedValue, true);
    return typedValue.resourceId;
  }
}
