package ru.didim99.tstu.core.graphics;

import java.util.ArrayList;
import ru.didim99.tstu.core.graphics.utils.Edge;
import ru.didim99.tstu.core.graphics.utils.Mat4;
import ru.didim99.tstu.core.graphics.utils.Model;
import ru.didim99.tstu.core.graphics.utils.Vec4;
import ru.didim99.tstu.core.graphics.utils.Vertex;
import ru.didim99.tstu.ui.view.DrawerView;

/**
 * Created by didim99 on 13.12.19.
 */
public class FractalRenderer extends ModelRenderer {
  private static final int FPS = 2;
  // Scene configuration
  public static final int SCL_MIN = 20;
  public static final int SCL_MAX = 300;
  public static final int ROT_MIN = -180;
  public static final int ROT_MAX = 180;
  public static final double SCL_FACTOR = 0.05;
  private static final double DEFAULT_SCL = 2.0;
  private static final boolean DEFAULT_DRAW_AXIS = false;
  // Fractal algorithm configuration
  public static final int LEVEL_MIN = 2;
  public static final int LEVEL_MAX = 8;
  public static final int BCOUNT_MIN = 1;
  public static final int BCOUNT_MAX = 10;
  public static final int BANGLE_MIN = 0;
  public static final int BANGLE_MAX = 180;
  public static final int BL_MIN = 50;
  public static final int BL_MAX = 250;
  public static final int BLF_MIN = 0;
  public static final int BLF_MAX = 200;
  public static final int BAF_MIN = 0;
  public static final int BAF_MAX = 200;
  public static final double BL_FACTOR = 0.01;
  public static final double BLF_FACTOR = 0.01;
  public static final double BAF_FACTOR = 0.01;
  private static final int DEFAULT_LEVEL = 3;
  private static final int DEFAULT_BCOUNT = 4;
  private static final int DEFAULT_ANGLE = 90;
  private static final double DEFAULT_L = 1.0;
  private static final double DEFAULT_LF = 0.5;
  private static final double DEFAULT_AF = 1.0;
  private static final boolean DEFAULT_CB = true;
  // internal fractal algorithm settings
  private static final Vec4 ROOT = new Vec4(0, 0, 0);

  private Model model;
  private ArrayList<Vertex> vertices;
  private ArrayList<Edge> edges;
  private double zGap;

  public FractalRenderer(DrawerView target, Config config) {
    super(Model.Type.EDGE, target, config);
    setFPS(FPS);

    if (config == null) {
      this.config.models.add(new Model());
      this.config.drawAxis = DEFAULT_DRAW_AXIS;
      resetBuilder();
    }

    model = this.config.models.get(0);
    vertices = model.getVertices();
    edges = model.getEdges();
    onBuilderChanged();
    onSceneChanged();
  }

  public Model getModel() {
    return model;
  }

  public boolean hasModel() {
    return !vertices.isEmpty() && !edges.isEmpty();
  }

  public void setMaxLevel(int level) {
    config.maxLevel = level;
    onBuilderChanged();
  }

  public void setBranchCount(int count) {
    config.branchCount = count;
    onBuilderChanged();
  }

  public void setCentralBranch(boolean state) {
    config.centralBranch = state;
    onBuilderChanged();
  }

  public void setBranchL(double l) {
    config.branchL = l;
    onBuilderChanged();
  }

  public void setBranchAngle(double angle) {
    config.branchAngle = angle;
    onBuilderChanged();
  }

  public void setBranchLF(double lf) {
    config.branchLF = lf;
    onBuilderChanged();
  }

  public void setBranchAF(double af) {
    config.branchAF = af;
    onBuilderChanged();
  }

  private void onBuilderChanged() {
    try {
      zGap = 360.0 / config.branchCount;
      generateFractal();
      publishProgress();
    } catch (InterruptedException ignored) {}
  }

  @Override
  void animateInternal() throws InterruptedException {
    generateFractal();
  }

  @Override
  void onFrame() throws InterruptedException {
    model.configure();
    super.onFrame();
  }

  @Override
  public void clear() {
    if (model != null) {
      model.clearData();
      publishProgress();
    }
  }

  @Override
  void onClearTransform() {
    super.onClearTransform();
    config.scale = new Vec4(DEFAULT_SCL, DEFAULT_SCL, DEFAULT_SCL);
  }

  public Config resetBuilder() {
    this.config.maxLevel = DEFAULT_LEVEL;
    this.config.branchCount = DEFAULT_BCOUNT;
    this.config.centralBranch = DEFAULT_CB;
    this.config.branchL = DEFAULT_L;
    this.config.branchAngle = DEFAULT_ANGLE;
    this.config.branchLF = DEFAULT_LF;
    this.config.branchAF = DEFAULT_AF;
    onBuilderChanged();
    return config;
  }

  private void generateFractal() throws InterruptedException {
    if (model == null) return;
    model.clearData();
    // Add initial branch
    edges.add(new Edge(0, 1));
    vertices.add(new Vertex(ROOT));
    vertices.add(new Vertex(new Vec4(ROOT)
      .sub(new Vec4(0, config.branchL * 2, 0))));
    model.configure();
    // Add all other branches
    branch(0, 0, 0, config.branchL, config.branchAngle);
  }

  private void branch(int rootId, int level, int n, double l, double a)
    throws InterruptedException {
    if (level > config.maxLevel) return;
    int nextLevel = level + 1;

    double lNext = l * config.branchLF, aNext = a * config.branchAF;
    Vec4 root = new Vec4(vertices.get(rootId).world());
    Vec4 grow = new Vec4(0, l, 0);

    Mat4 transform = new Mat4();
    transform.loadRotate(new Vec4(a, zGap * n, 0));
    grow.multiply(transform);

    if (config.centralBranch)
      addBranch(rootId, nextLevel, 0, new Vec4(root).add(grow), lNext, aNext);


    for (int i = 0; i < config.branchCount; i++) {
      /*transform.rotate(rotate);

      addBranch(rootId, nextLevel, i, new Vec4(root).add(newBase),
        new Vec4(newBase).multiply(lf), newAngle);*/
    }
  }

  private void addBranch(int level, int rootId, int n,
                         Vec4 p, double l, double a)
    throws InterruptedException {
    int newRootId = vertices.size();
    edges.add(new Edge(rootId, newRootId));
    vertices.add(new Vertex(p));
    if (isAnimating()) onFrame();
    branch(newRootId, level, n, l, a);
  }
}
