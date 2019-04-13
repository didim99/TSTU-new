package ru.didim99.tstu.ui.os.expanse;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Simple EditText value watcher
 * Created by didim99 on 13.04.19.
 */
class ValueWatcher implements TextWatcher {
  private OnValueChangeListener listener;
  private final int viewId;

  ValueWatcher(EditText et, OnValueChangeListener listener) {
    et.addTextChangedListener(this);
    this.viewId = et.getId();
    this.listener = listener;
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {}

  @Override
  public void afterTextChanged(Editable s) {
    try {
      int value = Integer.parseInt(s.toString());
      listener.OnValueChanged(viewId, value);
    } catch (NumberFormatException ignored) {}
  }

  interface OnValueChangeListener {
    void OnValueChanged(int viewId, int value);
  }
}
