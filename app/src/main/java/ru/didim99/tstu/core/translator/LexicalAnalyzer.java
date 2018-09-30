package ru.didim99.tstu.core.translator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import ru.didim99.tstu.utils.MyLog;

import static ru.didim99.tstu.core.translator.LangStruct.DictEntry;

/**
 * Created by didim99 on 06.09.18.
 */
class LexicalAnalyzer {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_LA";

  private static ArrayList<LangStruct.DictEntry> symbolSet;
  private static ArrayList<LangStruct.DictEntry> sortedSymbolSet;
  private static boolean initCompleted = false;

  private ArrayList<String> symbolTable;
  private int lineNum;

  LexicalAnalyzer() {
    if (!initCompleted) initStatic();
    symbolTable = new ArrayList<>();
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
      result.lexicalStream.add(LangStruct.INTERNAL.NEWLINE);
      lexemLine.clear();
      lineNum++;
    }

    if (forTesting) {
      result.lines.add("\nVAR TABLE:");
      for (int i = 0; i < symbolTable.size(); i++) {
        result.lines.add(String.format(Locale.US,
          "\t%2d: %s", i, symbolTable.get(i)));
      }
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
      lexemLine.add(LangStruct.CUSTOM.LITERAL);
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
    if (!symbolTable.contains(varName))
      symbolTable.add(varName);
    lexemLine.add(LangStruct.CUSTOM.ID);
    lexemLine.add(symbolTable.indexOf(varName));
    return line.substring(length);
  }

  private boolean isValidId(String str) {
    return str.matches("^[a-z_][a-z0-9_]*$");
  }

  private void initStatic() {
    symbolSet = new ArrayList<>();
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.KEYWORD.PROGRAM, LangStruct.KEYWORD.PROGRAM));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.KEYWORD.VAR, LangStruct.KEYWORD.VAR));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.KEYWORD.INTEGER, LangStruct.KEYWORD.INTEGER));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.KEYWORD.REAL, LangStruct.KEYWORD.REAL));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.KEYWORD.BEGIN, LangStruct.KEYWORD.BEGIN));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.KEYWORD.END, LangStruct.KEYWORD.END));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.KEYWORD.FOR, LangStruct.KEYWORD.FOR));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.KEYWORD.DOWNTO, LangStruct.KEYWORD.DOWNTO));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.KEYWORD.DO, LangStruct.KEYWORD.DO));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.KEYWORD.TO, LangStruct.KEYWORD.TO));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.KEYWORD.READ, LangStruct.KEYWORD.READ));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.KEYWORD.WRITELN, LangStruct.KEYWORD.WRITELN));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.KEYWORD.WRITE, LangStruct.KEYWORD.WRITE));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.OPERATOR.ASSIGN, LangStruct.OPERATOR.ASSIGN));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.OPERATOR.PLUS, LangStruct.OPERATOR.PLUS));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.OPERATOR.MINUS, LangStruct.OPERATOR.MINUS));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.OPERATOR.PRODUCT, LangStruct.OPERATOR.PRODUCT));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.OPERATOR.DIV, LangStruct.OPERATOR.DIV));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.OPERATOR.EQUAL, LangStruct.OPERATOR.EQUAL));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.OPERATOR.LT, LangStruct.OPERATOR.LT));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.OPERATOR.GT, LangStruct.OPERATOR.GT));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.DIVIDER.END_OP, LangStruct.DIVIDER.END_OP));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.DIVIDER.SEP_VL, LangStruct.DIVIDER.SEP_VL));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.DIVIDER.END_VL, LangStruct.DIVIDER.END_VL));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.DIVIDER.BEG_CALL, LangStruct.DIVIDER.BEG_CALL));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.DIVIDER.END_CALL, LangStruct.DIVIDER.END_CALL));
    symbolSet.add(new DictEntry(
      LangStruct.MNEMONIC.DIVIDER.END_PROG, LangStruct.DIVIDER.END_PROG));

    sortedSymbolSet = new ArrayList<>(symbolSet.size());
    for (DictEntry symbol : symbolSet)
      sortedSymbolSet.add(new DictEntry(symbol));
    sortedSymbolSet.add(new DictEntry("ID", LangStruct.CUSTOM.ID));
    sortedSymbolSet.add(new DictEntry("LITERAL", LangStruct.CUSTOM.LITERAL));
    Collections.sort(sortedSymbolSet, (s1, s2) ->
      Integer.compare(s1.getValue(), s2.getValue()));

    initCompleted = true;
  }

  ArrayList<LangStruct.DictEntry> getSortedSymbolSet() {
    return sortedSymbolSet;
  }

  static class Result {
    private ArrayList<String> lines;
    private ArrayList<Integer> lexicalStream;
    private ArrayList<String> symbolTable;

    ArrayList<String> getLines() { return lines; }
    ArrayList<Integer> getLexicalStream() { return lexicalStream; }
    ArrayList<String> getSymbolTable() { return symbolTable; }
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
