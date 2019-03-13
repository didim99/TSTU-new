package ru.didim99.tstu.ui.graphics.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import ru.didim99.tstu.R;

/**
 * Created by didim99 on 10.03.19.
 */
public class RangeBar extends LinearLayout
  implements SeekBar.OnSeekBarChangeListener {
  private static final int DEFAULT_MIN = 0;
  private static final int DEFAULT_MAX = 100;
  private static final int DEFAULT_VALUE = 0;
  private static final double DEFAULT_SCALED = 0.0;

  private final SeekBar seekBar;
  private final TextView tvTitle;
  private OnValueChangedListener listener;
  private OnScaledValueChangedListener sListener;
  private boolean isScaled;
  private double factor = 1.0;
  private int minValue;
  private String title;

  public RangeBar(Context context) {
    this(context, null, 0);
  }

  public RangeBar(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RangeBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    TypedArray array = context.obtainStyledAttributes(
      attrs, R.styleable.RangeBar, defStyleAttr, 0);
    title = array.getString(R.styleable.RangeBar_title);
    isScaled = array.getBoolean(R.styleable.RangeBar_scaled, false);
    array.recycle();

    View itemView = LayoutInflater.from(context).inflate(
      R.layout.view_range_bar, this);
    seekBar = itemView.findViewById(R.id.seekBar);
    tvTitle = itemView.findViewById(R.id.title);
    seekBar.setOnSeekBarChangeListener(this);
    setBounds(DEFAULT_MIN, DEFAULT_MAX);
    if (isScaled) setValue(DEFAULT_SCALED);
    else setValue(DEFAULT_VALUE);
  }

  public double getValue() {
    return (minValue + seekBar.getProgress()) * factor;
  }

  public void setBounds(int min, int max) {
    seekBar.setMax(max - min);
    minValue = min;
  }

  public void setValue(int value) {
    seekBar.setProgress(value - minValue);
    tvTitle.setText(String.format(title, value));
  }

  public void setValue(double value) {
    seekBar.setProgress((int) (value / factor - minValue));
    tvTitle.setText(String.format(title, value));
  }

  public void setFactor(double factor) {
    this.factor = factor;
  }

  public void setOnValueChangedListener(OnValueChangedListener listener) {
    this.listener = listener;
  }

  public void setOnScaledValueChangedListener(OnScaledValueChangedListener listener) {
    this.sListener = listener;
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    int value = minValue + progress;
    if (isScaled) {
      double sValue = value * factor;
      tvTitle.setText(String.format(title, sValue));
      if (fromUser && sListener != null)
        sListener.OnScaledValueChanged(sValue);
    } else {
      tvTitle.setText(String.format(title, value));
      if (fromUser && listener != null)
        listener.OnValueChanged(value);
    }
  }

  @Override public void onStartTrackingTouch(SeekBar seekBar) {}
  @Override public void onStopTrackingTouch(SeekBar seekBar) {}

  @FunctionalInterface
  public interface OnValueChangedListener {
    void OnValueChanged(int value);
  }

  @FunctionalInterface
  public interface OnScaledValueChangedListener {
    void OnScaledValueChanged(double value);
  }
}
