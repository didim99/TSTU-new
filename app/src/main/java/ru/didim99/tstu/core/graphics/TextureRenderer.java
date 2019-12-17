package ru.didim99.tstu.core.graphics;

import android.graphics.Point;
import ru.didim99.tstu.core.graphics.utils.Face;
import ru.didim99.tstu.core.graphics.utils.Mat4;
import ru.didim99.tstu.core.graphics.utils.Model;
import ru.didim99.tstu.core.graphics.utils.Projection;
import ru.didim99.tstu.core.graphics.utils.Texture;
import ru.didim99.tstu.core.graphics.utils.VertexHolder;
import ru.didim99.tstu.core.graphics.utils.Vec4;
import ru.didim99.tstu.ui.view.DrawerView;

import static java.lang.Math.ceil;

/**
 * Created by didim99 on 06.04.19.
 */
public class TextureRenderer extends AsyncRenderer {
  // Minimum & maximum parameters values
  public static final int TRN_MIN = -100;
  public static final int TRN_MAX = 100;
  public static final int SCL_MIN = 50;
  public static final int SCL_MAX = 500;
  public static final int ROT_MIN = -360;
  public static final int ROT_MAX = 360;
  public static final double TRN_FACTOR = 0.1;
  public static final double SCL_FACTOR = 0.01;
  // Internal constants
  private static final boolean DEFAULT_FILTERING = false;
  private static final int DEFAULT_COLOR = 0xffdddddd;
  private static final double DEFAULT_TRN = 0;
  private static final double DEFAULT_SCL = 1.0;
  private static final double DEFAULT_ROT = 0;
  private static final int VP_WIDTH = 500;
  private static final int VP_HEIGHT = 500;
  private static final int DSS = 50;
  private static final int DTX = VP_WIDTH / 2;
  private static final int DTY = VP_HEIGHT / 2;
  private static final int DTZ = 0;

  private Mat4 modelTransform;
  private Mat4 sceneTransform;
  private Projection projection;
  private Model plane;

  public TextureRenderer(DrawerView target, Config config) {
    super(target, Config.Type.BITMAP, config,
      VP_WIDTH, VP_HEIGHT, false);
    projection = new Projection(null, null);
    modelTransform = new Mat4();
    sceneTransform = new Mat4();

    if (config == null) {
      this.config.antiAlias = DEFAULT_FILTERING;
      clearTransform();
    }

    sceneTransform.scale(new Vec4(DSS, DSS, DSS));
    sceneTransform.translate(new Vec4(DTX, DTY, DTZ));
  }

  public void onModelLoaded(Model model) {
    if (model != null) {
      this.plane = model;
      clearTransform();
    }
  }

  public void onTextureLoaded(Texture texture) {
    if (texture != null) {
      texture.enableFiltering(config.antiAlias);
      config.texture = texture;
      onSceneChanged();
    }
  }

  public Config clearTransform() {
    config.translate = new Vec4(DEFAULT_TRN, DEFAULT_TRN, DEFAULT_TRN);
    config.scale = new Vec4(DEFAULT_SCL, DEFAULT_SCL, DEFAULT_SCL);
    config.rotate = new Vec4(DEFAULT_ROT, DEFAULT_ROT, DEFAULT_ROT);
    if (plane != null) onSceneChanged();
    return config;
  }

  public void clearTexture() {
    config.texture.recycle();
    config.texture = null;
    onSceneChanged();
  }

  public void setTranslateX(double translate) {
    config.translate.x(translate);
    onSceneChanged();
  }

  public void setTranslateY(double translate) {
    config.translate.y(translate);
    onSceneChanged();
  }

  public void setScale(double scale) {
    config.scale.x(scale);
    config.scale.y(scale);
    onSceneChanged();
  }

  public void setRotate(double rotate) {
    config.rotate.z(rotate);
    onSceneChanged();
  }

  public void setTextureFiltering(boolean filter) {
    config.texture.enableFiltering(filter);
    config.antiAlias = filter;
    onSceneChanged();
  }

