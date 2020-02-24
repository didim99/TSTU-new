package ru.didim99.tstu.core.modeling;

import ru.didim99.tstu.core.optimization.math.FunctionRN;

/**
 * Created by didim99 on 24.02.20.
 */
public class Functions {
  // Static constants
  private static final double kT    = 5000;   // W/m^2
  private static final double tR    = 90;     // degC
  private static final double cT    = 4187;   // J/kg
  private static final double r     = 2.26E6; // J/kg*degC
  private static final double sigma = 0.12;   // kg^0.5*m/s
  private static final double F     = 10;     // m^2
  private static final double S     = 0.75;   // m^2
  private static final double P0    = 7900;   // kg/m^2
  private static final double P1    = 7600;   // kg/m^2

  // Input variables
  public static Variable[] vars = new Variable[] {
    new Variable("cIn", 0.08, 0.06, 0.10, 0.004, 0.03),
    new Variable("mIn", 4.0, 4.0, 6.0, 0.2, 3.0),
    new Variable("tIn", 130.0, 122.0, 140.0, 0.5, 13.0)
  };

  // Values definition: cIn, mIn, tIn
  public static FunctionRN cOutStatic = in ->
    in.get(1) * in.get(0) / (in.get(1) + kT * F * (in.get(2) - tR) / (cT * tR - r));

  // Values definition: mIn, mSec
  public static FunctionRN MStatic = in ->
    S * (Math.pow((in.get(0) - in.get(1)) / sigma, 2) + P1 - P0);

  public static String[] getVarList() {
    String[] list = new String[vars.length];
    for (int i = 0; i < list.length; i++)
      list[i] = vars[i].getName();
    return list;
  }
}
