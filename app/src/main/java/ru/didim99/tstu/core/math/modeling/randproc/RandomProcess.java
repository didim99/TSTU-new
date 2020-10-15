package ru.didim99.tstu.core.math.modeling.randproc;

import ru.didim99.tstu.utils.CyclicBuffer;

/**
 * Created by didim99 on 08.05.20.
 */
public class RandomProcess {
  private final Random random;
  private final CyclicBuffer<Double> buffer;
  private final int interval;

  private double m0, s0, a0, sigma;
  private double f1, f2;

  public RandomProcess(Random random, int interval) {
    this.buffer = new CyclicBuffer<>(interval, 0.0);
    this.buffer.fill(random::nextDoubleCentered);
    this.random = random;
    this.interval = interval;
  }

  public void init(double sigma, double m0, double s0, double a0) {
    this.sigma = sigma;
    this.m0 = m0;
    this.s0 = s0;
    this.a0 = a0;
  }

  public void setup(double a1, double a2) {
    this.f1 = Math.sqrt(s0 / (sigma * a0 * a2)) * a1;
    this.f2 = -a2 * a0;
    this.random.reset();
  }

  public double next() {
    double res = 0;
    for (int i = 0; i < interval; i++)
      res += buffer.get(i) * f1 * Math.exp(f2 * i);
    buffer.push(random.nextDoubleCentered());
    return res / interval + m0;
  }
}
