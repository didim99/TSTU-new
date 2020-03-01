package ru.didim99.tstu.core.itheory.compression;

import java.io.IOException;

/**
 * Created by didim99 on 01.03.20.
 */
public class ArithmeticCompressor extends Compressor {
  private static final byte[] HEADER = new byte[] {0x41, 0x52, 0x4D};

  @Override
  public byte[] compress(String data) throws IOException {
    return new byte[0];
  }

  @Override
  public String decompress(byte[] data) throws IOException {
    return null;
  }
}
