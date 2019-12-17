package ru.didim99.tstu.core.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import java.util.ArrayList;
import java.util.Arrays;
import ru.didim99.tstu.core.graphics.utils.Axis;
import ru.didim99.tstu.core.graphics.utils.Face;
import ru.didim99.tstu.core.graphics.utils.Mat4;
import ru.didim99.tstu.core.graphics.utils.Model;
import ru.didim99.tstu.core.graphics.utils.Projection;
import ru.didim99.tstu.core.graphics.utils.Vec4;
import ru.didim99.tstu.core.graphics.utils.VertexHolder;
import ru.didim99.tstu.ui.view.DrawerView;

import static java.lang.Math.ceil;
import static ru.didim99.tstu.utils.Utils.lerp;

/**
 * Created by didim99 on 09.03.19.
 */
public class ModelRenderer extends AsyncRenderer implements Scene {
  // Minimum & maximum parameters values
  public static final int TRN_MIN = -100;
  public static final int TRN_MAX = 100;
  public static final int SCL_MIN = 50;
  public static final int SCL_MAX = 500;
  public static final int ROT_MIN = -360;
  public static final int ROT_MAX = 360;
  public static final int KD_MIN = 0;
  public static final int KD_MAX = 100;
  public static final double TRN_FACTOR = 0.1;
  public static final double SCL_FACTOR = 0.01;
  public static final double KD_FACTOR = 0.01;
  // Default parameters values
  private static final boolean DEFAULT_DRAW_AXIS = true;
  private static final boolean DEFAULT_NEGATIVE_AXIS = false;
  private static final boolean DEFAULT_SYNC_SCALE = true;
  private static final boolean DEFAULT_VNORMALS = true;
  private static final boolean DEFAULT_USE_LAMP = false;
  private static final double DEFAULT_TRN = 0;
  private static final double DEFAULT_SCL = 1.0;
  private static final int DEFAULT_ROT = 0;
  private static final double DEFAULT_KD = 1.0;
  private static final double DEFAULT_LP = 10d;
  // Internal constants
  private static final int DSS = 50;
  private static final int DEFAULT_WIDTH = 500;
  private static final int DEFAULT_HEIGHT = 500;
  private static final double AXIS_LENGTH = 100;
  private static final int DEFAULT_DARK = 0xff040c30;
  private static final int DEFAULT_LIGHT = 0xffdbebff;

  public static final class TransformType {
    public static final int TRANSLATE = 0;
    public static final int SCALE     = 1;
    public static final int ROTATE    = 2;
  }

  private boolean configured;
  private double[] zBuffer;
  private Projection projection;
  private Mat4 modelTransform;
  private Mat4 normalTransform;
  private Mat4 sceneTransform;
  private Axis axis;

