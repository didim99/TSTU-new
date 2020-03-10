package ru.didim99.tstu.core.itheory.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import ru.didim99.tstu.core.itheory.compression.utils.LZWHashTable;

/**
 * Created by didim99 on 07.03.20.
 */
public class LZWCompressor extends Compressor {
  private static final byte[] HEADER = {0x4C, 0x5A, 0x57};
  private static final int RATIO_CHECK_INTERVAL = 100;
  private static final double RATIO_POOR = 2.0;
  private static final double RATIO_DELTA = 0.2;

  @Override
  public byte[] compress(String data) throws IOException {
    ByteArrayInputStream inBuffer = new ByteArrayInputStream(data.getBytes());
    ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(outBuffer);
    StringBuilder infoBuilder = new StringBuilder();
    StringBuilder msgBuilder = new StringBuilder();

    ArrayList<Integer> buffer = new ArrayList<>(LZWHashTable.BUFFER_SIZE);
    LZWHashTable table = new LZWHashTable(false);
    int marker = getMarker(inBuffer);

    outBuffer.write(HEADER);
    outBuffer.write(marker);

    double bestRatio = RATIO_POOR;
    int read = 1, readAfter = 0;
    int symbol, code;

    buffer.add(inBuffer.read());
    int length = inBuffer.available();
    while (read++ <= length) {
      symbol = inBuffer.read();
      buffer.add(symbol);

      if (table.contains(buffer))
        continue;
      if (readAfter > 0 || table.isFull()) {
        if (++readAfter == RATIO_CHECK_INTERVAL) {
          readAfter = 0;
          double ratio = getRatio(read, out.size());
          if (ratio < bestRatio) {
            bestRatio = ratio;
          } else if (ratio - bestRatio < RATIO_DELTA) {
            writeAndDescribe(marker, out, msgBuilder);
            table.rebuild();
          }
        }
      }

      code = table.add(buffer);
      if (code == marker)
        writeAndDescribe(marker, out, msgBuilder);
      writeAndDescribe(code, out, msgBuilder);
      buffer.clear();
      buffer.add(symbol);
    }

    code = table.getCode(buffer);
    if (code == marker)
      writeAndDescribe(marker, out, msgBuilder);
    writeAndDescribe(code, out, msgBuilder);

    describe(infoBuilder, data, outBuffer.size(), marker, table);
    compressed = msgBuilder.toString().trim();
    info = infoBuilder.toString().trim();
    return outBuffer.toByteArray();
  }

  private int getMarker(InputStream in) throws IOException {
    int[] buffer = new int[256];
    int length = in.available();
    while (length-- > 0)
      buffer[in.read()]++;
    in.reset();

    int marker = 0, minValue = Integer.MAX_VALUE;
    for (int i = 0; i < buffer.length; i++) {
      if (buffer[i] < minValue) {
        minValue = buffer[i];
        marker = i;
      }
    }

    return marker;
  }

  private void writeAndDescribe(int code, DataOutput out, StringBuilder sb)
    throws IOException {
    sb.append(code).append(' ');
    out.writeShort(code);
  }

  @Override
  public String decompress(byte[] data) throws IOException {
    ByteArrayInputStream inBuffer = new ByteArrayInputStream(data);
    ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    DataInputStream in = new DataInputStream(inBuffer);
    StringBuilder infoBuilder = new StringBuilder();
    StringBuilder msgBuilder = new StringBuilder();

    int compSize = data.length;
    byte[] header = new byte[HEADER.length];
    if (inBuffer.read(header) < HEADER.length)
      throw new IOException("Unexpected end of file");
    if (!Arrays.equals(header, HEADER))
      throw new IOException("Invalid file header");

    LZWHashTable table = new LZWHashTable(true);
    int marker = inBuffer.read();

    int oldCode = readAndDescribe(inBuffer, in, marker, msgBuilder);
    writeSequence(table.getSequence(oldCode), outBuffer);
    int symbol = oldCode;

    ArrayList<Integer> buffer;
    while (inBuffer.available() > 0) {
      int newCode = readAndDescribe(inBuffer, in, marker, msgBuilder);
      if (newCode == -marker) {
        table.rebuild();
        continue;
      }

      if (table.contains(newCode)) {
        buffer = table.getSequence(newCode);
      } else {
        buffer = table.getSequence(oldCode);
        buffer.add(symbol);
      }

      writeSequence(buffer, outBuffer);
      symbol = buffer.get(0);
      table.add(oldCode, symbol);
      oldCode = newCode;
    }

    String message = new String(outBuffer.toByteArray());
    describe(infoBuilder, message, compSize, marker, table);
    compressed = msgBuilder.toString().trim();
    info = infoBuilder.toString().trim();
    return message;
  }

  private int readAndDescribe(InputStream buffer, DataInput in,
                              int marker, StringBuilder msgBuilder)
    throws IOException {
    int symbol = in.readShort();
    msgBuilder.append(symbol).append(' ');
    if (symbol == marker) {
      buffer.mark(0);
      int nextSymbol = in.readShort();
      if (nextSymbol == marker) {
        msgBuilder.append(nextSymbol).append(' ');
        return nextSymbol;
      } else {
        buffer.reset();
        return -marker;
      }
    } else return symbol;
  }

  private void writeSequence(ArrayList<Integer> sequence, OutputStream out)
    throws IOException {
    for (Integer i : sequence) out.write(i);
  }

  private static void describe(StringBuilder sb, String message, int compSize,
                               int marker, LZWHashTable table) {
    int origSize = message.getBytes(Charset.defaultCharset()).length;
    sb.append(String.format(Locale.US,
      "Message length: %d characters\n", message.length()));
    sb.append(String.format(Locale.US,
      "Original size: %d bytes\n", origSize));
    sb.append(String.format(Locale.US,
      "Compressed size: %d bytes\n", compSize));
    sb.append(String.format(Locale.US,
      "Compression factor: %.1f%%\n", percent(origSize, compSize)));
    sb.append(String.format(Locale.US,
      "Marker byte: %d\n\n", marker));
    sb.append(table.describe());
  }
}
