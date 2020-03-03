package ru.didim99.tstu.core.itheory.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import ru.didim99.tstu.core.itheory.compression.utils.BitStream;
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
    BitStream bitStream = new BitStream(buffer, null);
    DataOutputStream stream = new DataOutputStream(buffer);
    StringBuilder infoBuilder = new StringBuilder();
    StringBuilder msgBuilder = new StringBuilder();

    buffer.write(HEADER);
    table.serializeTo(stream);
    stream.writeInt(data.length());

    for (char c : data.toCharArray()) {
      msgBuilder.append(table.getCodeStr(c)).append(' ');
      for (Boolean b : table.getCode(c))
        bitStream.pushBit(b ? 1 : 0);
    }

    bitStream.flush();
    int compSize = bitStream.size();
    int compSizeTree = buffer.size();

    describe(infoBuilder, data, compSize, compSizeTree, table);
    compressed = msgBuilder.toString().trim();
    info = infoBuilder.toString().trim();
    return buffer.toByteArray();
  }

  @Override
  public String decompress(byte[] data) throws IOException {
    ByteArrayInputStream buffer = new ByteArrayInputStream(data);
    BitStream bitStream = new BitStream(buffer, null);
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

    HuffmanCharTable table = new HuffmanCharTable(stream);
    int totalLength = stream.readInt();
    int compSize = buffer.available();

    HuffmanTreeEntry entry = table.getRoot();
    while (totalLength > 0) {
      if (entry.isLeaf()) {
        char c = entry.getCharacter();
        msgBuilder.append(table.getCodeStr(c)).append(' ');
        outBuilder.append(c);
        entry = table.getRoot();
        totalLength--;
      }

      entry = entry.getChild(bitStream.pullBit());
    }

    String message = outBuilder.toString();
    describe(infoBuilder, message, compSize, compSizeTree, table);
    compressed = msgBuilder.toString().trim();
    info = infoBuilder.toString().trim();
    return message;
  }
}
