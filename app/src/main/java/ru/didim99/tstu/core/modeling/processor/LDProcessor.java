package ru.didim99.tstu.core.modeling.processor;

import ru.didim99.tstu.core.modeling.Functions;
import ru.didim99.tstu.core.optimization.math.PointRN;

/**
 * Lumped system dynamic mode processor
 *
 * Created by didim99 on 24.02.20.
 */
public class LDProcessor extends VariableProcessor {
  private static final double T_START = 0; // sec
  private static final double T_END = 500; // sec
  private static final double T_STEP = 0.1; // sec

  public LDProcessor(int variableId) {
    super(variableId);
  }

  @Override
  public void process() {
    LPoint prev = new LPoint(T_START);
    for (int i = 0; i < Functions.lumpedVars.length; i++)
      prev.set(i, Functions.lumpedVars[i].getDefaultValue());
    prev.cOut = Functions.cOutStatic.f(prev);
    prev.mOut = Functions.mOutStatic.f(prev);
    prev.M = Functions.MStatic.f(prev.mOut);

    LPoint next = new LPoint(prev);
    next.set(variableId, next.get(
      variableId) + var.getDelta());

    while (next.t <= T_END) {
      next.mOut = Functions.mOutDynamic.f(prev.M);
      prev.dM = Functions.dM.f(prev);
      next.M = prev.M + prev.dM * T_STEP;
      prev.mOut = next.mOut;
      next.cOut = prev.cOut + Functions.dCxM.f(prev)
        * T_STEP / prev.M;

      series.add(new LPoint(prev));
      prev.set(next);
      next.t += T_STEP;
    }
  }

  public static class LPoint implements PointRN {
    public static final int C_IN = 0;
    public static final int M_IN = 1;
    public static final int T_IN = 2;
    public static final int C_OUT = 3;
    public static final int M_OUT = 4;
    public static final int DM = 5;

    private double t, cIn, mIn, tIn;
    private double cOut, mOut, M, dM;

    private LPoint(double t) {
      this.t = t;
    }

    private LPoint(LPoint src) {
      set(src);
    }

    @Override
    public void set(int pos, double val) {
      switch (pos) {
        case C_IN: this.cIn = val; break;
        case M_IN: this.mIn = val; break;
        case T_IN: this.tIn = val; break;
      }
    }

    @Override
    public double get(int pos) {
      switch (pos) {
        case C_IN: return cIn;
        case M_IN: return mIn;
        case T_IN: return tIn;
        case C_OUT: return cOut;
        case M_OUT: return mOut;
        case DM: return dM;
        default: return 0;
      }
    }

    @Override
    public double getX() {
      return t;
    }

    @Override
    public double getY() {
      return cOut;
    }

    private void set(LPoint src) {
      this.t = src.t;
      this.cIn = src.cIn;
      this.mIn = src.mIn;
      this.tIn = src.tIn;
      this.cOut = src.cOut;
      this.mOut = src.mOut;
      this.M = src.M;
      this.dM = src.dM;
    }
  }
}
