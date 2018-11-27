package ru.didim99.tstu.core.translator;

import java.util.ArrayList;
import java.util.Collections;
import ru.didim99.tstu.core.translator.utils.SymbolTable;
import ru.didim99.tstu.utils.MyLog;

import static ru.didim99.tstu.core.translator.LangStruct.DictEntry;

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
      result.lexicalStream.add(InLang.INTERNAL.NEWLINE);
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
        if (Character.isLetter(line.charAt(pos)))
          throw new ProcessException("Unexpected character: " + first, lineNum);
        if (!Character.isDigit(line.charAt(pos)))
          break;
      }
      String literal = line.substring(0, pos);
      lexemLine.add(InLang.CUSTOM.LITERAL);
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
    lexemLine.add(InLang.CUSTOM.ID);
    lexemLine.add(symbolTable.add(varName));
    return line.substring(length);
  }

  private boolean isValidId(String str) {
    return str.matches("^[a-z_][a-z0-9_]*$");
  }

  private void initStatic() {
    symbolSet = new ArrayList<>();
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.KEYWORD.PROGRAM, InLang.KEYWORD.PROGRAM));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.KEYWORD.VAR, InLang.KEYWORD.VAR));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.KEYWORD.INTEGER, InLang.KEYWORD.INTEGER));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.KEYWORD.REAL, InLang.KEYWORD.REAL));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.KEYWORD.BEGIN, InLang.KEYWORD.BEGIN));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.KEYWORD.END, InLang.KEYWORD.END));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.KEYWORD.FOR, InLang.KEYWORD.FOR));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.KEYWORD.DOWNTO, InLang.KEYWORD.DOWNTO));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.KEYWORD.DO, InLang.KEYWORD.DO));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.KEYWORD.TO, InLang.KEYWORD.TO));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.KEYWORD.READ, InLang.KEYWORD.READ));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.KEYWORD.WRITELN, InLang.KEYWORD.WRITELN));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.KEYWORD.WRITE, InLang.KEYWORD.WRITE));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.OPERATOR.ASSIGN, InLang.OPERATOR.ASSIGN));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.OPERATOR.PLUS, InLang.OPERATOR.PLUS));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.OPERATOR.MINUS, InLang.OPERATOR.MINUS));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.OPERATOR.PRODUCT, InLang.OPERATOR.PRODUCT));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.OPERATOR.DIV, InLang.OPERATOR.DIV));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.OPERATOR.EQUAL, InLang.OPERATOR.EQUAL));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.OPERATOR.LT, InLang.OPERATOR.LT));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.OPERATOR.GT, InLang.OPERATOR.GT));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.DIVIDER.END_OP, InLang.DIVIDER.END_OP));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.DIVIDER.SEP_VL, InLang.DIVIDER.SEP_VL));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.DIVIDER.END_VL, InLang.DIVIDER.END_VL));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.DIVIDER.BEG_CALL, InLang.DIVIDER.BEG_CALL));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.DIVIDER.END_CALL, InLang.DIVIDER.END_CALL));
    symbolSet.add(new DictEntry(
      InLang.MNEMONIC.DIVIDER.END_PROG, InLang.DIVIDER.END_PROG));

    sortedSymbolSet = new ArrayList<>(symbolSet.size());
    for (DictEntry symbol : symbolSet)
      sortedSymbolSet.add(new DictEntry(symbol));
    sortedSymbolSet.add(new DictEntry(
      InLang.MNEMONIC.CUSTOM.ID, InLang.CUSTOM.ID));
    sortedSymbolSet.add(new DictEntry(
      InLang.MNEMONIC.CUSTOM.LITERAL, InLang.CUSTOM.LITERAL));
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
