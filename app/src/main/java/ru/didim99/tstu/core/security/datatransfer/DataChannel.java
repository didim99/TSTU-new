package ru.didim99.tstu.core.security.datatransfer;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by didim99 on 21.04.19.
 */
class DataChannel {
  private Random random;
  private boolean hasNoise;
  private int brokenBlocks;
  private int brokenBits;

  DataChannel() {
    random = new Random();
  }

  void setHasNoise(boolean hasNoise) {
    this.hasNoise = hasNoise;
  }

  void configure(int brokenBlocks, int brokenBits) {
    this.brokenBlocks = brokenBlocks;
    this.brokenBits = brokenBits;
  }

  void transfer(ArrayList<Integer> data, int maxBit) {
    if (!hasNoise) return;
    int frame, pos, maxPos = data.size();
    int blocks = brokenBlocks, bits;

    while (blocks-- > 0) {
      pos = random.nextInt(maxPos);
      frame = data.get(pos);
      bits = brokenBits;
      while (bits-- > 0)
        frame ^= 1 << random.nextInt(maxBit);
      data.set(pos, frame);
    }
  }
}
