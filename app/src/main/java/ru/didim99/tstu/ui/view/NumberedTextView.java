package ru.didim99.tstu.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Dimension;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 10.03.19.
 */
public class NumberedTextView extends LinearLayout {
  private static final String NEWLINE = "\n";

  private final TextView lines;
  private final TextView content;

  public NumberedTextView(Context context) {
    this(context, null, 0);
  }

  public NumberedTextView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public NumberedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    View itemView = LayoutInflater.from(context).inflate(
      R.layout.view_numbered_text, this);
    lines = itemView.findViewById(R.id.lines);
    content = itemView.findViewById(R.id.content);

    float size = lines.getTextSize();
    int color = lines.getTextColors().getDefaultColor();
    TypedArray array = context.obtainStyledAttributes(
      attrs, R.styleable.NumberedTextView, defStyleAttr, 0);
    color = array.getColor(R.styleable.NumberedTextView_lineNumColor, color);
    size = array.getDimension(R.styleable.NumberedTextView_textSize, size);
    array.recycle();

    lines.setTextColor(color);
    lines.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    content.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
  }

  public void setText(String text) {
    if (text == null) {
      content.setText(null);
      lines.setText(null);
      return;
    }

    int count = text.split(NEWLINE).length;
    int[] numbers = new int[count];
    for (int i = 0; i < count; i++) numbers[i] = i + 1;
    String[] strNumbers = Utils.intArrayToStringArray(numbers);
    lines.setText(Utils.joinStr(NEWLINE, strNumbers));
    content.setText(text);
  }
}
