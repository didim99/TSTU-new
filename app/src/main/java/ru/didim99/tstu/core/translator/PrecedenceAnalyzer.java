package ru.didim99.tstu.core.translator;

import java.util.ArrayList;
import java.util.Iterator;
import ru.didim99.tstu.core.translator.utils.PrecedenceStream;
import ru.didim99.tstu.core.translator.utils.PrecedenceTable;
import ru.didim99.tstu.core.translator.utils.SymbolTable;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 01.11.18.
 */
class PrecedenceAnalyzer extends SyntaxAnalyzer {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_PSA";

  private PrecedenceStream inputStream;
  private SymbolTable st;
  private LangStruct ls;
  private String out;

  PrecedenceAnalyzer(LexicalAnalyzer.Result input) {
    super(input);
    inputStream = new PrecedenceStream(input.getLexicalStream());
    st = result.getSymbolTable();
    ls = LangStruct.getInstance();
  }

  @Override
  Result analyze() {
    ArrayList<String> out = new ArrayList<>();
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

      ArrayList<Integer> range = inputStream.removeRange(start, pos);
      MyLog.d(LOG_TAG, "Detected: " + range);

      Integer l;
      ArrayList<String> line = new ArrayList<>();
      Iterator<Integer> it = range.iterator();
      while (it.hasNext()) {
        l = it.next();
        if (l == LangMap.CUSTOM.ID)
          line.add(st.get(it.next()).getName());
        else if (l == LangMap.CUSTOM.LITERAL)
          line.add(String.valueOf(it.next()));
        else
          line.add(ls.getMnemonic(l));
      }

      out.add(Utils.joinStr(" ", line));
    }

    this.out = Utils.joinStr("\n", out);
    return result;
  }

  String getOutput() {
    return out;
  }
}
