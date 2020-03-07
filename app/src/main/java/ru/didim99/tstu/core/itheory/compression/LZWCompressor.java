package ru.didim99.tstu.core.itheory.compression;

import java.io.IOException;

/**
 * Created by didim99 on 07.03.20.
 */
public class LZWCompressor extends Compressor {
  private static final byte[] HEADER = {0x4C, 0x5A, 0x57};

  @Override
  public byte[] compress(String data) throws IOException {
    return new byte[0];
  }

  @Override
  public String decompress(byte[] data) throws IOException {
    return null;
  }
}
