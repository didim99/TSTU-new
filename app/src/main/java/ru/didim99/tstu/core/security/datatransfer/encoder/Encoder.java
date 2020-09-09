package ru.didim99.tstu.core.security.datatransfer.encoder;

import java.util.Random;

/**
 * Created by didim99 on 21.04.19.
 */
public interface Encoder {
  int generate(Random random);
  int encode(int frame, StringBuilder logger);
  int decode(int frame, StringBuilder logger);
  String codeInfo();
  int dataBits();

  class ProcessException extends IllegalArgumentException {
    public ProcessException() { super(); }
  }
}
