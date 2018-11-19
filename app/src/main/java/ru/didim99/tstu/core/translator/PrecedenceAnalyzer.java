package ru.didim99.tstu.core.translator;

import java.io.IOException;
import ru.didim99.tstu.core.translator.utils.PrecedenceStream;
import ru.didim99.tstu.core.translator.utils.PrecedenceTable;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 01.11.18.
 */
class PrecedenceAnalyzer extends SyntaxAnalyzer {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_PSA";

  private PrecedenceStream inputStream;

  PrecedenceAnalyzer(LexicalAnalyzer.Result input, String tableFile)
    throws IOException {
    super(input);
    PrecedenceTable pt = PrecedenceTable.getInstance();
    if (!pt.isInitCompleted())
      pt.initStatic(tableFile);
    inputStream = new PrecedenceStream(
      input.getLexicalStream(), pt);
  }

  @Override
  Result analyze() {
    while (inputStream.getSize() > 0) {
      inputStream.moveToStart();
      int start = 0, pos = 0;
      int rel = inputStream.nextPrecedence();

      while (rel != PrecedenceTable.RT.HIGHER) {
        if (rel == PrecedenceTable.RT.LOWER)
          start = pos + 1;
        rel = inputStream.nextPrecedence();
        pos++;
      }

      MyLog.d(LOG_TAG, "Detected: " +
        inputStream.removeRange(start, pos));
    }

    return result;
  }
}
