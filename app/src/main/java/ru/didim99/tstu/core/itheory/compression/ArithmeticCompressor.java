package ru.didim99.tstu.core.itheory.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import ru.didim99.tstu.core.itheory.compression.utils.ArithmeticCharTable;

/**
 * Created by didim99 on 01.03.20.
 */
public class ArithmeticCompressor extends Compressor {
  private static final byte[] HEADER = new byte[] {0x41, 0x52, 0x4D};
  private static final int SEQUENCE_SIZE = 20;

  @Override
  public byte[] compress(String data) throws IOException {
    ArithmeticCharTable table = new ArithmeticCharTable(getFrequency(data));
    BigDecimal start = ArithmeticCharTable.DEFAULT_MIN;
    BigDecimal end = ArithmeticCharTable.DEFAULT_MAX;

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DataOutputStream stream = new DataOutputStream(buffer);
    StringBuilder infoBuilder = new StringBuilder();
    StringBuilder msgBuilder = new StringBuilder();

    buffer.write(HEADER);
    table.serializeTo(stream);
    stream.writeInt(data.length());
    int compSize = 0;

    int read = 0;
    for (char c : data.toCharArray()) {
      BigDecimal interval = end.subtract(start);
      end = start.add(interval.multiply(table.getMaxValue(c)));
      start = start.add(interval.multiply(table.getMinValue(c)));

      if (++read == SEQUENCE_SIZE) {
        double value = start.doubleValue();
        msgBuilder.append(value).append(' ');
        stream.writeDouble(value);
        start = ArithmeticCharTable.DEFAULT_MIN;
        end = ArithmeticCharTable.DEFAULT_MAX;
        compSize += Double.SIZE / Byte.SIZE;
        read = 0;
      }
    }

    if (read > 0) {
      double value = start.doubleValue();
      msgBuilder.append(value).append(' ');
      stream.writeDouble(value);
      compSize += Double.SIZE / Byte.SIZE;
    }

    int compSizeTree = buffer.size();
    describe(infoBuilder, data, compSize, compSizeTree, table);
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
    int totalLength = stream.readInt();
    int compSize = buffer.available();

    double value = stream.readDouble();
    msgBuilder.append(value).append(' ');
    BigDecimal number = BigDecimal.valueOf(value);

    int read = 0;
    while (totalLength-- > 0) {
      char c = table.getCharacter(number);
      outBuilder.append(c);
      number = number.subtract(table.getMinValue(c));
      number = number.divide(table.getInterval(c), RoundingMode.HALF_UP);

      if (++read == SEQUENCE_SIZE) {
        value = stream.readDouble();
        msgBuilder.append(value).append(' ');
        number = BigDecimal.valueOf(value);
        read = 0;
      }
    }

    String message = outBuilder.toString();
    describe(infoBuilder, message, compSize, compSizeTree, table);
    compressed = msgBuilder.toString().trim();
    info = infoBuilder.toString().trim();
    return message;
  }
}
