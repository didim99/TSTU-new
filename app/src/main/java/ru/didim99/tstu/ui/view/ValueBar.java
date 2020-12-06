package ru.didim99.tstu.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.ui.UIManager;

/**
 * Custom ProgressBar class with percentage and value displaying.
 * Created by didim99 on 29.08.18.
 */
public class ValueBar extends LinearLayout {
  protected static final int MAX_VISIBLE_VALUE = 1000;

  protected final ProgressBar progressBar;
  protected final TextView percentage;
  protected final TextView values;
  protected final ImageView iconView;
  protected final int colorBg, colorFg;
  protected final boolean hasIcon;
  protected final String units;
  private double maxValue;

  public ValueBar(Context context) {
    this(context, null, 0);
  }

  public ValueBar(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ValueBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    UIManager uiManager = UIManager.getInstance();
    TypedArray array = context.obtainStyledAttributes(
      attrs, R.styleable.ValueBar, defStyleAttr, 0);
    colorBg = array.getColor(R.styleable.ValueBar_colorBackground,
      getResources().getColor(uiManager.resolveAttr(R.attr.clr_valueBarBg)));
    colorFg = array.getColor(R.styleable.ValueBar_colorForeground,
      getResources().getColor(uiManager.resolveAttr(R.attr.clr_valueBarFg)));
    units = array.getString(R.styleable.ValueBar_units);
    int iconID = array.getResourceId(R.styleable.ValueBar_iconID, 0);
    array.recycle();

    LayerDrawable drawable = (LayerDrawable) getResources()
      .getDrawable(R.drawable.bar_circle);
    drawable.findDrawableByLayerId(android.R.id.background)
      .setColorFilter(colorBg, PorterDuff.Mode.SRC_IN);
    drawable.findDrawableByLayerId(R.id.bar_circle_fg)
      .setColorFilter(colorFg, PorterDuff.Mode.SRC_IN);

    View itemView = LayoutInflater.from(context).inflate(
      R.layout.view_bar_circle_single, this);
    iconView = itemView.findViewById(R.id.icon);
    progressBar = itemView.findViewById(R.id.progressBar);
    percentage = itemView.findViewById(R.id.percentage);
    values = itemView.findViewById(R.id.value);
    progressBar.setProgressDrawable(drawable);
    progressBar.setMax(MAX_VISIBLE_VALUE);
    setValueIfMaxZero(0);

    hasIcon = iconID != 0;
    if (hasIcon) {
      iconView.setImageResource(iconID);
      updateIconColor(0);
    } else {
      iconView.setVisibility(GONE);
    }
  }

  public void setMaximum(double max) {
    maxValue = max;
  }

  public void setValueDouble(double current) {
    progressBar.setProgress((int) (current * MAX_VISIBLE_VALUE / maxValue));
    percentage.setText(getResources().getString(R.string.valueBar_percentage,
      (int) (current * 100 / maxValue)));
    updateValuesDouble(current, maxValue);
  }

  public void setValueInteger(int current) {
    progressBar.setProgress(current * MAX_VISIBLE_VALUE / (int) maxValue);
    percentage.setText(getResources().getString(R.string.valueBar_percentage,
      (int) (current * 100 / maxValue)));
    updateValuesInteger(current, (int) maxValue);
    updateIconColor(maxValue);
  }

  public void setValueDouble(double max, double current) {
    maxValue = max;
    if (!(max > 0))
      setValueIfMaxZero((int) Math.ceil(current));
    else {
      progressBar.setProgress((int) (current * MAX_VISIBLE_VALUE / max));
      percentage.setText(getResources().getString(R.string.valueBar_percentage,
        (int) (current * 100 / max)));
    }
    updateValuesDouble(current, max);
    updateIconColor(max);
  }

  public void setValueInteger(int max, int current) {
    maxValue = max;
    if (max == 0)
      setValueIfMaxZero(current);
    else {
      progressBar.setProgress(current * MAX_VISIBLE_VALUE / max);
      percentage.setText(getResources().getString(R.string.valueBar_percentage,
        current * 100 / max));
    }
    updateValuesInteger(current, max);
    updateIconColor(max);
  }

  private void setValueIfMaxZero(int current) {
    progressBar.setProgress(MAX_VISIBLE_VALUE);
    if (current == 0) progressBar.setProgress(0);
    percentage.setText(getResources().getString(
      R.string.valueBar_percentage, current > 0 ? 100 : 0));
  }

  private void updateValuesInteger(int current, int max) {
    if (units == null) {
      values.setText(String.format(Locale.US, getResources().getString(
        R.string.valueBar_valueInteger), current, max));
    } else {
      values.setText(String.format(Locale.US, getResources().getString(
        R.string.valueBar_valueIntegerUnits), current, units));
    }
  }

  private void updateValuesDouble(double current, double max) {
    if (units == null) {
      values.setText(String.format(Locale.US, getResources().getString(
        R.string.valueBar_valueFloat), current, max));
    } else {
      values.setText(String.format(Locale.US, getResources().getString(
        R.string.valueBar_valueFloatUnits), current, units));
    }
  }

  private void updateIconColor(double maxValue) {
    if (!hasIcon) return;
    iconView.getDrawable().setColorFilter(
      maxValue == 0.0 ? colorBg : colorFg, PorterDuff.Mode.SRC_IN);
  }
}
