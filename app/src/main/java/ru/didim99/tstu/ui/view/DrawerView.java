package ru.didim99.tstu.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import ru.didim99.tstu.core.graphics.Scene;

/**
 * Created by didim99 on 13.02.19.
 */
public class DrawerView extends View {
  private Bitmap bitmap;
  private Rect src, dst;
  private Paint paint;
  private Scene scene;

  public DrawerView(Context context) {
    super(context);
  }

  public DrawerView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public void setSource(Bitmap bitmap, boolean antiAlias) {
    this.src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    this.dst = new Rect();
    this.bitmap = bitmap;
    if (!antiAlias) {
      paint = new Paint();
      paint.setAntiAlias(false);
    }
  }

  public void setSource(Scene scene) {
    this.scene = scene;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (src != null && dst != null) {
      dst.set(0, 0, getWidth(), getHeight());
      if (dst.width() < dst.height()) {
        int newH = src.bottom * dst.right / src.right;
        dst.top = (dst.bottom - newH) >> 1;
        dst.bottom = (dst.bottom + newH) >> 1;
      } else {
        int newW = src.right * dst.bottom / src.bottom;
        dst.left = (dst.right - newW) >> 1;
        dst.right = (dst.right + newW) >> 1;
      }
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (bitmap != null)
      canvas.drawBitmap(bitmap, src, dst, paint);
    else if (scene != null)
      scene.draw(canvas);
  }
}
