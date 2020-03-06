package ru.didim99.tstu.core.itheory.encryption.math;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by didim99 on 06.03.20.
 */
public class SieveEratosthenes {
  private List<PrimePair> primes;

  public SieveEratosthenes() {
    primes = new ArrayList<>();
    primes.add(new PrimePair(2, 2));
    primes.add(new PrimePair(3, 3));
  }

  public long get(int index) {
    return primes.get(index).prime;
  }

  public int getSize() {
    return primes.size();
  }

  public void fillPrimesUpTo(long maxNumber) {
    while (getLastNumber() < maxNumber)
      addNextPrime();
  }

  private void addNextPrime() {
    long candidate = getLastNumber() + 2;
    for (int i = 1; i < primes.size(); i++) {
      PrimePair p = primes.get(i);
      while (p.lastCrossed < candidate)
        p.lastCrossed += p.prime;
      if (p.lastCrossed == candidate) {
        candidate += 2; //restart
        i =- 1;
      }
    }

    primes.add(new PrimePair(candidate, candidate));
  }

  private long getLastNumber() {
    return primes.get(primes.size() - 1).prime;
  }

  private static class PrimePair {
    private long prime, lastCrossed;

    private PrimePair(long prime, long lastCrossed) {
      this.lastCrossed = lastCrossed;
      this.prime = prime;
    }
  }
}
