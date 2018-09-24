package ru.didim99.tstu.core.translator;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by didim99 on 24.09.18.
 */
class SyntaxAnalyzer {
  private SyntaxStream stream;

  SyntaxAnalyzer(ArrayList<String> input) {
    this.stream = new SyntaxStream(input);
  }

  void analyze() {
    checkProgram();
  }

  private static class SyntaxStream {
    private Iterator<Integer> input;
    private int lineNum;

    SyntaxStream(ArrayList<String> input) {
      ArrayList<Integer> stream = new ArrayList<>();
      for (String line : input) {
        String[] lexemLine = line.split("\\s");
        for (String lexem : lexemLine)
          stream.add(Integer.parseInt(lexem));
        stream.add(LangStruct.NEWLINE);
      }

      this.input = stream.iterator();
      this.lineNum = 1;
    }

    boolean hasNext() {
      return input.hasNext();
    }

    Integer next() {
      Integer next = input.next();
      if (next == LangStruct.NEWLINE) {
        next = input.next();
        lineNum++;
      }

      return next;
    }

    int getLineNum() {
      return lineNum;
    }
  }

  /* ======== SYNTAX IMPLEMENTATION ========== */

  private void checkProgram() {
    checkHeader();
    checkSectionVar();
    checkSectionOp();
  }

  private void checkHeader() {
    checkProgramName();
  }

  private void checkProgramName() {

  }

  private void checkSectionVar() {
    checkVarList();
    checkType();
  }

  private void checkVarList() {

  }

  private void checkType() {

  }

  private void checkSectionOp() {
    checkOpList();
  }

  private void checkOpList() {
    checkOperator();
  }

  private void checkOperator() {
    checkAssign();
    checkInput();
    checkOutput();
    checkCycle();
  }

  private void checkInput() {
    checkVarList();
  }

  private void checkOutput() {
    checkVarList();
  }

  private void checkAssign() {
    checkExpression();
  }

  private void checkExpression() {
    checkSummand();
    checkSummand();
  }

  private void checkSummand() {
    checkMultiplier();
    checkMultiplier();
  }

  private void checkMultiplier() {
    checkExpression();
  }

  private void checkCycle() {
    checkAssign();
    checkVarList();
    checkOpList();
  }
}
