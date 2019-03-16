package ru.didim99.tstu.core.translator;

import java.util.ArrayList;
import java.util.Collections;
import ru.didim99.tstu.core.translator.LangMap.*;
import ru.didim99.tstu.core.translator.LangStruct.DictEntry;
import ru.didim99.tstu.core.translator.utils.SymbolTable;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 06.09.18.
 */
class LexicalAnalyzer {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_LA";

  private static ArrayList<DictEntry> symbolSet;
  private static ArrayList<DictEntry> sortedSymbolSet;
  private static boolean initCompleted = false;

  private SymbolTable symbolTable;
  private int lineNum;

  LexicalAnalyzer() {
    if (!initCompleted) initStatic();
    symbolTable = new SymbolTable();
    lineNum = 1;
  }

  Result analyze(ArrayList<String> srcList, boolean forTesting) {
    Result result = new Result();
    result.lexicalStream = new ArrayList<>();
    ArrayList<Integer> lexemLine = new ArrayList<>();
    if (forTesting)
      result.lines = new ArrayList<>(srcList.size());

    for (String line : srcList) {
      line = line.toLowerCase();
      String[] subLines = line.split("\\s");
      for (String subLine : subLines) {
        MyLog.v(LOG_TAG, "before: " + subLine);
        while (subLine != null && !subLine.isEmpty()) {
          subLine = nextStatement(subLine, lexemLine);
          MyLog.v(LOG_TAG, "after: " + subLine);
        }
      }

      if(forTesting) {
        StringBuilder sb = new StringBuilder();
        for (Integer lexem : lexemLine)
          sb.append(lexem).append(" ");
        result.lines.add(sb.toString().trim());
      }

      result.lexicalStream.addAll(lexemLine);
      result.lexicalStream.add(INTERNAL.NEWLINE);
      lexemLine.clear();
      lineNum++;
    }

    if (forTesting) {
      result.lines.add("\nVAR TABLE:");
      result.lines.add(symbolTable.symbolList());
    }

    result.symbolTable = symbolTable;
    return result;
  }

  private String nextStatement(String line, ArrayList<Integer> lexemLine) {
    for (DictEntry symbol : symbolSet) {
      String mnemonic = symbol.getMnemonic();
      if (line.equals(mnemonic)) {
        lexemLine.add(symbol.getValue());
        return null;
      } else if (line.startsWith(mnemonic)) {
        if (!isValidId(line.substring(0, mnemonic.length() + 1))) {
          lexemLine.add(symbol.getValue());
          return line.substring(mnemonic.length());
        } else {
          return findId(line, mnemonic.length(), lexemLine);
        }
      }
    }

    char first = line.charAt(0);
    if (Character.isDigit(first)) {
      int pos = 0;
      while (++pos < line.length()) {
        char curr = line.charAt(pos);
        if (Character.isLetter(curr))
          throw new ProcessException("Unexpected character: " + curr, lineNum);
        if (!Character.isDigit(curr))
          break;
      }
      String literal = line.substring(0, pos);
      lexemLine.add(CUSTOM.LITERAL);
      lexemLine.add(Integer.parseInt(literal));
      return line.substring(pos);
    }

    if (isValidId(String.valueOf(first)))
      return findId(line, 1, lexemLine);

    throw new ProcessException("Unexpected character: " + first, lineNum);
  }

  private String findId(String line, int length, ArrayList<Integer> lexemLine) {
    if (length == line.length())
      length++;
    while (length < line.length())
      if (!isValidId(line.substring(0, ++length))) break;
    String varName = line.substring(0, --length);
    //MyLog.e(LOG_TAG, "\'" + line + "\' \'" + varName + "\'");
    lexemLine.add(CUSTOM.ID);
    lexemLine.add(symbolTable.add(varName));
    return line.substring(length);
  }

  private boolean isValidId(String str) {
    return str.matches("^[a-z_][a-z0-9_]*$");
  }

