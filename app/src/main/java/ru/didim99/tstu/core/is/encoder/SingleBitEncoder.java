package ru.didim99.tstu.core.is.encoder;

import java.util.Locale;
import java.util.Random;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 21.04.19.
 */
public class SingleBitEncoder implements Encoder {
  private static final String[] DATA = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
  private static final int DATA_BITS = 9;

  @Override
  public int generate(Random random) {
    return random.nextInt(DATA_BITS);
  }

  @Override
  public int encode(int frame, StringBuilder logger) {
    int encoded = 1 << frame;
    logger.append(String.format(Locale.ROOT, "%2s -> %010d",
      DATA[frame], Utils.dec2bin(encoded)));
    return encoded;
  }

  @Override
  public int decode(int frame, StringBuilder logger) {
    String data = "Decode error";
    boolean success = false;
    int decoded = 0;

    if (Integer.bitCount(frame) == 1) {
      decoded = Integer.numberOfTrailingZeros(frame);
      data = DATA[decoded];
      success = true;
    }

    logger.append(String.format(Locale.ROOT, "%010d -> %s",
      Utils.dec2bin(frame), data));
    if (!success) throw new ProcessException();
    return decoded;
  }

  @Override
  public String codeInfo() {
    StringBuilder sb = new StringBuilder();
    int code = 1;

    for (String s : DATA) {
      sb.append(String.format(Locale.ROOT, "%2s -> %010d\n",
        s, Utils.dec2bin(code)));
      code <<= 1;
    }

    return sb.toString();
  }

  @Override
  public int dataBits() {
    return DATA_BITS;
  }
}
