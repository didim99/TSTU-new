package ru.didim99.tstu.core.translator.utils;

import java.util.ArrayList;
import ru.didim99.tstu.core.translator.LangStruct;
import ru.didim99.tstu.core.translator.SyntaxAnalyzer;

/**
 * Created by didim99 on 03.11.18.
 */
public class PrecedenceStream extends SyntaxStream {
  private PrecedenceTable pt;

  public PrecedenceStream(ArrayList<Integer> input, PrecedenceTable pt) {
    super(input);
    this.pt = pt;
  }

  public int nextPrecedence() {
    if (!hasNext(2))
      throw new SyntaxAnalyzer.ProcessException("Unexpected end of file", lineNum);
    int rel = pt.get(inputStream.get(realPos), inputStream.get(++realPos));
    if (rel == PrecedenceTable.RT.NONE)
      validateNext(LangStruct.INTERNAL.UNKNOWN);
    return rel;
  }

  public boolean hasNext(int steps) {
    return realPos + steps <= realSize;
  }
}
