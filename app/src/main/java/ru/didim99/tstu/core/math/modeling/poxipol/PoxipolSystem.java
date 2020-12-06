package ru.didim99.tstu.core.math.modeling.poxipol;

import java.util.Arrays;
import ru.didim99.tstu.core.math.common.Function;
import ru.didim99.tstu.core.math.common.FunctionRN;
import ru.didim99.tstu.core.math.common.PointD;
import ru.didim99.tstu.core.math.common.PointRN;
import ru.didim99.tstu.core.math.modeling.Functions;
import ru.didim99.tstu.utils.Supplier;

import static ru.didim99.tstu.core.math.modeling.Functions.c2k;
import static ru.didim99.tstu.core.math.modeling.Functions.k2c;
import static ru.didim99.tstu.core.math.modeling.poxipol.PoxipolPointMapper.*;

/**
 * Created by didim99 on 04.10.20.
 */

public class PoxipolSystem extends DiffSystem<PoxipolSystem.Point>
  implements Function, FunctionRN {
  // Chemical constants
  private static final double A1    = 2553;
  private static final double A2    = 2.1E9;
  private static final double A3    = 8.4E9;
  private static final double A4    = 9.45E4;
  private static final double E1    = 34000;    // J/Mol
  private static final double E2    = 7.7E4;    // J/Mol
  private static final double E3    = 5.2E4;    // J/Mol
  private static final double E4    = 3.75E4;   // J/Mol
  private static final double Q1    = 6254E4;   // J/Mol
  private static final double Q2    = 1E9;      // J/Mol
  private static final double Q4    = 1.08E9;   // J/Mol
  // Physical constants
  private static final double DTAU  = 0.05;     // sec
  private static final double V     = 3;        // m^3
  private static final double KT    = 700;      // W/m^2*deg
  private static final double RHO   = 1180;     // kg/m^3
  private static final double CT    = 1200;     // J/kg*deg
  private static final double TT0   = c2k(8);   // degC
  // Input & output constants
  private static final double IN_T  = c2k(40);  // degK
  private static final double IN_C1 = 0.55;     // mol/m^3
  private static final double IN_C2 = 0.35;     // mol/m^3
  private static final double IN_C5 = 0.10;     // mol/m^3
  private static final double TARGET_C3 = 0.3;  // mol/m^3
  // Limitations
  private static final double T_MAX = 60;       // degC
  public static final double F_MIN = 6.5;       // m^3
  public static final double F_MAX = 10.5;      // m^3
  // Modeling parameters
  public static final PointD RANDOM_PARAMS = new PointD(8, 4, 0.085);
  private static final int TT_INTERVAL = 10;

  private double f = (F_MIN + F_MAX) / 2;

  private Supplier<Double> ttSource;
  private double ttBuffer;
  private int stepCounter;

  public PoxipolSystem() {
    system.addAll(Arrays.asList(
      new Entry<>(K1, false, p -> Functions.k(A1, E1, p.get(T))),
      new Entry<>(K2, false, p -> Functions.k(A2, E2, p.get(T))),
      new Entry<>(K3, false, p -> Functions.k(A3, E3, p.get(T))),
      new Entry<>(K4, false, p -> Functions.k(A4, E4, p.get(T))),
      new Entry<>(C1, true,
        p -> -(p.get(K1) + p.get(K2)) * p.get(C1) * p.get(C2)),
      new Entry<>(C2, true,
        p -> -(p.get(K1) + p.get(K2)) * p.get(C1) * p.get(C2)
          + p.get(K4) * p.get(C3) * p.get(C6)),
      new Entry<>(C3, true,
        p -> -p.get(K4) * p.get(C3) * p.get(C6)
          + p.get(K1) * p.get(C1) * p.get(C2)),
      new Entry<>(C4, true,
        p -> -p.get(K3) * p.get(C4) * p.get(C5)
          + p.get(K2) * p.get(C1) * p.get(C2)),
      new Entry<>(C5, true,
        p -> -p.get(K3) * p.get(C4) * p.get(C5)
          + p.get(K4) * p.get(C3) * p.get(C6)),
      new Entry<>(C6, true,
        p -> -p.get(K4) * p.get(C3) * p.get(C6)
          + p.get(K3) * p.get(C4) * p.get(C5)),
      new Entry<>(T, true,
        p -> (Q1 * p.get(K1) * p.get(C1) * p.get(C2)
          + Q2 * p.get(K2) * p.get(C1) * p.get(C2)
          + Q4 * p.get(K4) * p.get(C3) * p.get(C6)
          - KT * f * (p.get(T) - tt(p))) / (CT * V * RHO))
    ));
  }

  @Override
  public void integrate(StepListener listener) {
    if (this.ttSource != null) {
      this.ttBuffer = ttSource.nextElement();
      this.stepCounter = 0;
    }

    super.integrate(listener);
  }

  private double tt(Point p) {
    if (ttSource != null) {
      if (++stepCounter == TT_INTERVAL) {
        ttBuffer = ttSource.nextElement();
        stepCounter = 0;
      }

      double tt = c2k(ttBuffer);
      p.set(TT, tt);
      return tt;
    } else return TT0;
  }

  public PointD getMinBound() {
    return new PointD(F_MIN);
  }

  public PointD getMaxBound() {
    return new PointD(F_MAX);
  }

  @Override
  protected Point getStartPoint() {
    Point point = new Point();
    point.set(T, IN_T);
    point.set(C1, IN_C1);
    point.set(C2, IN_C2);
    point.set(C5, IN_C5);
    return point;
  }

  @Override
  protected boolean completed() {
    return point.get(C3) > TARGET_C3;
  }

  @Override
  protected double getStep() {
    return DTAU;
  }

  @Override
  public double f(double x) {
    this.f = x;
    integrate(null);
    return point.get(TAU);
  }

  @Override
  public double f(PointRN p) {
    return f(p.get(0));
  }

  public void setTTSource(Supplier<Double> source) {
    this.ttSource = source;
  }

  public void setF(double f) {
    this.f = f;
  }

  public static class Point extends PointD {
    private static PoxipolPointMapper yMapper = C3;

    public Point() {
      super(PoxipolPointMapper.values().length);
    }

    public Point(Point point) {
      super(point);
    }

    @Override
    public Point copy() {
      return new Point(this);
    }

    @Override
    public double getX() {
      return get(TAU);
    }

    @Override
    public double getY() {
      if (yMapper == T || yMapper == TT)
        return k2c(get(yMapper));
      else return get(yMapper);
    }

    public static void setYMapper(int index) {
      yMapper = PoxipolPointMapper.getByOrdinal(index);
    }
  }
}
