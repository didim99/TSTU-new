package ru.didim99.tstu.core.itheory.compression.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ru.didim99.tstu.core.itheory.compression.ArithmeticCompressor;

/**
 * Created by didim99 on 02.03.20.
 */
public class BitStream {
  private static final int MAX_GARBAGE = ArithmeticCompressor.CODE_BITS - 2;
  private static final int MAX_OFFSET = Byte.SIZE - 1;

  private InputStream input;
  private OutputStream output;
  private StringBuilder msgBuilder;
  private int count, follow, garbage;
  private int buffer, offset;

  public BitStream(InputStream input, StringBuilder msgBuilder) {
    this.msgBuilder = msgBuilder;
    this.input = input;
    this.buffer = 0;
    this.offset = 0;
    this.garbage = 0;
    this.count = 0;
  }

  public BitStream(OutputStream output, StringBuilder msgBuilder) {
    this.msgBuilder = msgBuilder;
    this.output = output;
    this.buffer = 0;
    this.offset = 0;
    this.follow = 0;
    this.count = 0;
  }

  public void followBit() {
    follow++;
  }

  public int size() {
    return count;
  }

  public int pullBit() throws IOException {
    if (offset == 0) {
      if (input.available() > 0) {
        buffer = input.read();
        writeMessage(buffer);
        offset = MAX_OFFSET + 1;
        count++;
      } else if (++garbage > MAX_GARBAGE)
        throw new EOFException();
    }

    int bit = buffer & 0x01;
    buffer >>= 1;
    offset--;
    return bit;
  }

  public void pushBit(int bit) throws IOException {
    writeBit(bit);
    while (follow > 0) {
      writeBit(~bit);
      follow--;
    }
  }

  public void flush() throws IOException {
    writeMessage(buffer);
    output.write(buffer);
    buffer = offset = 0;
    count++;
  }

  private void writeBit(int bit) throws IOException {
    if (bit > 0) buffer |= 1 << offset;
    if (++offset == MAX_OFFSET) flush();
  }

  private void writeMessage(int buffer) {
    if (msgBuilder != null) msgBuilder.append(
      Integer.toHexString(buffer)).append(' ');
  }
}
