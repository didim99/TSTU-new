package ru.didim99.tstu.core.graphics;

import java.util.ArrayList;
import ru.didim99.tstu.core.graphics.model.Mat4;
import ru.didim99.tstu.core.graphics.model.Model;
import ru.didim99.tstu.core.graphics.model.Vec4;
import ru.didim99.tstu.core.graphics.model.Vertex;
import ru.didim99.tstu.ui.view.DrawerView;

/**
 * Created by didim99 on 13.12.19.
 */
public class FractalRenderer extends ModelRenderer {
  private static final int FPS = 10;
  // Scene configuration
  public static final int SCL_MIN = 20;
  public static final int SCL_MAX = 300;
  public static final int ROT_MIN = -180;
  public static final int ROT_MAX = 180;
  public static final double SCL_FACTOR = 0.05;
  private static final double DEFAULT_SCL = 4.0;
  private static final boolean DEFAULT_DRAW_AXIS = false;
  // Fractal algorithm configuration
  public static final int LEVEL_MIN = 2;
  public static final int LEVEL_MAX = 10;
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
  private static final int DEFAULT_LEVEL = 5;
  private static final int DEFAULT_BCOUNT = 2;
  private static final int DEFAULT_ANGLE = 120;
  private static final double DEFAULT_L = 1.0;
  private static final double DEFAULT_LF = 0.5;
  private static final double DEFAULT_AF = 1.0;
  private static final boolean DEFAULT_CB = true;
  // internal fractal algorithm settings
  private static final Vec4 ROOT = new Vec4(0, 0, 0);

  private Model model;
  private ArrayList<Vertex> vertices;
  private double zGap;

  public FractalRenderer(DrawerView target, Config config) {
    super(Model.Type.EDGE, target, config);

    if (config == null) {
      this.config.models.add(new Model());
      this.config.drawAxis = DEFAULT_DRAW_AXIS;
      resetBuilder();
    }

    model = this.config.models.get(0);
    vertices = model.getVertices();
    onBuilderChanged();
    onSceneChanged();
  }

  public Model getModel() {
    return model;
  }

  public boolean hasModel() {
    return !model.isEmpty();
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
      setFPS(FPS * config.maxLevel * config.branchCount / 4);
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
  public void clear() {
    if (model != null) {
      model.clearData();
      onSceneChanged();
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

  /**
   * Build the fractal tree
   */
  private void generateFractal() throws InterruptedException {
    if (model == null) return;
    // Clear all previous data
    model.clearData();
    // Add initial branch
    model.addVertex(ROOT);
    model.addVertex(new Vec4(ROOT).sub(
      new Vec4(0, config.branchL * 2, 0)));
    model.addEdge(0, 1);
    // Build all other branches
    branch(0, 0, config.branchL, config.branchAngle, new Mat4());
  }

  /**
   * Build one level of branches in the fractal tree
   *
   * @param level   current recursion level
   * @param rootId  root vertex number
   * @param l       branch length
   * @param a       branch angle
   * @param basis   n -> 1 basis transition matrix
   */
  private void branch(int level, int rootId, double l, double a, Mat4 basis)
    throws InterruptedException {
    if (level >= config.maxLevel) return;
    int nextLevel = level + 1;

    // Compute branch length and angle for next level
    double lNext = l * config.branchLF, aNext = a * config.branchAF;
    // Get root vertex position
    Vec4 root = new Vec4(vertices.get(rootId).world());
    // Initialize grow vector
    Vec4 grow = new Vec4(0, l, 0);

    // Build central branch if necessary
    if (config.centralBranch)
      addBranch(nextLevel, rootId, lNext, aNext, basis, root, grow);

    // Compute n+1 -> n basis transition matrix
    Mat4 migrate = new Mat4();
    migrate.loadRotate(new Vec4(0, 0, a));
    // Compute n+1 -> 1 basis transition matrix
    Mat4 newBasis = new Mat4(migrate);
    newBasis.multiply(basis);

    // Build all side branches
    for (int i = 0; i < config.branchCount; i++) {
      addBranch(nextLevel, rootId, lNext, aNext, newBasis, root, grow);
      migrate.rotate(new Vec4(0, zGap, 0));
      newBasis.load(migrate);
      newBasis.multiply(basis);
    }
  }

  /**
   * Add single branch to the fractal tree
   *
   * @param level   current recursion level
   * @param rootId  root vertex number
   * @param l       branch length
   * @param a       branch angle
   * @param basis   n -> 1 basis transition matrix
   * @param root    root vertex position
   * @param grow    grow vector
   */
  private void addBranch(int level, int rootId, double l, double a,
                         Mat4 basis, Vec4 root, Vec4 grow)
    throws InterruptedException {
    // Compute new root vertex position in the Model space
    grow = new Vec4(root).add(grow.multiply(basis));
    // Get new root vertex number
    int newRootId = vertices.size();
    // Add branch into model
    model.addVertex(grow);
    model.addEdge(rootId, newRootId);
    // Draw current frame if animation enabled
    if (isAnimating()) onFrame();
    // Build next level of branches from new root vertex
    branch(level, newRootId, l, a, basis);
  }
}
