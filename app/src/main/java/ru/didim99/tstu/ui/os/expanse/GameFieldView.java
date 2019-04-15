package ru.didim99.tstu.ui.os.expanse;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.os.expanse.GameField;

/**
 * Created by didim99 on 11.04.19.
 */
public class GameFieldView extends View {
  private static final int CELL_SIZE_STEP = 10;
  private static final int DEFAULT_SIZE = 10;
  private static final int GRID_WIDTH = 2;

  // Logic configuration
  private GameField gameField;
  private int sizeX, sizeY;
  // View configuration
  private int width, height;
  private int offsetX, offsetY;
  private int cellSize;
  private int gridColor;
  private Paint paint;

  public GameFieldView(Context context) {
    this(context, null);
  }

  public GameFieldView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public GameFieldView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray array = context.obtainStyledAttributes(
      attrs, R.styleable.GameFieldView, defStyleAttr, 0);
    gridColor = array.getColor(R.styleable.GameFieldView_gridColor, Color.BLACK);
    array.recycle();

    sizeX = sizeY = DEFAULT_SIZE;
    paint = new Paint();
  }

  public void setSource(GameField field) {
    sizeX = field.getWidth();
    sizeY = field.getHeight();
    gameField = field;
    onFieldChanged();
  }

  private void onFieldChanged() {
    double cs = Math.min(width / sizeX, height / sizeY);
    cellSize = (int) (cs - cs % CELL_SIZE_STEP);
    offsetX = (width - sizeX * cellSize) / 2;
    offsetY = (height - sizeY * cellSize) / 2;
    invalidate();
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    width = w; height = h;
    onFieldChanged();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    drawGrid(canvas);
    drawCells(canvas);
  }

  private void drawGrid(Canvas canvas) {
    paint.setStrokeCap(Paint.Cap.ROUND);
    paint.setStrokeWidth(GRID_WIDTH);
    paint.setColor(gridColor);
    int start, end, pos;

    pos = offsetX;
    start = offsetY;
    end = height - offsetY;
    for (int i = 0; i <= sizeX; i++) {
      canvas.drawLine(pos, start, pos, end, paint);
      pos += cellSize;
    }

    pos = offsetY;
    start = offsetX;
    end = width - offsetX;
    for (int i = 0; i <= sizeY; i++) {
      canvas.drawLine(start, pos, end, pos, paint);
      pos += cellSize;
    }
  }

  private void drawCells(Canvas canvas) {
    if (gameField == null) return;
    int columnSize = cellSize * -sizeY;
    Rect rect = new Rect(offsetX, offsetY,
      offsetX + cellSize, offsetY + cellSize);

    for (int x = 0; x < sizeX; x++) {
      for (int y = 0; y < sizeY; y++) {
        GameField.Cell cell = gameField.getCell(x, y);
        if (!cell.isEmpty()) {
          paint.setColor(cell.getColor());
          canvas.drawRect(rect, paint);
        }

        rect.offset(0, cellSize);
      }

      rect.offset(cellSize, columnSize);
    }
  }
}
