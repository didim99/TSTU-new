package ru.didim99.tstu.core.security.datatransfer.encoder;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 22.04.19.
 */
public class HammingEncoder implements Encoder {
  private static final int DATA_BITS  = 19;
  private static final int CRC_BITS   = 5;
  private static final int FRAME_BITS = DATA_BITS + CRC_BITS;

  @Override
  public int generate(Random random) {
    return random.nextInt(1 << DATA_BITS);
  }

  @Override
  public int encode(int frame, StringBuilder logger) {
    int encoded = 0, pos = 0;

    for (int i = 0; i < FRAME_BITS; i++) {
      if (i + 1 != 1 << pos)
        encoded |= (frame & (1 << (i - pos))) << pos;
      else pos++;
    }

    int crc = parity(encoded);
    int tmp = encoded;
    encoded |= crc;

    logger.append(String.format(Locale.ROOT,
      "%019d -> %024d\n%22s %024d\n%22s %024d\n",
      Utils.dec2bin(frame), Utils.dec2bin(tmp),
      Arrays.toString(crcArray(crc)), Utils.dec2bin(crc),
      "кадр:", Utils.dec2bin(encoded)));
    return encoded;
  }

  @Override
  public int decode(int frame, StringBuilder logger) {
    String bp = "", cr = "", de = "";
    boolean success = true;

    int crc = parity(frame);
    int[] bits = crcArray(crc);
    int pos = 0, decoded = 0;
    int tmpFrame = frame;

    if (crc > 0) {
      for (int i = 0; i < CRC_BITS; i++)
        pos += bits[i] << i;
      bp = " -> " + pos;

      if (pos <= FRAME_BITS) {
        cr = "[CORRECTED]";
        tmpFrame ^= 1 << (pos - 1);
      } else {
        cr = "[UNCORRECTABLE]";
        de = " Decode error";
        success = false;
        tmpFrame = 0;
      }
    }

    if (success) {
      pos = 0;
      for (int i = 0; i < FRAME_BITS; i++) {
        if (i + 1 != 1 << pos)
          decoded |= (tmpFrame & (1 << i)) >> pos;
        else pos++;
      }
    }

    logger.append(String.format(Locale.ROOT,
      "%024d\n%024d %s%s\n%024d %s\n     %019d%s",
      Utils.dec2bin(frame), Utils.dec2bin(crc),
      Arrays.toString(bits), bp,
      Utils.dec2bin(tmpFrame), cr,
      Utils.dec2bin(decoded), de));
    if (!success) throw new ProcessException();
    return decoded;
  }

  @Override
  public String codeInfo() {
    StringBuilder sb = new StringBuilder();
    int[] rank = new int[CRC_BITS];
    int offset = 0;

    for (int i = 1; i <= FRAME_BITS; i++) {
      sb.append(i == 1 << offset ? ++offset : "-");
      for (int r = 0; r < CRC_BITS; r++)
        rank[r] |= (i & (1 << r)) << (i - r - 1);
    }

    sb.reverse();
    String fmt = String.format(
      Locale.ROOT, "\n  %%0%dd", FRAME_BITS);
    for (int r = 0; r < CRC_BITS; r++)
      sb.append(String.format(fmt, Utils.dec2bin(rank[r])));

    return String.format(Locale.ROOT,
      "%22s: %d\n%22s: %d\n%22s:\n\n  %s",
      "Биты данных", DATA_BITS,
      "Контрольные биты", CRC_BITS,
      "Контрольные группы", sb.toString()
    );
  }

  @Override
  public int dataBits() {
    return FRAME_BITS;
  }

  private static int parity(int frame) {
    int pos = 0, crc = 0, min, max, parity, s ,j;
    for (int i = 1; i <= FRAME_BITS; i = 1 << pos) {
      pos++;
      min = 1;
      parity = 0;
      j = max = i;

      for ( ; j <= FRAME_BITS; ) {
        for (s = j; max >= min && s <= FRAME_BITS; min++, s++) {
          if ((frame & (1 << (s - 1))) > 0) parity++;
        }

        j = s + i;
        min = 1;
      }

      crc |= (parity % 2) << (i - 1);
    }

    return crc;
  }

  private static int[] crcArray(int frame) {
    int[] crc = new int[CRC_BITS];
    int pos = 0;

    for (int i = 0; i < FRAME_BITS; i++) {
      if (i + 1 == 1 << pos) {
        crc[pos++] = (frame & (1 << i)) >> i;
      }
    }

    return crc;
  }
}
