package ru.didim99.tstu.core.translator.utils;

import java.util.ArrayList;
import ru.didim99.tstu.core.translator.LangStruct;
import ru.didim99.tstu.core.translator.SyntaxAnalyzer;

/**
 * Created by didim99 on 03.11.18.
 */
public class SyntaxStream {
  final ArrayList<Integer> inputStream;
  int realPos, realSize, lineNum;

  public SyntaxStream(ArrayList<Integer> input) {
    this.inputStream = input;
    this.realSize = input.size();
    this.lineNum = 1;
    this.realPos = 0;
  }

  public Integer pickNext() {
    if (!hasNext())
      throw new SyntaxAnalyzer.ProcessException("Unexpected end of file", lineNum);
    int nextPos = realPos;
    Integer next = inputStream.get(nextPos);
    while (next == LangStruct.INTERNAL.NEWLINE)
      next = inputStream.get(++nextPos);
    return next;
  }

  public Integer next() {
    if (!hasNext())
      throw new SyntaxAnalyzer.ProcessException("Unexpected end of file", lineNum);
    Integer next = inputStream.get(realPos++);
    while (next == LangStruct.INTERNAL.NEWLINE) {
      next = inputStream.get(realPos++);
      lineNum++;
    }

    return next;
  }

  public int validateNext(Integer... expected) {
    Integer nextSymbol = next();
    for (Integer possible : expected) {
      if (nextSymbol.equals(possible))
        return possible;
    }

    String type = "";
    if ((nextSymbol & LangStruct.MASK.KEYWORD) > 0)
      type = "T_KEYWORD";
    else if ((nextSymbol & LangStruct.MASK.OPERATOR) > 0)
      type = "T_OPERATOR";
    else if ((nextSymbol & LangStruct.MASK.DIVIDER) > 0)
      type = "T_DIVIDER";
    else if (nextSymbol.equals(LangStruct.CUSTOM.ID))
      type = "T_VARIABLE";
    else if (nextSymbol.equals(LangStruct.CUSTOM.LITERAL))
      type = "T_CONST";
    throw new SyntaxAnalyzer.ProcessException(String.format("Unexpected %s", type), lineNum);
  }

  boolean hasNext() {
    return realPos < realSize;
  }
}
