package ru.didim99.tstu.core.translator.utils;

import java.util.ArrayList;

import ru.didim99.tstu.core.translator.InLang;
import ru.didim99.tstu.core.translator.SyntaxAnalyzer;

/**
 * Created by didim99 on 03.11.18.
 */
public class SyntaxStream {
  final ArrayList<Integer> inputStream;
  int lineNum, pos, size;
  int realPos, realSize;

  public SyntaxStream(ArrayList<Integer> input) {
    this.inputStream = input;
    moveToStart();
    calcSize();
  }

  public int getSize() {
    return size;
  }

  public Integer pickNext() {
    if (!hasNext())
      throw new SyntaxAnalyzer.ProcessException("Unexpected end of file", lineNum);
    int nextPos = realPos;
    Integer next = inputStream.get(nextPos);
    while (next == InLang.INTERNAL.NEWLINE)
      next = inputStream.get(++nextPos);
    return next;
  }

  public Integer next() {
    if (!hasNext())
      throw new SyntaxAnalyzer.ProcessException("Unexpected end of file", lineNum);
    Integer next = inputStream.get(realPos++);
    while (next == InLang.INTERNAL.NEWLINE) {
      next = inputStream.get(realPos++);
      lineNum++;
    }

    pos++;
    return next;
  }

  public int validateNext(Integer... expected) {
    Integer nextSymbol = next();
    for (Integer possible : expected) {
      if (nextSymbol.equals(possible))
        return possible;
    }

    String type = "";
    if ((nextSymbol & InLang.MASK.KEYWORD) > 0)
      type = "T_KEYWORD";
    else if ((nextSymbol & InLang.MASK.OPERATOR) > 0)
      type = "T_OPERATOR";
    else if ((nextSymbol & InLang.MASK.DIVIDER) > 0)
      type = "T_DIVIDER";
    else if (nextSymbol.equals(InLang.CUSTOM.ID))
      type = "T_VARIABLE";
    else if (nextSymbol.equals(InLang.CUSTOM.LITERAL))
      type = "T_CONST";
    throw new SyntaxAnalyzer.ProcessException(String.format("Unexpected %s", type), lineNum);
  }

  public void moveToStart() {
    lineNum = 1;
    realPos = 0;
    pos = 0;
  }

  boolean hasNext() {
    return pos < size;
  }

  void calcSize() {
    realSize = inputStream.size();
    size = 0;

    for (Integer l : inputStream) {
      if (l != InLang.INTERNAL.NEWLINE)
        size++;
    }
  }
}
