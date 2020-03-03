package ru.didim99.tstu.core.itheory.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import ru.didim99.tstu.core.itheory.compression.utils.ArithmeticCharTable;
import ru.didim99.tstu.core.itheory.compression.utils.BitStream;

/**
 * Created by didim99 on 01.03.20.
 */
public class ArithmeticCompressor extends Compressor {
  private static final byte[] HEADER = new byte[] {0x41, 0x52, 0x4D};

  public static final int CODE_BITS = 32;
  private static final long CODE_MIN = 0L;
  private static final long CODE_MAX = (1L << CODE_BITS) - 1;
  private static final long FIRST_QTR = CODE_MAX / 4 + 1;
  private static final long HALF = FIRST_QTR * 2;
  private static final long THIRD_QTR = FIRST_QTR * 3;
  public static final long MAX_FREQ = (1L << (CODE_BITS - 2)) - 1;

  @Override
  public byte[] compress(String data) throws IOException {
    ArithmeticCharTable table = new ArithmeticCharTable(getFrequency(data));
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DataOutputStream stream = new DataOutputStream(buffer);
    StringBuilder infoBuilder = new StringBuilder();
    StringBuilder msgBuilder = new StringBuilder();
    BitStream bitStream = new BitStream(stream, msgBuilder);

    buffer.write(HEADER);
    table.serializeTo(stream);
    stream.writeInt(data.length());

    long start = CODE_MIN, end = CODE_MAX;
    long maxFreq = table.getMaxFrequency();
    for (char c : data.toCharArray()) {
      long range = end - start + 1;
      end = start + table.getMaxValue(c) * range / maxFreq - 1;
      start += table.getMinValue(c) * range / maxFreq;

      while (true) {
        if (end < HALF)
          bitStream.pushBit(0);
        else if (start >= HALF) {
          bitStream.pushBit(1);
          start -= HALF;
          end -= HALF;
        } else if (start >= FIRST_QTR && end < THIRD_QTR) {
          bitStream.followBit();
          start -= FIRST_QTR;
          end -= FIRST_QTR;
        } else break;
        end = end * 2 + 1;
        start *= 2;
      }
    }

    bitStream.followBit();
    bitStream.pushBit(start < FIRST_QTR ? 0 : 1);
    bitStream.flush();

    describe(infoBuilder, data, bitStream.size(), buffer.size(), table);
    compressed = msgBuilder.toString().trim();
    info = infoBuilder.toString().trim();
    return buffer.toByteArray();
  }

  @Override
  public String decompress(byte[] data) throws IOException {
    ByteArrayInputStream buffer = new ByteArrayInputStream(data);
    DataInputStream stream = new DataInputStream(buffer);
    StringBuilder infoBuilder = new StringBuilder();
    StringBuilder msgBuilder = new StringBuilder();
    StringBuilder outBuilder = new StringBuilder();

    int compSizeTree = data.length;
    byte[] header = new byte[HEADER.length];
    if (buffer.read(header) < HEADER.length)
      throw new IOException("Unexpected end of file");
    if (!Arrays.equals(header, HEADER))
      throw new IOException("Invalid file header");

    ArithmeticCharTable table = new ArithmeticCharTable(stream);
    BitStream bitStream = new BitStream(stream, msgBuilder);
    int totalLength = stream.readInt();
    int compSize = buffer.available();

    long value = 0;
    long start = CODE_MIN, end = CODE_MAX;
    long maxFreq = table.getMaxFrequency();
    for (int i = 0; i < CODE_BITS; i++)
      value = (value << 1) + bitStream.pullBit();

    while (totalLength-- > 0) {
      long range = end - start + 1;
      long cum = ((value - start + 1) * maxFreq - 1) / range;
      char c = table.getCharacter(cum);
      end = start + table.getMaxValue(c) * range / maxFreq - 1;
      start += table.getMinValue(c) * range / maxFreq;
      outBuilder.append(c);

      while (true) {
        if (end < HALF) {
          /* Nothing to do */
        } else if (start >= HALF) {
          value -= HALF;
          start -= HALF;
          end -= HALF;
        } else if (start >= FIRST_QTR && end < THIRD_QTR) {
          value -= FIRST_QTR;
          start -= FIRST_QTR;
          end -= FIRST_QTR;
        } else break;
        value = (value << 1) + bitStream.pullBit();
        end = end * 2 + 1;
        start *= 2;
      }
    }

    String message = outBuilder.toString();
    describe(infoBuilder, message, compSize, compSizeTree, table);
    compressed = msgBuilder.toString().trim();
    info = infoBuilder.toString().trim();
    return message;
  }
}
