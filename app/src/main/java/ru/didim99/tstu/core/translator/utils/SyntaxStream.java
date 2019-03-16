package ru.didim99.tstu.core.translator.utils;

import java.util.ArrayList;
import ru.didim99.tstu.core.translator.LangMap;
import ru.didim99.tstu.core.translator.LangStruct;
import ru.didim99.tstu.core.translator.SyntaxAnalyzer;
import ru.didim99.tstu.utils.Utils;

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

  public int getLineNim() {
    return lineNum;
  }

  public Integer pickNext() {
    if (endOfStream())
      throw new SyntaxAnalyzer.ProcessException("Unexpected end of file", lineNum);
    int nextPos = realPos;
    Integer next = inputStream.get(nextPos);
    while (next == LangMap.INTERNAL.NEWLINE)
      next = inputStream.get(++nextPos);
    return next;
  }

  public Integer next() {
    if (endOfStream())
      throw new SyntaxAnalyzer.ProcessException("Unexpected end of file", lineNum);
    Integer next = inputStream.get(realPos++);
    while (next == LangMap.INTERNAL.NEWLINE) {
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

    String type = "", exp = "";
    LangStruct ls = LangStruct.getInstance();
    String token = ls.getMnemonic(nextSymbol);
    if (token == null) token = "";
    else token = String.format("'%s' ", token);

    if (expected[0] != LangMap.INTERNAL.UNKNOWN) {
      ArrayList<String> list = new ArrayList<>();
      for (Integer possible : expected)
        list.add(ls.getMnemonic(possible));
      exp = Utils.joinStr(", ", list);
      exp = " expecting: ".concat(exp);
    }

    if ((nextSymbol & LangMap.MASK.KEYWORD) > 0)
      type = "(T_KEYWORD)";
    else if ((nextSymbol & LangMap.MASK.OPERATOR) > 0)
      type = "(T_OPERATOR)";
    else if ((nextSymbol & LangMap.MASK.DIVIDER) > 0)
      type = "(T_DIVIDER)";
    else if (nextSymbol.equals(LangMap.CUSTOM.ID))
      type = "T_VARIABLE";
    else if (nextSymbol.equals(LangMap.CUSTOM.LITERAL))
      type = "T_CONST";
    throw new SyntaxAnalyzer.ProcessException(
      String.format("Unexpected %s%s%s", token, type, exp), lineNum);
  }

  public void moveToStart() {
    lineNum = 1;
    realPos = 0;
    pos = 0;
  }

  private boolean endOfStream() {
    return pos >= size;
  }

  void calcSize() {
    realSize = inputStream.size();
    size = 0;

    for (Integer l : inputStream) {
      if (l != LangMap.INTERNAL.NEWLINE)
        size++;
    }
  }
}
