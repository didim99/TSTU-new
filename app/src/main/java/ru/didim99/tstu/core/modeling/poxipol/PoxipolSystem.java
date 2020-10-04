package ru.didim99.tstu.core.modeling.poxipol;

import java.util.Arrays;
import ru.didim99.tstu.core.modeling.Functions;
import ru.didim99.tstu.core.modeling.poxipol.PoxipolPoint.Pos;

/**
 * Created by didim99 on 04.10.20.
 */

class PoxipolSystem extends DiffSystem<PoxipolPoint> {

  // Chemical constants
  private static final double A1    = 2553;
  private static final double A2    = 2.1E9;
  private static final double A3    = 8.4E9;
  private static final double A4    = 9.45E4;
  private static final double E1    = 34000;       // J/Mol
  private static final double E2    = 7.7E4;       // J/Mol
  private static final double E3    = 5.2E4;       // J/Mol
  private static final double E4    = 3.75E4;      // J/Mol
  private static final double Q1    = 62540;       // kJ/Mol
  private static final double Q2    = 1E6;         // kJ/Mol
  private static final double Q4    = 1.08E6;      // kJ/Mol
  // Physical constants
  private static final double DTAU  = 0.001;      // sec
  private static final double V     = 3;          // m^3
  private static final double KT    = 700;        // W/m^2*deg
  private static final double RHO   = 1180;       // kg/m^3
  private static final double CT    = 1200;       // J/kg*deg
  private static final double TT    = 8;          // degC
  // Input & output constants
  private static final double IN_T  = 40;       // degC
  private static final double IN_C1 = 0.55;     // mol/m^3
  private static final double IN_C2 = 0.35;     // mol/m^3
  private static final double IN_C5 = 0.10;     // mol/m^3
  private static final double TARGET_C3 = 0.3;  // mol/m^3
  // Limitations
  private static final double F_MIN = 6.5;      // m^3
  private static final double F_MAX = 10.5;     // m^3
  private static final double T_MAX = 60;       // degC


  private double f = (F_MIN + F_MAX) / 2;

  public PoxipolSystem() {
    system.addAll(Arrays.asList(
      new Entry<>(Pos.K1.ordinal(), false, p -> Functions.k(A1, E1, p.T())),
      new Entry<>(Pos.K2.ordinal(), false, p -> Functions.k(A2, E2, p.T())),
      new Entry<>(Pos.K3.ordinal(), false, p -> Functions.k(A3, E3, p.T())),
      new Entry<>(Pos.K4.ordinal(), false, p -> Functions.k(A4, E4, p.T())),
      new Entry<>(Pos.C1.ordinal(), true,
        p -> -(p.k1() + p.k2()) * p.c1() * p.c2()),
      new Entry<>(Pos.C2.ordinal(), true,
        p -> -(p.k1() + p.k2()) * p.c1() * p.c2() + p.k4() * p.c3() * p.c6()),
      new Entry<>(Pos.C3.ordinal(), true,
        p -> -p.k4() * p.c3() * p.c6() + p.k1() * p.c1() * p.c2()),
      new Entry<>(Pos.C4.ordinal(), true,
        p -> -p.k3() * p.c4() * p.c5() + p.k2() * p.c1() * p.c2()),
      new Entry<>(Pos.C5.ordinal(), true,
        p -> -p.k3() * p.c4() * p.c5() + p.k4() * p.c3() * p.c6()),
      new Entry<>(Pos.C6.ordinal(), true,
        p -> -p.k4() * p.c3() * p.c6() + p.k3() * p.c4() * p.c5()),
      new Entry<>(Pos.T.ordinal(), true,
        p -> (Q1 * p.k1() * p.c1() * p.c2() + Q2 * p.k2() * p.c1() * p.c2()
        + Q4 * p.k4() * p.c3() * p.c6() - KT * f * (p.T() - TT)) / (CT * V * RHO * p.T()))
    ));
  }

  @Override
  protected PoxipolPoint getStartPoint() {
    PoxipolPoint point = new PoxipolPoint();
    point.set(Pos.T.ordinal(), IN_T);
    point.set(Pos.C1.ordinal(), IN_C1);
    point.set(Pos.C2.ordinal(), IN_C2);
    point.set(Pos.C5.ordinal(), IN_C5);
    return point;
  }

  @Override
  protected boolean completed() {
    return point.c3() >= TARGET_C3;
  }

  @Override
  protected double getStep() {
    return DTAU;
  }
}
