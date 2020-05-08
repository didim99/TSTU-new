package ru.didim99.tstu;

import org.junit.Test;

import ru.didim99.tstu.core.modeling.randproc.CyclicBuffer;
import ru.didim99.tstu.core.modeling.randproc.DiscreteMath;
import ru.didim99.tstu.core.modeling.randproc.Random;
import ru.didim99.tstu.core.modeling.randproc.RandomProcessor;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
  @Test
  public void randomSpread() {
    Random random = new Random(48828125, 1L << 18, 1);
    CyclicBuffer buffer = new CyclicBuffer(1000);
    buffer.fill(random::nextDoubleCentered);
    double prev = DiscreteMath.spread(buffer.getAll());
    double mean = DiscreteMath.mean(buffer.getAll());
    assertEquals(mean, 0, 5E-2);

    int iterations = 0;
    while (++iterations <= 10) {
      buffer.fill(random::nextDoubleCentered);
      double next = DiscreteMath.spread(buffer.getAll());
      mean = DiscreteMath.mean(buffer.getAll());
      System.out.println("Iteration: " + iterations + " mean: " + mean);
      assertEquals(mean, 0, 5E-2);
      assertEquals(prev, next, prev / 10);
    }
  }

  @Test
  public void sampleTest() {
    RandomProcessor processor = new RandomProcessor();
    processor.process();
    System.out.println(processor.getDescription());
  }
}