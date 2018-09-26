package ru.didim99.tstu.core.translator;

import java.util.ArrayList;

/**
 * Created by didim99 on 24.09.18.
 */
class SyntaxAnalyzer {
  private SyntaxStream stream;

  SyntaxAnalyzer(ArrayList<Integer> input) {
    this.stream = new SyntaxStream(input);
  }

  void analyze() {
    checkProgram();
  }

  /* ======== SYNTAX IMPLEMENTATION ========== */

  private void checkProgram() {
    checkHeader();
    stream.validateNext(LangStruct.DIVIDER.END_OP);
    checkSectionVar();
    checkSectionOp();
    stream.validateNext(LangStruct.DIVIDER.END_PROG);
  }

  private void checkHeader() {
    stream.validateNext(LangStruct.KEYWORD.PROGRAM);
    checkVarName();
  }

  private void checkVarName() {
    stream.validateNext(LangStruct.CUSTOM.ID);
    stream.next();
  }

  private void checkSectionVar() {
    stream.validateNext(LangStruct.KEYWORD.VAR);
    checkVarList();
    stream.validateNext(LangStruct.DIVIDER.END_VL);
    checkType();
    stream.validateNext(LangStruct.DIVIDER.END_OP);
  }

  private void checkVarList() {
    checkVarName();
    if (stream.pickNext() == LangStruct.DIVIDER.SEP_VL) {
      stream.next();
      checkVarList();
    }
  }

  private void checkType() {
    stream.validateNext(LangStruct.KEYWORD.INTEGER, LangStruct.KEYWORD.REAL);
  }

  private void checkSectionOp() {
    stream.validateNext(LangStruct.KEYWORD.BEGIN);
    checkOpList();
    stream.validateNext(LangStruct.KEYWORD.END);
  }

  private void checkOpList() {
    checkOperator();
    if (stream.pickNext() == LangStruct.DIVIDER.END_OP) {
      stream.next();
      checkOpList();
    }
  }

  private void checkOperator() {
    switch (stream.pickNext()) {
      case LangStruct.KEYWORD.READ:   checkInput(); break;
      case LangStruct.KEYWORD.WRITE:  checkOutput(); break;
      case LangStruct.KEYWORD.FOR:    checkCycle(); break;
      case LangStruct.CUSTOM.ID:      checkAssign(); break;
      default: stream.validateNext(LangStruct.INTERNAL.UNKNOWN);
    }
  }

  private void checkInput() {
    stream.validateNext(LangStruct.KEYWORD.READ);
    stream.validateNext(LangStruct.DIVIDER.BEG_CALL);
    checkVarList();
    stream.validateNext(LangStruct.DIVIDER.END_CALL);
  }

  private void checkOutput() {
    stream.validateNext(LangStruct.KEYWORD.WRITE);
    stream.validateNext(LangStruct.DIVIDER.BEG_CALL);
    checkVarList();
    stream.validateNext(LangStruct.DIVIDER.END_CALL);
  }

  private void checkAssign() {
    checkVarName();
    stream.validateNext(LangStruct.OPERATOR.ASSIGN);
    checkExpression();
  }

  private void checkExpression() {
    checkSummand();
    if ((stream.pickNext()
      & (LangStruct.OPERATOR.PLUS
      | LangStruct.OPERATOR.MINUS)) > 0) {
      stream.next();
      checkExpression();
    }
  }

  private void checkSummand() {
    checkMultiplier();
    if ((stream.pickNext()
      & (LangStruct.OPERATOR.PRODUCT
      | LangStruct.OPERATOR.DIV)) > 0) {
      stream.next();
      checkSummand();
    }
  }

  private void checkMultiplier() {
    switch (stream.pickNext()) {
      case LangStruct.DIVIDER.BEG_CALL:
        stream.next();
        checkExpression();
        stream.validateNext(LangStruct.DIVIDER.END_CALL);
        break;
      case LangStruct.CUSTOM.ID:
        stream.next();
        stream.next();
        break;
      case LangStruct.CUSTOM.LITERAL:
        stream.next();
        stream.next();
        break;
      default: stream.validateNext(LangStruct.INTERNAL.UNKNOWN);
    }
  }

  private void checkCycle() {
    stream.validateNext(LangStruct.KEYWORD.FOR);
    checkAssign();
    stream.validateNext(LangStruct.KEYWORD.TO, LangStruct.KEYWORD.DOWNTO);
    checkExpression();
    stream.validateNext(LangStruct.KEYWORD.DO);
    if (stream.pickNext() == LangStruct.KEYWORD.BEGIN)
      checkOpList();
    else
      checkOperator();
  }



  private static class SyntaxStream {
    private final ArrayList<Integer> inputStream;
    private int pos, size, lineNum;

    SyntaxStream(ArrayList<Integer> input) {
      this.inputStream = input;
      this.size = input.size();
      this.lineNum = 1;
      this.pos = 0;
    }

    Integer pickNext() {
      if (!hasNext())
        throw new ProcessException("Unexpected end of file", lineNum);
      Integer next = inputStream.get(pos);
      if (next == LangStruct.INTERNAL.NEWLINE)
        next = inputStream.get(pos + 1);
      return next;
    }

    Integer next() {
      if (!hasNext())
        throw new ProcessException("Unexpected end of file", lineNum);
      Integer next = inputStream.get(pos++);
      if (next == LangStruct.INTERNAL.NEWLINE) {
        next = inputStream.get(pos++);
        lineNum++;
      }

      return next;
    }

    void validateNext(Integer... expected) {
      Integer nextSymbol = next();
      for (Integer possible : expected)
        if (nextSymbol.equals(possible)) return;

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
      throw new ProcessException(String.format("Unexpected %s", type), lineNum);
    }

    private boolean hasNext() {
      return pos < size;
    }
  }

  static class ProcessException extends IllegalStateException {
    private int lineNum;

    ProcessException(String msg, int lineNum) {
      super(msg);
      this.lineNum = lineNum;
    }

    int getLineNum() {
      return lineNum;
    }
  }
}
