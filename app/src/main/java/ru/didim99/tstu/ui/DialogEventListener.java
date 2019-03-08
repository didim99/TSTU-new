package ru.didim99.tstu.ui;

import android.support.v7.app.AlertDialog;

/**
 * Created by didim99 on 26.02.19.
 */
@FunctionalInterface
public interface DialogEventListener {
  void onEvent(AlertDialog d);
}
