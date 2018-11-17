package ru.didim99.tstu.core.translator;

import java.io.IOException;
import ru.didim99.tstu.core.translator.utils.PrecedenceStream;
import ru.didim99.tstu.core.translator.utils.PrecedenceTable;

/**
 * Created by didim99 on 01.11.18.
 */
class PrecedenceAnalyzer extends SyntaxAnalyzer {

  private PrecedenceStream inputStream;

  PrecedenceAnalyzer(LexicalAnalyzer.Result input, String tableFile)
    throws IOException {
    super(input);
    PrecedenceTable pt = PrecedenceTable.getInstance();
    if (!pt.isInitCompleted())
      pt.initStatic(tableFile);
    inputStream = new PrecedenceStream(input.getLexicalStream(), pt);
  }

  @Override
  Result analyze() {
    int start = 0, pos = 0, end = 0;

    int rel = inputStream.nextPrecedence();
    while (rel != PrecedenceTable.RT.HIGHER) {
      if (rel == PrecedenceTable.RT.LOWER)
        start = pos;
      rel = inputStream.nextPrecedence();
      pos++;
    }

    return result;
  }
}