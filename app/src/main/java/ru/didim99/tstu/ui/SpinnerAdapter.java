package ru.didim99.tstu.ui;

import android.view.View;
import android.widget.AdapterView;

/**
 * Created by didim99 on 21.04.19.
 */
public class SpinnerAdapter implements AdapterView.OnItemSelectedListener {
  private final OnItemSelectedListener listener;

  public SpinnerAdapter(OnItemSelectedListener listener) {
    this.listener = listener;
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    listener.onItemSelected(position);
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {}

  @FunctionalInterface
  public interface OnItemSelectedListener {
    void onItemSelected(int position);
  }
}
