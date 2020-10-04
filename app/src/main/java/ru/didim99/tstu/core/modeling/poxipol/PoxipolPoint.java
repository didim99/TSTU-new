package ru.didim99.tstu.core.modeling.poxipol;

import ru.didim99.tstu.core.optimization.math.PointD;

/**
 * Created by didim99 on 04.10.20.
 */

public class PoxipolPoint extends PointD {

  public enum Pos { TAU, T, K1, K2, K3, K4, C1, C2, C3, C4, C5, C6 }

  public PoxipolPoint() { super(Pos.values().length); }

  public double t() { return get(Pos.TAU.ordinal()); }
  public double T() { return get(Pos.T.ordinal()); }
  public double k1() { return get(Pos.K1.ordinal()); }
  public double k2() { return get(Pos.K2.ordinal()); }
  public double k3() { return get(Pos.K3.ordinal()); }
  public double k4() { return get(Pos.K4.ordinal()); }
  public double c1() { return get(Pos.C1.ordinal()); }
  public double c2() { return get(Pos.C2.ordinal()); }
  public double c3() { return get(Pos.C3.ordinal()); }
  public double c4() { return get(Pos.C4.ordinal()); }
  public double c5() { return get(Pos.C5.ordinal()); }
  public double c6() { return get(Pos.C6.ordinal()); }

  @Override
  public double getY() { return c3(); }
}
