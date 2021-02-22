package ru.didim99.tstu.ui.knowledge;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.knowledge.neuralnet.Net;
import ru.didim99.tstu.core.knowledge.neuralnet.NetConfig;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 15.02.21.
 */

public class NetView extends View {
  private static final double MARGIN = 0.1; // %
  private static final int DOT_RADIUS = 40; // px
  private static final int GRID_WIDTH = 6;  // px

  private Net net;
  private List<List<PointF>> points;
  private boolean hasBias;
  // View configuration
  private final Paint gridPaint;
  private final Paint dotPaint;
  private final int gridColor;
  private final int gridPosColor;
  private final int gridNegColor;
  private final int dotColor;
  private final int inputColor;
  private final int outputColor;
  private final int biasColor;
  private int width, height;
  private int eWidth, eHeight;
  private int vMargin, hMargin;
  private int vGap, hGap;

  public NetView(Context context) {
    this(context, null);
  }

  public NetView(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public NetView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray array = context.obtainStyledAttributes(
      attrs, R.styleable.NetView, defStyleAttr, 0);
    gridColor = array.getColor(R.styleable.NetView_gridDefaultColor, Color.BLACK);
    gridPosColor = array.getColor(R.styleable.NetView_gridPositiveColor, Color.GREEN);
    gridNegColor = array.getColor(R.styleable.NetView_gridNegativeColor, Color.RED);
    dotColor = array.getColor(R.styleable.NetView_dotColor, Color.BLUE);
    inputColor = array.getColor(R.styleable.NetView_inputColor, Color.YELLOW);
    outputColor = array.getColor(R.styleable.NetView_outputColor, Color.GREEN);
    biasColor = array.getColor(R.styleable.NetView_biasColor, Color.RED);
    array.recycle();

    gridPaint = new Paint();
    gridPaint.setStrokeWidth(GRID_WIDTH);
    gridPaint.setColor(gridColor);
    dotPaint = new Paint();
    dotPaint.setStrokeCap(Paint.Cap.ROUND);
    dotPaint.setStrokeWidth(DOT_RADIUS * 2);
    dotPaint.setColor(dotColor);
  }

  public void setDataSource(@Nullable Net source) {
    this.net = source;
    onConfigurationChanged();
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    width = w; height = h;
    onConfigurationChanged();
  }

  private void onConfigurationChanged() {
    if (width == 0 || height == 0) return;
    if (net != null) {
      computeGeometry();
      arrangePoints();
    }

    invalidate();
  }

  private void computeGeometry() {
    NetConfig config = net.getConfig();
    hasBias = config.isHasBias();
    vMargin = (int) (height * MARGIN / 2) + DOT_RADIUS;
    hMargin = (int) (width * MARGIN / 2) + DOT_RADIUS;
    eHeight = height - vMargin * 2;
    eWidth = width - hMargin * 2;
    vGap = eHeight / (config.getMaxLayerSize() - 1);
    hGap = eWidth / (config.getLayersCount() - 1);
  }

  private void arrangePoints() {
    NetConfig config = net.getConfig();
    points = new ArrayList<>(config.getLayersCount());

    int lastLayer = config.getLayersCount();
    int layerIndex = 1;

    float x = hMargin, y;
    for (int count : config.getAllLayers()) {
      if (hasBias && layerIndex++ < lastLayer) count++;
      y = vMargin + (eHeight - vGap * (count - 1)) / 2f;
      List<PointF> layer = new ArrayList<>(count);
      for (int i = 0; i < count; i++) {
        layer.add(new PointF(x, y));
        y += vGap;
      }

      points.add(layer);
      x += hGap;
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (net == null) return;

    double minW = net.getMinWeight();
    double maxW = net.getMaxWeight();
    int lastLayer = points.size() - 1;

    int currentDotColor, edgeColor;
    for (int index = 0; index <= lastLayer; index++) {
      if (index == 0) currentDotColor = inputColor;
      else if (index == lastLayer) currentDotColor = outputColor;
      else currentDotColor = dotColor;

      List<PointF> layer = points.get(index);
      int lastNeuron = layer.size() - 1;

      if (index < lastLayer) {
        List<PointF> nextLayer = points.get(index + 1);
        int lastTarget = nextLayer.size() - 1;
        int ci, ni = 0;

        for (PointF target : nextLayer) {
          if (hasBias && index + 1 < lastLayer && ni == lastTarget)
            continue;

          ci = 0;
          double weight;
          for (PointF source : layer) {
            weight = net.getWeight(index + 1, ni, ci);
            edgeColor = weight > 0 ? gridPosColor : gridNegColor;
            weight = Utils.norm(weight, 0, weight > 0 ? maxW : minW);
            gridPaint.setColor(Utils.lerp(edgeColor, gridColor, weight));
            canvas.drawLine(source.x, source.y,
              target.x, target.y, gridPaint);
            ci++;
          }

          ni++;
        }
      }

      int neuron = 0;
      for (PointF point : layer) {
        if (hasBias && index < lastLayer && neuron == lastNeuron)
          currentDotColor = biasColor;

        dotPaint.setColor(currentDotColor);
        dotPaint.setStrokeWidth(DOT_RADIUS * 2);
        canvas.drawPoint(point.x, point.y, dotPaint);
        dotPaint.setStrokeWidth((DOT_RADIUS - GRID_WIDTH) * 2);
        dotPaint.setColor(Utils.lerp(currentDotColor, gridColor,
          net.getNeuronOutput(index, neuron++)));
        canvas.drawPoint(point.x, point.y, dotPaint);
      }
    }
  }
}
