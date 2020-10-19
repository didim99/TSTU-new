package ru.didim99.tstu.core.math.modeling.randproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import ru.didim99.tstu.core.math.common.Function;
import ru.didim99.tstu.core.math.common.FunctionRN;
import ru.didim99.tstu.core.math.common.FunctionTabulator;
import ru.didim99.tstu.core.math.common.PointD;
import ru.didim99.tstu.core.math.common.PointRN;
import ru.didim99.tstu.core.math.modeling.Functions;
import ru.didim99.tstu.core.math.modeling.processor.MultiSeriesProcessor;
import ru.didim99.tstu.core.math.optimization.multidim.DownhillMethod;
import ru.didim99.tstu.core.math.optimization.multidim.ExtremaFinderRN;
import ru.didim99.tstu.utils.CyclicBuffer;
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
  private static final PointD REF_WEIGHT = new PointD(1.0, 1.0, 5.0);
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

  private final boolean useOptimization;
  private final Random random;
  private PointD reference;
  private RandomProcess process;
  private PointRN result;
  private Double[] processData;
  private ArrayList<PointRN> cTable;

  public RandomProcessor(boolean useOptimization) {
    this.useOptimization = useOptimization;
    random = new Random(RNG_A, RNG_M, RNG_SEED);
    result = new PointD(REFERENCE.size() + 2);
    seriesNames.addAll(Arrays.asList(SERIES_NAMES));
  }

  public RandomProcess getProcess() {
    return process;
  }

  @Override
  public void process() {
    PointD params = configure(REFERENCE);
    computeDelta(params);
    buildSeriesFamily();
  }

  public PointD configure(PointD reference) {
    this.reference = reference;
    CyclicBuffer<Double> buffer = new CyclicBuffer<>(BUFF_SIZE, 0.0);
    buffer.fill(random::nextDoubleCentered);
    double sx = DiscreteMath.spread(buffer.getAll());
    process = new RandomProcess(random, INTERVAL);
    process.init(sx, reference.get(IM),
      reference.get(IS), reference.get(IA));

    PointD params = new PointD(A1, A2, 0);
    if (useOptimization) {
      ExtremaFinderRN finder = new DownhillMethod();
      params = finder.find(this, params);
    }

    process.setup(params.get(OA1), params.get(OA2));
    return params;
  }

  public void computeDelta(PointRN params) {
    process.setup(params.get(OA1), params.get(OA2));
    CyclicBuffer<Double> buffer = new CyclicBuffer<>(Z_SIZE, 0.0);
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
    result.set(IA1, params.get(OA1));
    result.set(IA2, params.get(OA2));
  }

  private void buildSeriesFamily() {
    Function k = s -> Functions.approx.f(
      new PointD(result.get(IA), s, result.get(IS)));
    seriesFamily.add(buildSeriesInteger(processData, 1));
    FunctionTabulator tabulator = new FunctionTabulator(k);
    seriesFamily.add(tabulator.tabulate(cTable));
    seriesFamily.add(cTable);
  }

  @Override
  public double f(PointRN p) {
    computeDelta(p);
    double res = 0;
    for (int i = 0; i < reference.size(); i++)
      res += Math.pow((reference.get(i) - result.get(i))
        * REF_WEIGHT.get(i), 2);
    return res;
  }

  @Override
  public String getDescription() {
    return getParamsDescription() +
      "\nRandom process range:\n" +
      super.getDescription();
  }

  public String getParamsDescription() {
    return String.format(Locale.US, "Mz = %.4f (err: %.1f%%)\n",
      result.get(IM), Utils.calcError(result.get(IM), reference.get(IM))) +
      String.format(Locale.US, "Sz = %.4f (err: %.1f%%)\n",
        result.get(IS), Utils.calcError(result.get(IS), reference.get(IS))) +
      String.format(Locale.US, "ɑz = %.4f (err: %.1f%%)\n",
        result.get(IA), Utils.calcError(result.get(IA), reference.get(IA))) +
      String.format(Locale.US, "A1 = %.4f\n", result.get(IA1)) +
      String.format(Locale.US, "A2 = %.4f\n", result.get(IA2));
  }

  private static List<PointRN> buildSeriesInteger(Double[] src, int from) {
    List<PointRN> series = new ArrayList<>(src.length);
    for (double v : src)
      series.add(new PointD((double) from++, v));
    return series;
  }

  public static String[] getSeriesList() {
    return new String[] {SERIES_NAMES[0],
      SERIES_NAMES[1] + " / " + SERIES_NAMES[2]};
  }
}