  private void initStatic() {
    symbolSet = new ArrayList<>();
    symbolSet.add(new DictEntry(PASCAL.KEYWORD.PROGRAM, KEYWORD.PROGRAM));
    symbolSet.add(new DictEntry(PASCAL.KEYWORD.VAR, KEYWORD.VAR));
    symbolSet.add(new DictEntry(PASCAL.KEYWORD.INTEGER, KEYWORD.INTEGER));
    symbolSet.add(new DictEntry(PASCAL.KEYWORD.REAL, KEYWORD.REAL));
    symbolSet.add(new DictEntry(PASCAL.KEYWORD.BEGIN, KEYWORD.START));
    symbolSet.add(new DictEntry(PASCAL.KEYWORD.END, KEYWORD.END));
    symbolSet.add(new DictEntry(PASCAL.KEYWORD.FOR, KEYWORD.CYCLE));
    symbolSet.add(new DictEntry(PASCAL.KEYWORD.DOWNTO, KEYWORD.DOWNTO));
    symbolSet.add(new DictEntry(PASCAL.KEYWORD.DO, KEYWORD.DO));
    symbolSet.add(new DictEntry(PASCAL.KEYWORD.TO, KEYWORD.TO));
    symbolSet.add(new DictEntry(PASCAL.KEYWORD.READ, KEYWORD.IN));
    symbolSet.add(new DictEntry(PASCAL.KEYWORD.WRITELN, KEYWORD.OUTLINE));
    symbolSet.add(new DictEntry(PASCAL.KEYWORD.WRITE, KEYWORD.OUT));
    symbolSet.add(new DictEntry(PASCAL.OPERATOR.ASSIGN, OPERATOR.ASSIGN));
    symbolSet.add(new DictEntry(PASCAL.OPERATOR.PLUS, OPERATOR.PLUS));
    symbolSet.add(new DictEntry(PASCAL.OPERATOR.MINUS, OPERATOR.MINUS));
    symbolSet.add(new DictEntry(PASCAL.OPERATOR.PRODUCT, OPERATOR.PRODUCT));
    symbolSet.add(new DictEntry(PASCAL.OPERATOR.DIV, OPERATOR.DIV));
    symbolSet.add(new DictEntry(PASCAL.OPERATOR.EQUAL, OPERATOR.EQUAL));
    symbolSet.add(new DictEntry(PASCAL.OPERATOR.LT, OPERATOR.LT));
    symbolSet.add(new DictEntry(PASCAL.OPERATOR.GT, OPERATOR.GT));
    symbolSet.add(new DictEntry(PASCAL.DIVIDER.END_OP, DIVIDER.SEMICOLON));
    symbolSet.add(new DictEntry(PASCAL.DIVIDER.SEP_VL, DIVIDER.COMMA));
    symbolSet.add(new DictEntry(PASCAL.DIVIDER.END_VL, DIVIDER.COLON));
    symbolSet.add(new DictEntry(PASCAL.DIVIDER.BEG_CALL, DIVIDER.LBR));
    symbolSet.add(new DictEntry(PASCAL.DIVIDER.END_CALL, DIVIDER.RBR));
    symbolSet.add(new DictEntry(PASCAL.DIVIDER.END_PROG, DIVIDER.DOT));

    sortedSymbolSet = new ArrayList<>(symbolSet.size());
    for (DictEntry symbol : symbolSet)
      sortedSymbolSet.add(new DictEntry(symbol));
    sortedSymbolSet.add(new DictEntry(PASCAL.CUSTOM.ID, CUSTOM.ID));
    sortedSymbolSet.add(new DictEntry(PASCAL.CUSTOM.LITERAL, CUSTOM.LITERAL));
    Collections.sort(sortedSymbolSet, (s1, s2) ->
      Integer.compare(s1.getValue(), s2.getValue()));

    initCompleted = true;
  }

  ArrayList<DictEntry> getSortedSymbolSet() {
    return sortedSymbolSet;
  }

  static class Result {
    private ArrayList<String> lines;
    private ArrayList<Integer> lexicalStream;
    private SymbolTable symbolTable;

    ArrayList<String> getLines() { return lines; }
    ArrayList<Integer> getLexicalStream() { return lexicalStream; }
    SymbolTable getSymbolTable() { return symbolTable; }
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
