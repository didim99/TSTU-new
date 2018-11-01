package ru.didim99.tstu.core.translator;

import java.io.IOException;

/**
 * Created by didim99 on 01.11.18.
 */
class PrecedenceAnalyzer extends SyntaxAnalyzer {

  private PrecedenceTable pt;

  PrecedenceAnalyzer(LexicalAnalyzer.Result input, String tableFile)
    throws IOException {
    super(input);
    pt = PrecedenceTable.getInstance();
    if (!pt.isInitCompleted())
      pt.initStatic(tableFile);
  }

  @Override
  Result analyze() {


    return result;
  }

}