  public ModelRenderer(int type, DrawerView target, Config config) {
    super(target, Config.Type.SCENE, config,
      DEFAULT_WIDTH, DEFAULT_HEIGHT, true);
    this.modelTransform = new Mat4();
    this.normalTransform = new Mat4();
    this.sceneTransform = new Mat4();
    this.configured = false;

    projection = new Projection(
      this.config.prConfig, this::onSceneChanged);
    this.config.prConfig = projection.getConfig();

    if (config == null) {
      this.config.modelType = type;
      this.config.models = new ArrayList<>();
      this.config.drawAxis = DEFAULT_DRAW_AXIS;
      this.config.negativeAxis = DEFAULT_NEGATIVE_AXIS;
      this.config.useVNormals = DEFAULT_VNORMALS;
      this.config.useLamp = DEFAULT_USE_LAMP;
      this.config.lampPos = new Vec4(DEFAULT_LP, DEFAULT_LP, -DEFAULT_LP);
      this.config.kd = DEFAULT_KD;
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
    config.translate.x(translate);
    onSceneChanged();
  }

  public void setTranslateY(double translate) {
    config.translate.y(translate);
    onSceneChanged();
  }

  public void setTranslateZ(double translate) {
    config.translate.z(translate);
    onSceneChanged();
  }

  public void setScaleX(double scale) {
    config.scale.x(scale);
    onSceneChanged();
  }

  public void setScaleY(double scale) {
    config.scale.y(scale);
    onSceneChanged();
  }

  public void setScaleZ(double scale) {
    config.scale.z(scale);
    onSceneChanged();
  }

  public void setScale(double scale) {
    config.scale.x(scale);
    config.scale.y(scale);
    config.scale.z(scale);
    onSceneChanged();
  }

  public void setRotateX(double rotate) {
    config.rotate.x(rotate);
    onSceneChanged();
  }

  public void setRotateY(double rotate) {
    config.rotate.y(rotate);
    onSceneChanged();
  }

  public void setRotateZ(double rotate) {
    config.rotate.z(rotate);
    onSceneChanged();
  }

  public void setUseVNormals(boolean state) {
    config.useVNormals = state;
    for (Model model : config.models)
      model.useVNormals(state);
    onSceneChanged();
  }

  public void setUseLamp(boolean useLamp) {
    config.useLamp = useLamp;
    onSceneChanged();
  }

  public void setKd(double kd) {
    config.kd = kd;
    for (Model model : config.models)
      model.setKd(kd);
    onSceneChanged();
  }

  public void onModelLoaded(Model model) {
    if (model != null) {
      model.setKd(config.kd);
      model.useVNormals(config.useVNormals);
      config.models.add(model);
      onSceneChanged();
    }
  }

  @Override
  public void clear() {
    if (config.models != null) {
      config.models.clear();
      publishProgress();
    }
  }

  public Config clearTransform() {
    onClearTransform();
    onSceneChanged();
    return config;
  }

  void onClearTransform() {
    config.translate = new Vec4(DEFAULT_TRN, DEFAULT_TRN, DEFAULT_TRN);
    config.scale = new Vec4(DEFAULT_SCL, DEFAULT_SCL, DEFAULT_SCL);
    config.rotate = new Vec4(DEFAULT_ROT, DEFAULT_ROT, DEFAULT_ROT);
    config.syncScale = DEFAULT_SYNC_SCALE;
  }

  void onSceneChanged() {
    axis = new Axis(AXIS_LENGTH, config.negativeAxis);
    modelTransform.loadIdentity();
    modelTransform.rotate(config.rotate);
    normalTransform.load(modelTransform);
    modelTransform.scale(config.scale);
    modelTransform.translate(config.translate);
    modelTransform.multiply(sceneTransform);
    publishProgress();
  }

  private void configure(Canvas canvas) {
    if (configured) return;
    int w = canvas.getWidth();
    int h = canvas.getHeight();
    config.width = w / 2;
    config.height = h / 2;
    zBuffer = new double[w * h];
    bitmap = Bitmap.createBitmap(
      w, h, Bitmap.Config.ARGB_8888);
    configured = true;
    onSceneChanged();
  }

  private void render() {
    Point center = new Point(config.width, config.height);
    if (config.drawAxis)
      axis.render(sceneTransform, projection, center);
    for (Model model : config.models) {
      model.render(modelTransform, projection, center);
      if (model.getType() == Model.Type.FACE) {
        model.rotateNormals(normalTransform);
        if (config.useLamp) model.calcLight(config.lampPos);
      }
    }
  }

  @Override
  public void draw(Canvas canvas) {
    configure(canvas);
    render();
    Arrays.fill(zBuffer, 0);
    canvas.drawColor(config.colorBg);
    Paint paint = new Paint();
    if (config.drawAxis)
      drawModel(axis, canvas, paint);
    for (Model model : config.models) {
      if (model.isEmpty()) continue;
      if (config.modelType == Model.Type.EDGE
        || model.getType() == Model.Type.EDGE)
        drawModel(model, canvas, paint);
      else if (model.getType() == Model.Type.FACE)
        drawModel(model, canvas);
    }
  }

  private void drawModel(Model model, Canvas canvas, Paint paint) {
    paint.setColor(model.getColor());
    paint.setStrokeWidth(model.getLineWidth());
    canvas.drawLines(model.getRastered(),
      0, model.getRasteredSize(), paint);
  }

  private void drawModel(Model model, Canvas canvas) {
    int w, h, yCurr, xCurr, len, offset, iay, iby, icy, color;
    double dxStart, dcStart, dzStart, dxEnd, dcEnd, dzEnd;
    double xStart, xEnd, cStart, cEnd, zStart, zEnd;
    double tmp, k, dc, dz, x, cc, z, ay, by, bcy;
    VertexHolder a, b, c, vtmp;

    w = config.width * 2 - 1;
    h = config.height * 2 - 1;
    bitmap.eraseColor(0x00000000);
    for (Face face : model.getFaces()) {
      a = new VertexHolder(face.v1.rastered(), face.n1.transformed());
      b = new VertexHolder(face.v2.rastered(), face.n2.transformed());
      c = new VertexHolder(face.v3.rastered(), face.n3.transformed());

      // считаем освещенность в вершинах
      if (config.useLamp) {
        a.c = (float) face.n1.light;
        b.c = (float) face.n2.light;
        c.c = (float) face.n3.light;
      } else {
        a.c = a.nz < 0 ? -a.nz : 0;
        b.c = b.nz < 0 ? -b.nz : 0;
        c.c = c.nz < 0 ? -c.nz : 0;
      }

      // отсортируем вершины грани по y
      if (a.y > b.y) { vtmp = a; a = b; b = vtmp; }
      if (a.y > c.y) { vtmp = a; a = c; c = vtmp; }
      if (b.y > c.y) { vtmp = b; b = c; c = vtmp; }
      ay = ceil(a.y); iay = (int) ay;
      by = ceil(b.y); iby = (int) by;
      icy = (int) ceil(c.y);
      if (icy == iay) continue;

      // посчитаем du/dsx и dv/dsx
      // считаем по самой длинной линии (т.е. проходящей через вершину B)
      a.z = 1 / (a.z + 10000);
      b.z = 1 / (b.z + 10000);
      c.z = 1 / (c.z + 10000);
      k = (b.y - a.y) / (c.y - a.y);
      xStart = a.x + (c.x - a.x) * k;
      cStart = a.c + (c.c - a.c) * k;
      zStart = a.z + (c.z - a.z) * k;
      xEnd = b.x;
      cEnd = b.c;
      zEnd = b.z;
      dc = (cStart - cEnd) / (xStart - xEnd);
      dz = (zStart - zEnd) / (xStart - xEnd);

      xStart = a.x;
      cStart = a.c;
      zStart = a.z;
      tmp = c.y - a.y;
      dxStart = (c.x - a.x) / tmp;
      dcStart = (c.c - a.c) / tmp;
      dzStart = (c.z - a.z) / tmp;
      bcy = c.y - b.y;

      if (by > ay) {
        xEnd = a.x;
        cEnd = a.c;
        zEnd = a.z;
        tmp = b.y - a.y;
        dxEnd = (b.x - a.x) / tmp;
        dcEnd = (b.c - a.c) / tmp;
        dzEnd = (b.z - a.z) / tmp;
      } else {
        xEnd = b.x;
        cEnd = b.c;
        zEnd = b.z;
        dxEnd = (c.x - b.x) / bcy;
        dcEnd = (c.c - b.c) / bcy;
        dzEnd = (c.z - b.z) / bcy;
      }

      for (yCurr = iay; yCurr <= icy; yCurr++) {
        if (yCurr > h) break;
        if (yCurr >= 0) {
          if (yCurr == iby) {
            xEnd = b.x;
            cEnd = b.c;
            zEnd = b.z;
            dxEnd = (c.x - b.x) / bcy;
            dcEnd = (c.c - b.c) / bcy;
            dzEnd = (c.z - b.z) / bcy;
          }

          // xStart должен находиться левее xEnd
          if (xStart > xEnd) {
            x = xEnd;
            cc = cEnd;
            z = zEnd;
            len = (int) (ceil(xStart) - ceil(xEnd));
          } else {
            x = xStart;
            cc = cStart;
            z = zStart;
            len = (int) (ceil(xEnd) - ceil(xStart));
          }

          // текстурируем строку
          xCurr = (int) ceil(x);
          offset = yCurr * w + xCurr;
          while (len-- > 0) {
            if (xCurr > w) break;
            if (xCurr >= 0) {
              if (zBuffer[offset] < z) {
                color = lerp(DEFAULT_DARK, DEFAULT_LIGHT, 1 - cc);
                bitmap.setPixel(xCurr, yCurr, color);
                zBuffer[offset] = z;
              }
            }

            cc += dc;
            z += dz;
            xCurr++;
            offset++;
          }
        }

        // сдвигаем начальные и конечные значения x/u/v/(1/z)
        xStart += dxStart;
        cStart += dcStart;
        zStart += dzStart;
        xEnd += dxEnd;
        cEnd += dcEnd;
        zEnd += dzEnd;
      }
    }

    canvas.drawBitmap(bitmap, 0, 0, null);
  }
}
