package ru.didim99.tstu.core.translator;

import java.util.ArrayList;

/**
 * Created by didim99 on 24.09.18.
 */
class SyntaxAnalyzer {
  private SyntaxStream inputStream;

  SyntaxAnalyzer(ArrayList<Integer> input) {
    this.inputStream = new SyntaxStream(input);
  }

  void analyze() {
    checkProgram();
  }

  /* ======== SYNTAX IMPLEMENTATION ========== */

  private void checkProgram() {
    checkHeader();
    inputStream.validateNext(LangStruct.DIVIDER.END_OP);
    checkSectionVar();
    checkSectionOp();
    inputStream.validateNext(LangStruct.DIVIDER.END_PROG);
  }

  private void checkHeader() {
    inputStream.validateNext(LangStruct.KEYWORD.PROGRAM);
    checkVarName();
  }

  private void checkVarName() {
    inputStream.validateNext(LangStruct.CUSTOM.ID);
    inputStream.next();
  }

  private void checkSectionVar() {
    inputStream.validateNext(LangStruct.KEYWORD.VAR);
    checkVarDeclaration();
  }

  private void checkVarDeclaration() {
    checkVarList();
    inputStream.validateNext(LangStruct.DIVIDER.END_VL);
    checkType();
    inputStream.validateNext(LangStruct.DIVIDER.END_OP);
    if (inputStream.pickNext() == LangStruct.CUSTOM.ID) {
      checkVarDeclaration();
    }
  }

  private void checkVarList() {
    checkVarName();
    if (inputStream.pickNext() == LangStruct.DIVIDER.SEP_VL) {
      inputStream.next();
      checkVarList();
    }
  }

  private void checkType() {
    inputStream.validateNext(LangStruct.KEYWORD.INTEGER, LangStruct.KEYWORD.REAL);
  }

  private void checkSectionOp() {
    inputStream.validateNext(LangStruct.KEYWORD.BEGIN);
    checkOpList();
    inputStream.validateNext(LangStruct.KEYWORD.END);
  }

  private void checkOpList() {
    checkOperator();
    if (inputStream.pickNext() == LangStruct.DIVIDER.END_OP) {
      inputStream.next();
      checkOpList();
    }
  }

  private void checkOperator() {
    switch (inputStream.pickNext()) {
      case LangStruct.KEYWORD.READ:   checkInput(); break;
      case LangStruct.KEYWORD.WRITELN:
      case LangStruct.KEYWORD.WRITE:  checkOutput(); break;
      case LangStruct.KEYWORD.FOR:    checkCycle(); break;
      case LangStruct.CUSTOM.ID:      checkAssign(); break;
      default: inputStream.validateNext(LangStruct.INTERNAL.UNKNOWN);
    }
  }

  private void checkInput() {
    inputStream.validateNext(LangStruct.KEYWORD.READ);
    inputStream.validateNext(LangStruct.DIVIDER.BEG_CALL);
    checkVarList();
    inputStream.validateNext(LangStruct.DIVIDER.END_CALL);
  }

  private void checkOutput() {
    inputStream.validateNext(LangStruct.KEYWORD.WRITE, LangStruct.KEYWORD.WRITELN);
    inputStream.validateNext(LangStruct.DIVIDER.BEG_CALL);
    checkVarList();
    inputStream.validateNext(LangStruct.DIVIDER.END_CALL);
  }

  private void checkAssign() {
    checkVarName();
    inputStream.validateNext(LangStruct.OPERATOR.ASSIGN);
    checkExpression();
  }

  private void checkExpression() {
    checkSummand();
    if ((inputStream.pickNext()
      & (LangStruct.OPERATOR.PLUS
      | LangStruct.OPERATOR.MINUS)) > 0) {
      inputStream.next();
      checkExpression();
    }
  }

  private void checkSummand() {
    checkMultiplier();
    if ((inputStream.pickNext()
      & (LangStruct.OPERATOR.PRODUCT
      | LangStruct.OPERATOR.DIV)) > 0) {
      inputStream.next();
      checkSummand();
    }
  }

  private void checkMultiplier() {
    switch (inputStream.pickNext()) {
      case LangStruct.DIVIDER.BEG_CALL:
        inputStream.next();
        checkExpression();
        inputStream.validateNext(LangStruct.DIVIDER.END_CALL);
        break;
      case LangStruct.CUSTOM.ID:
        checkVarName();
        break;
      case LangStruct.CUSTOM.LITERAL:
        inputStream.next();
        inputStream.next();
        break;
      default: inputStream.validateNext(LangStruct.INTERNAL.UNKNOWN);
    }
  }

  private void checkCycle() {
    inputStream.validateNext(LangStruct.KEYWORD.FOR);
    checkAssign();
    inputStream.validateNext(LangStruct.KEYWORD.TO, LangStruct.KEYWORD.DOWNTO);
    checkExpression();
    inputStream.validateNext(LangStruct.KEYWORD.DO);
    if (inputStream.pickNext() == LangStruct.KEYWORD.BEGIN)
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

    int getLineNum() { return lineNum; }
  }
}
