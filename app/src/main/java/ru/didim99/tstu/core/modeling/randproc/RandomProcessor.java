package ru.didim99.tstu.core.modeling.randproc;

import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.core.modeling.Functions;
import ru.didim99.tstu.core.modeling.processor.MultiSeriesProcessor;
import ru.didim99.tstu.core.optimization.math.PointD;
import ru.didim99.tstu.core.optimization.math.PointRN;

/**
 * Created by didim99 on 08.05.20.
 */
public class RandomProcessor extends MultiSeriesProcessor {
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
  private static final int IM = 0;
  private static final int IS = 1;
  private static final int IA = 2;

  private Random random;
  private PointRN result;

  public RandomProcessor() {
    random = new Random(RNG_A, RNG_M, RNG_SEED);
    result = new PointD(REFERENCE.size());
  }

  @Override
  public void process() {
    CyclicBuffer buffer = new CyclicBuffer(BUFF_SIZE);
    buffer.fill(random::nextDoubleCentered);
    double sx = DiscreteMath.spread(buffer.getAll());
    RandomProcess process = new RandomProcess(random, INTERVAL);
    process.init(sx, REFERENCE.get(IM),
      REFERENCE.get(IS), REFERENCE.get(IA));

    process.setup(A1, A2);
    buffer = new CyclicBuffer(Z_SIZE);
    buffer.fill(process::next);
    double[] data = buffer.getAll();
    result.set(IM, DiscreteMath.mean(data));
    result.set(IS, DiscreteMath.spread(data));

    ArrayList<PointRN> cTable = new ArrayList<>();
    for (int s = 0; s < K_SIZE; s++)
      cTable.add(new PointD((double) s, DiscreteMath.correlate(s, data)));
    PointD start = new PointD(0, 0, result.get(IS));
    Approximator approximator = new Approximator(cTable);
    start = approximator.approximate(Functions.approx, start, 1);
    result.set(IA, start.get(0));

    seriesFamily.add(buildSeriesInteger(data, 1));
    seriesFamily.add(cTable);
  }

  @Override
  public String getDescription() {
    return
      String.format(Locale.US, "Mz = %.4f\n", result.get(IM)) +
      String.format(Locale.US, "Sz = %.4f\n", result.get(IS)) +
      String.format(Locale.US, "ɑz = %.4f\n", result.get(IA)) +
      "\nRandom process range:\n" +
      super.getDescription();
  }

  private ArrayList<PointRN> buildSeriesInteger(double[] src, int from) {
    ArrayList<PointRN> series = new ArrayList<>(src.length);
    for (double v : src)
      series.add(new PointD((double) from++, v));
    return series;
  }
}
