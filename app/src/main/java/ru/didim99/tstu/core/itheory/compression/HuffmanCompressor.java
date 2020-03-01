package ru.didim99.tstu.core.itheory.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import ru.didim99.tstu.core.itheory.compression.utils.HuffmanCharTable;
import ru.didim99.tstu.core.itheory.compression.utils.HuffmanTreeEntry;

/**
 * Created by didim99 on 28.02.20.
 */
public class HuffmanCompressor extends Compressor {
  private static final byte[] HEADER = new byte[] {0x48, 0x46, 0x4D};

  @Override
  public byte[] compress(String data) throws IOException {
    HuffmanCharTable table = new HuffmanCharTable(getFrequency(data));
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DataOutputStream stream = new DataOutputStream(buffer);
    StringBuilder infoBuilder = new StringBuilder();
    StringBuilder msgBuilder = new StringBuilder();

    buffer.write(HEADER);
    table.serializeTo(stream);
    stream.writeInt(data.length());

    ArrayList<Boolean> dataBuffer = new ArrayList<>();
    for (char c : data.toCharArray()) {
      msgBuilder.append(table.getCodeStr(c)).append(' ');
      dataBuffer.addAll(table.getCode(c));
    }

    int offset = dataBuffer.size() % 8;
    while (offset++ < 8) dataBuffer.add(false);

    offset = 0;
    byte bitBuffer = 0;
    for (boolean bit : dataBuffer) {
      if (bit) bitBuffer |= 1 << offset;
      if (++offset == Byte.SIZE) {
        buffer.write(bitBuffer);
        bitBuffer = 0;
        offset = 0;
      }
    }

    int compSize = dataBuffer.size() / 8;
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

    HuffmanCharTable table = HuffmanCharTable.readFrom(stream);
    int totalLength = stream.readInt();
    int compSize = buffer.available();

    ArrayList<Boolean> dataBuffer = new ArrayList<>(compSize);
    while (buffer.available() > 0) {
      byte bitBuffer = stream.readByte();
      int offset = 0;
      while (offset < 8) {
        dataBuffer.add((bitBuffer & 1 << offset++) > 0);
      }
    }

    HuffmanTreeEntry entry = table.getRoot();
    for (boolean bit : dataBuffer) {
      if (entry.isLeaf()) {
        char c = entry.getCharacter();
        msgBuilder.append(table.getCodeStr(c)).append(' ');
        outBuilder.append(c);
        if (--totalLength == 0)
          break;
        entry = table.getRoot();
      }

      entry = entry.getChild(bit);
    }

    String message = outBuilder.toString();
    describe(infoBuilder, message, compSize, compSizeTree, table);
    compressed = msgBuilder.toString().trim();
    info = infoBuilder.toString().trim();
    return message;
  }

  private static void describe(StringBuilder sb, String message, int compSize,
                                 int compSizeTree, HuffmanCharTable table) {
    int origSize = message.getBytes(Charset.defaultCharset()).length;
    sb.append(String.format(Locale.US,
      "Message length: %d characters\n", message.length()));
    sb.append(String.format(Locale.US,
      "Original size: %d bytes\n", origSize));
    sb.append(String.format(Locale.US,
      "Tree size: %d bytes\n", compSizeTree - compSize - 4));
    sb.append(String.format(Locale.US,
      "Compressed size (w/o tree): %d bytes\n", compSize));
    sb.append(String.format(Locale.US,
      "Compressed size (w tree): %d bytes\n", compSizeTree));
    sb.append(String.format(Locale.US,
      "Compression factor (w/o tree): %.1f%%\n", percent(origSize, compSize)));
    sb.append(String.format(Locale.US,
      "Compression factor (w tree): %.1f%%\n", percent(origSize, compSizeTree)));
    sb.append("\nCharacter code table\n");
    sb.append(table.describe());
  }

  private static double percent(int total, int comp) {
    return (total - comp) * 100.0 / total;
  }
}
