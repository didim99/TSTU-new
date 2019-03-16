package ru.didim99.tstu.core.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.ArrayList;
import ru.didim99.tstu.core.graphics.utils.Axis;
import ru.didim99.tstu.core.graphics.utils.EdgeModel;
import ru.didim99.tstu.core.graphics.utils.Mat4;
import ru.didim99.tstu.core.graphics.utils.Projection;
import ru.didim99.tstu.core.graphics.utils.Vec4;
import ru.didim99.tstu.ui.view.DrawerView;

/**
 * Created by didim99 on 09.03.19.
 */
public class EdgeRenderer extends AsyncRenderer implements Scene {
  // Minimum & maximum parameters values
  public static final int TRN_MIN = -100;
  public static final int TRN_MAX = 100;
  public static final int SCL_MIN = 50;
  public static final int SCL_MAX = 500;
  public static final int ROT_MIN = -360;
  public static final int ROT_MAX = 360;
  public static final double TRN_FACTOR = 0.1;
  public static final double SCL_FACTOR = 0.01;
  // Default parameters values
  private static final boolean DEFAULT_DRAW_AXIS = true;
  private static final boolean DEFAULT_NEGATIVE_AXIS = false;
  private static final boolean DEFAULT_SYNC_SCALE = true;
  private static final double DEFAULT_TRN = 0;
  private static final double DEFAULT_SCL = 1.0;
  private static final int DEFAULT_ROT = 0;
  // Internal constants
  private static final int DSS = 50;
  private static final int DEFAULT_WIDTH = 500;
  private static final int DEFAULT_HEIGHT = 500;
  private static final double AXIS_LENGTH = 100;

  public static final class TransformType {
    public static final int TRANSLATE = 0;
    public static final int SCALE     = 1;
    public static final int ROTATE    = 2;
  }

  private boolean configured;
  private Projection projection;
  private Mat4 modelTransform;
  private Mat4 sceneTransform;
  private Axis axis;

  public EdgeRenderer(DrawerView target, Config config) {
    super(target, Config.Type.SCENE, config,
      DEFAULT_WIDTH, DEFAULT_HEIGHT, true);
    this.modelTransform = new Mat4();
    this.sceneTransform = new Mat4();
    this.configured = false;

    projection = new Projection(
      this.config.prConfig, this::onSceneChanged);
    this.config.prConfig = projection.getConfig();

    if (config == null) {
      this.config.models = new ArrayList<>();
      this.config.drawAxis = DEFAULT_DRAW_AXIS;
      this.config.negativeAxis = DEFAULT_NEGATIVE_AXIS;
      clearTransform();
    }

    sceneTransform.scale(new Vec4(DSS, DSS, DSS));
    onSceneCreated(this);
    onSceneChanged();
  }

  public Projection getProjection() {
    return projection;
  }

  public void setDrawAxis(boolean drawAxis) {
    config.drawAxis = drawAxis;
    onSceneChanged();
  }

  public void setNegativeAxis(boolean negativeAxis) {
    config.negativeAxis = negativeAxis;
    onSceneChanged();
  }

  public void setSyncScale(boolean syncScale) {
    config.syncScale = syncScale;
  }

  public void setTranslateX(double translate) {
    config.translate.setX(translate);
    onSceneChanged();
  }

  public void setTranslateY(double translate) {
    config.translate.setY(translate);
    onSceneChanged();
  }

  public void setTranslateZ(double translate) {
    config.translate.setZ(translate);
    onSceneChanged();
  }

  public void setScaleX(double scale) {
    config.scale.setX(scale);
    onSceneChanged();
  }

  public void setScaleY(double scale) {
    config.scale.setY(scale);
    onSceneChanged();
  }

  public void setScaleZ(double scale) {
    config.scale.setZ(scale);
    onSceneChanged();
  }

  public void setScale(double scale) {
    config.scale.setX(scale);
    config.scale.setY(scale);
    config.scale.setZ(scale);
    onSceneChanged();
  }

  public void setRotateX(double rotate) {
    config.rotate.setX(rotate);
    onSceneChanged();
  }

  public void setRotateY(double rotate) {
    config.rotate.setY(rotate);
    onSceneChanged();
  }

  public void setRotateZ(double rotate) {
    config.rotate.setZ(rotate);
    onSceneChanged();
  }

  public void onModelLoaded(EdgeModel model) {
    if (model != null) {
      config.models.add(model);
      onSceneChanged();
    }
  }

  public void clearScene() {
    if (config.models != null) {
      config.models.clear();
      onSceneChanged();
    }
  }

  public Config clearTransform() {
    config.translate = new Vec4(DEFAULT_TRN, DEFAULT_TRN, DEFAULT_TRN);
    config.scale = new Vec4(DEFAULT_SCL, DEFAULT_SCL, DEFAULT_SCL);
    config.rotate = new Vec4(DEFAULT_ROT, DEFAULT_ROT, DEFAULT_ROT);
    config.syncScale = DEFAULT_SYNC_SCALE;
    onSceneChanged();
    return config;
  }

  private void onSceneChanged() {
    axis = new Axis(AXIS_LENGTH, config.negativeAxis);
    modelTransform.loadIdentity();
    modelTransform.rotate(config.rotate);
    modelTransform.scale(config.scale);
    modelTransform.translate(config.translate);
    modelTransform.multiply(sceneTransform);
    publishProgress();
  }

  private void configure(Canvas canvas) {
    if (configured) return;
    config.width = canvas.getWidth() / 2;
    config.height = canvas.getHeight() / 2;
    configured = true;
    onSceneChanged();
  }

  private void render() {
    if (config.drawAxis)
      axis.render(sceneTransform, projection);
    for (EdgeModel model : config.models)
      model.render(modelTransform, projection);
  }

  @Override
  public void draw(Canvas canvas) {
    configure(canvas);
    render();
    canvas.drawColor(config.colorBg);
    Paint paint = new Paint();
    if (config.drawAxis)
      drawModel(axis, canvas, paint);
    for (EdgeModel model : config.models)
      drawModel(model, canvas, paint);
  }

  private void drawModel(EdgeModel model, Canvas canvas, Paint paint) {
    float[] rastered = model.getRastered();
    for (int i = 0; i < rastered.length; i++) {
      if (i % 2 == 0) rastered[i] += config.width;
      else rastered[i] = (float) config.height - rastered[i];
    }

    paint.setColor(model.getColor());
    paint.setStrokeWidth(model.getLineWidth());
    canvas.drawLines(rastered, paint);
  }
}