  private void onSceneChanged() {
    prepareDraw();
    draw(plane);
  }

  private void prepareDraw() {
    modelTransform.loadIdentity();
    modelTransform.rotate(config.rotate);
    modelTransform.scale(config.scale);
    modelTransform.translate(config.translate);
    modelTransform.multiply(sceneTransform);
    plane.render(modelTransform, projection, null);
    clear();
  }

  private void draw(Model model) {
    Texture texture = config.texture;

    VertexHolder a, b, c, vtmp;
    double xStart, xEnd, uStart, uEnd, vStart, vEnd;
    double dxStart, duStart, dvStart, dxEnd, duEnd, dvEnd;
    double tmp, k, du, dv, x, u, v, ay, by, bcy;
    int w, h, xCurr, yCurr, len, iay, iby, icy, color;

    w = config.width - 1;
    h = config.height - 1;
    color = DEFAULT_COLOR;

    // текстурирование каждой грани
    for (Face face : model.getFaces()) {
      a = new VertexHolder(face.v1.rastered, face.t1);
      b = new VertexHolder(face.v2.rastered, face.t2);
      c = new VertexHolder(face.v3.rastered, face.t3);

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
      k = (b.y - a.y) / (c.y - a.y);
      xStart = a.x + (c.x - a.x) * k;
      uStart = a.u + (c.u - a.u) * k;
      vStart = a.v + (c.v - a.v) * k;
      xEnd = b.x;
      uEnd = b.u;
      vEnd = b.v;
      du = (uStart - uEnd) / (xStart - xEnd);
      dv = (vStart - vEnd) / (xStart - xEnd);
      bcy = c.y - b.y;

      xStart = a.x;
      uStart = a.u;
      vStart = a.v;
      tmp = c.y - a.y;
      dxStart = (c.x - a.x) / tmp;
      duStart = (c.u - a.u) / tmp;
      dvStart = (c.v - a.v) / tmp;

      if (by > ay) {
        xEnd = a.x;
        uEnd = a.u;
        vEnd = a.v;
        tmp = b.y - a.y;
        dxEnd = (b.x - a.x) / tmp;
        duEnd = (b.u - a.u) / tmp;
        dvEnd = (b.v - a.v) / tmp;
      } else {
        xEnd = b.x;
        uEnd = b.u;
        vEnd = b.v;
        dxEnd = (c.x - b.x) / bcy;
        duEnd = (c.u - b.u) / bcy;
        dvEnd = (c.v - b.v) / bcy;
      }

      // текстурируем каждую строку
      for (yCurr = iay; yCurr <= icy; yCurr++) {
        if (yCurr > h) break;
        if (yCurr >= 0) {
          if (yCurr == iby) {
            xEnd = b.x;
            uEnd = b.u;
            vEnd = b.v;
            dxEnd = (c.x - b.x) / bcy;
            duEnd = (c.u - b.u) / bcy;
            dvEnd = (c.v - b.v) / bcy;
          }

          // xStart должен находиться левее xEnd
          if (xStart > xEnd) {
            x = xEnd;
            u = uEnd;
            v = vEnd;
            len = (int) (ceil(xStart) - ceil(xEnd));
          } else {
            x = xStart;
            u = uStart;
            v = vStart;
            len = (int) (ceil(xEnd) - ceil(xStart));
          }

          // текстурируем строку
          xCurr = (int) ceil(x);
          while (len-- > 0) {
            if (xCurr > w) break;
            if (xCurr >= 0) {
              if (texture != null)
                color = texture.getPixel(u, v);
              bitmap.setPixel(xCurr, yCurr, color);
            }

            u += du;
            v += dv;
            xCurr++;
          }
        }

        xStart += dxStart;
        uStart += duStart;
        vStart += dvStart;
        xEnd += dxEnd;
        uEnd += duEnd;
        vEnd += dvEnd;
      }
    }
  }
}
