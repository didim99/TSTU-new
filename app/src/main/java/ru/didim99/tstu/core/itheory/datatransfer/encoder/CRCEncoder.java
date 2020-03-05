package ru.didim99.tstu.core.itheory.datatransfer.encoder;

import java.util.Locale;
import java.util.Random;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 22.04.19.
 */
public class CRCEncoder implements Encoder {
  private static final int DATA_BITS  = 6;
  private static final int CRC_BITS   = 4;
  private static final int FRAME_BITS = DATA_BITS + CRC_BITS;
  private static final int POLY       = 0x0F;

  @Override
  public int generate(Random random) {
    return random.nextInt(1 << DATA_BITS);
  }

  @Override
  public int encode(int frame, StringBuilder logger) {
    int encoded = frame << CRC_BITS;
    int crc = polyMod(encoded);
    encoded |= crc;
    logger.append(String.format(Locale.ROOT, " %06d -> %04d -> %010d",
      Utils.dec2bin(frame), Utils.dec2bin(crc), Utils.dec2bin(encoded)));
    return encoded;
  }

  @Override
  public int decode(int frame, StringBuilder logger) {
    String data = "Decode error";
    boolean success = false;
    int decoded = 0;

    int err = polyMod(frame);
    if (err == 0) {
      decoded = frame >> CRC_BITS;
      data = String.format(Locale.ROOT,
        "%06d", Utils.dec2bin(decoded));
      success = true;
    }

    logger.append(String.format(Locale.ROOT, "%010d -> %04d -> %s",
      Utils.dec2bin(frame),  Utils.dec2bin(err), data));
    if (!success) throw new ProcessException();
    return decoded;
  }

  @Override
  public String codeInfo() {
    return String.format(Locale.ROOT,
      "%22s: %d\n%22s: %d\n%22s: %08d",
      "Биты данных", DATA_BITS,
      "Контрольные биты", CRC_BITS,
      "Пораждающий полином", Utils.dec2bin(POLY)
    );
  }

  @Override
  public int dataBits() {
    return FRAME_BITS;
  }

  private static int polyMod(int data) {
    int crc = 0;

    for (int i = FRAME_BITS - 1; i >= 0; i--) {
      crc = crc << 1 | ((data >> i) & 0x01);
      if ((crc & 1 << CRC_BITS - 1) > 0) crc ^= POLY;
    }

    return crc;
  }
}
