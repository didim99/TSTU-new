package ru.didim99.tstu.core.modeling.randproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import ru.didim99.tstu.core.modeling.Functions;
import ru.didim99.tstu.core.modeling.processor.MultiSeriesProcessor;
import ru.didim99.tstu.core.optimization.math.Function;
import ru.didim99.tstu.core.optimization.math.FunctionRN;
import ru.didim99.tstu.core.optimization.math.PointD;
import ru.didim99.tstu.core.optimization.math.PointRN;
import ru.didim99.tstu.core.optimization.multidim.DownhillMethod;
import ru.didim99.tstu.core.optimization.multidim.ExtremaFinderRN;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 08.05.20.
 */
public class RandomProcessor extends MultiSeriesProcessor implements FunctionRN {
  private static final String[] SERIES_NAMES = {"Z(t)", "EXP(s)", "K(s)"};
  // Buffer parameters
  private static final int BUFF_SIZE = 200;
  private static final int INTERVAL = 10;
  private static final int K_SIZE = 20;
  private static final int Z_SIZE = BUFF_SIZE - INTERVAL + 1;
  // RNG parameters
  private static final long RNG_A = 48828125; // 5^11
  private static final long RNG_M = 1L << 18; // 2^18
  private static final long RNG_SEED = 1;
  // Process target parameters: M0, Sigma0, Alpha0
  private static final PointD REFERENCE = new PointD(8.0, 4.5, 0.17);
  // Process default parameters
  private static final double A1 = 1.0;
  private static final double A2 = 1.0;
  // Parameter indexes
  private static final int IM =   0;
  private static final int IS =   1;
  private static final int IA =   2;
  private static final int IA1 =  3;
  private static final int IA2 =  4;
  // Parameter indexes (optimization)
  private static final int OA1 =  0;
  private static final int OA2 =  1;

  private boolean useOptimization;
  private Random random;
  private RandomProcess process;
  private PointRN result;
  private double[] processData;
  private ArrayList<PointRN> cTable;

  public RandomProcessor(boolean useOptimization) {
    this.useOptimization = useOptimization;
    random = new Random(RNG_A, RNG_M, RNG_SEED);
    result = new PointD(REFERENCE.size() + 2);
    seriesNames.addAll(Arrays.asList(SERIES_NAMES));
  }

  @Override
  public void process() {
    CyclicBuffer buffer = new CyclicBuffer(BUFF_SIZE);
    buffer.fill(random::nextDoubleCentered);
    double sx = DiscreteMath.spread(buffer.getAll());
    process = new RandomProcess(random, INTERVAL);
    process.init(sx, REFERENCE.get(IM),
      REFERENCE.get(IS), REFERENCE.get(IA));
    PointD params = new PointD(A1, A2, 0);

    if (useOptimization) {
      ExtremaFinderRN finder = new DownhillMethod();
      params = finder.find(this, params);
    } else compute(params);

    result.set(IA1, params.get(OA1));
    result.set(IA2, params.get(OA2));
    Function k = s -> Functions.approx.f(
      new PointD(result.get(IA), s, result.get(IS)));
    seriesFamily.add(buildSeriesInteger(processData, 1));
    seriesFamily.add(tabulateFunction(k, cTable));
    seriesFamily.add(cTable);
  }

  public void compute(PointRN params) {
    process.setup(params.get(OA1), params.get(OA2));
    CyclicBuffer buffer = new CyclicBuffer(Z_SIZE);
    buffer.fill(process::next);
    processData = buffer.getAll();
    result.set(IM, DiscreteMath.mean(processData));
    result.set(IS, DiscreteMath.spread(processData));

    cTable = new ArrayList<>();
    for (int s = 0; s < K_SIZE; s++)
      cTable.add(new PointD((double) s, DiscreteMath.correlate(s, processData)));
    PointD start = new PointD(0, 0, result.get(IS));
    Approximator approximator = new Approximator(cTable);
    start = approximator.approximate(Functions.approx, start, 1);
    result.set(IA, start.get(0));
  }

  @Override
  public double f(PointRN p) {
    compute(p);
    double res = 0;
    for (int i = 0; i < REFERENCE.size(); i++)
      res += Math.pow(REFERENCE.get(i) - result.get(i), 2);
    return res;
  }

  @Override
  public String getDescription() {
    return
      String.format(Locale.US, "Mz = %.4f (err: %.1f%%)\n",
        result.get(IM), Utils.calcError(result.get(IM), REFERENCE.get(IM))) +
      String.format(Locale.US, "Sz = %.4f (err: %.1f%%)\n",
        result.get(IS), Utils.calcError(result.get(IS), REFERENCE.get(IS))) +
      String.format(Locale.US, "ɑz = %.4f (err: %.1f%%)\n",
        result.get(IA), Utils.calcError(result.get(IA), REFERENCE.get(IA))) +
      String.format(Locale.US, "A1 = %.4f\n", result.get(IA1)) +
      String.format(Locale.US, "A2 = %.4f\n", result.get(IA2)) +
      "\nRandom process range:\n" +
      super.getDescription();
  }

  private static ArrayList<PointRN> buildSeriesInteger(double[] src, int from) {
    ArrayList<PointRN> series = new ArrayList<>(src.length);
    for (double v : src)
      series.add(new PointD((double) from++, v));
    return series;
  }

  private static ArrayList<PointRN> tabulateFunction(Function fun, ArrayList<PointRN> ref) {
    ArrayList<PointRN> series = new ArrayList<>(ref.size());
    for (PointRN point : ref)
      series.add(new PointD(point.get(0), fun.f(point.get(0))));
    return series;
  }

  public static String[] getSeriesList() {
    return new String[] {SERIES_NAMES[0],
      SERIES_NAMES[1] + " / " + SERIES_NAMES[2]};
  }
}
