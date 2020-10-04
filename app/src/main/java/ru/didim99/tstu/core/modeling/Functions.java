package ru.didim99.tstu.core.modeling;

import ru.didim99.tstu.core.modeling.processor.LDProcessor.LPoint;
import ru.didim99.tstu.core.modeling.processor.DSProcessor.DPoint;
import ru.didim99.tstu.core.optimization.math.Function;
import ru.didim99.tstu.core.optimization.math.FunctionRN;

/**
 * Created by didim99 on 24.02.20.
 */
public class Functions {
  // Static constants (Lumped)
  private static final double kT    = 5000;   // W/m^2
  private static final double tR    = 90;     // degC
  private static final double cT    = 4187;   // J/kg
  private static final double r     = 2.26E6; // J/kg*degC
  private static final double sigma = 0.12;   // kg^0.5*m/s
  private static final double F     = 10;     // m^2
  private static final double S     = 0.75;   // m^2
  private static final double P0    = 7900;   // kg/m^2
  private static final double P1    = 7600;   // kg/m^2
  // Static constants (Distributed)
  private static final double R     = 8.31;   // J/Mol*degC
  private static final double E1    = 251E3;  // J/Mol
  private static final double E2    = 297E3;  // J/Mol
  private static final double A1    = 2E11;   // no unit
  private static final double A2    = 8E12;   // no unit
  private static final double RO    = 1.4;    // kg/m^3
  private static final double D     = 0.1;    // m
  // Pre-calculated constants
  private static final double ROxS  = RO * Math.PI * D * D / 4;

  // Input variables (Lumped)
  public static final Variable[] lumpedVars = {
    new Variable("cIn", 0.08, 0.06, 0.10, 0.004, 0.03),
    new Variable("mIn", 4.0, 4.0, 6.0, 0.2, 3.0),
    new Variable("tIn", 130.0, 122.0, 140.0, 0.5, 13.0)
  };

  // Input variables (Distributed)
  public static final Variable[] distVars = {
    new Variable("L", 0, 0, 40, 0.05),
    new Variable("m", 0, 0.2, 0.5, 0.15),
    new Variable("cIn", 0.2, 0.2, 0.2, 0.05),
    new Variable("T", 1360, 1360, 1360, 25)
  };

  // Values definition: cIn, mIn, tIn
  public static final FunctionRN cOutStatic = in ->
    in.get(1) * in.get(0) / (in.get(1) + kT * F * (in.get(2) - tR) / (cT * tR - r));

  // Values definition: cIn, mIn, cOut
  public static final FunctionRN mOutStatic = in ->
    in.get(LPoint.M_IN) * in.get(LPoint.C_IN) / in.get(LPoint.C_OUT);

  public static final Function MStatic = mOut ->
    S * (Math.pow(mOut / sigma, 2) + P1 - P0);

  public static final Function mOutDynamic = m ->
    sigma * Math.sqrt(P0 + m / S - P1);

  // Values definition: mIn, mOut, tIn
  public static final FunctionRN dM = in ->
    kT * F * (in.get(LPoint.T_IN) - tR) / (cT * tR - r)
      + in.get(LPoint.M_IN) - in.get(LPoint.M_OUT);

  // Values definition: cIn, mIn, cOut, mOut, dM, M
  public static final FunctionRN dCxM = in ->
    in.get(LPoint.C_IN) * in.get(LPoint.M_IN) - in.get(LPoint.C_OUT)
      * (in.get(LPoint.M_OUT) + in.get(LPoint.DM));

  // Values definition: k1, cIn1, m
  public static final FunctionRN dC1 = in ->
    -in.get(DPoint.K1) * in.get(DPoint.C_OUT1) * ROxS / in.get(DPoint.M);

  // Values definition: k1, cIn1, k2, cIn2, m
  public static final FunctionRN dC2 = in ->
    (in.get(DPoint.K1) * in.get(DPoint.C_OUT1) - in.get(DPoint.K2)
      * in.get(DPoint.C_OUT2)) * ROxS / in.get(DPoint.M);

  public static final Function rSpeed1 = t -> k(A1, E1, t);
  public static final Function rSpeed2 = t -> k(A2, E2, t);

  public static double k(double a, double e, double t) {
    return a * Math.exp(-e / (R * t));
  }

  // Values definition: alpha, s, sigma
  public static final FunctionRN approx = x ->
    x.get(2) * Math.exp(-x.get(0) * Math.abs(x.get(1)));

  public static String[] getVarList() {
    String[] list = new String[lumpedVars.length];
    for (int i = 0; i < list.length; i++)
      list[i] = lumpedVars[i].getName();
    return list;
  }
}
