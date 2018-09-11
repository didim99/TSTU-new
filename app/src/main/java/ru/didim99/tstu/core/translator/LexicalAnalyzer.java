package ru.didim99.tstu.core.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 06.09.18.
 */
public class LexicalAnalyzer {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_LA";

  private static Map<String, Integer> symbolMap;
  private static boolean initCompleted = false;

  private boolean forTesting = false;
  private ArrayList<String> symbolTable;

  LexicalAnalyzer() {
    if (!initCompleted)
      initStatic();
    symbolTable = new ArrayList<>();
  }

  LexicalAnalyzer(boolean forTesting) {
    super();
    this.forTesting = forTesting;
  }

  ArrayList<String> analyze(ArrayList<String> srcList)
    throws ProcessException {
    ArrayList<String> outList = new ArrayList<>(srcList.size());
    ArrayList<Integer> lexemLine = new ArrayList<>();

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

      //TESTING ONLY
      StringBuilder sb = new StringBuilder();
      for (Integer lexem : lexemLine)
        sb.append(lexem).append(" ");
      outList.add(sb.toString().trim());
      lexemLine.clear();
    }

    if (forTesting) {
      outList.add("\nVAR TABLE:");
      for (int i = 0; i < symbolTable.size(); i++)
        outList.add(String.format(Locale.US, "\t%2d: %s", i, symbolTable.get(i)));
    }

    return outList;
  }

  private String nextStatement(String line, ArrayList<Integer> lexemLine)
    throws ProcessException {
    for (String symbol : LangStruct.SYMBOL_SEARCH_ORDER) {
      if (line.equals(symbol)) {
        lexemLine.add(symbolMap.get(symbol));
        return null;
      } else if (line.startsWith(symbol)) {
        if (!isValidId(line.substring(0, symbol.length() + 1))) {
          lexemLine.add(symbolMap.get(symbol));
          return line.substring(symbol.length());
        } else {
          return findId(line, symbol.length(), lexemLine);
        }
      }
    }

    char first = line.charAt(0);
    if (Character.isDigit(first)) {
      int pos = 0;
      while (++pos < line.length()) {
        if (Character.isLetter(line.charAt(pos)))
          throw new ProcessException("Unexpected character: " + first);
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

    throw new ProcessException("Unexpected character: " + first);
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
    symbolMap = new HashMap<>(LangStruct.SYMBOL_SEARCH_ORDER.length);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.PROGRAM, LangStruct.KEYWORD.PROGRAM);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.VAR, LangStruct.KEYWORD.VAR);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.INTEGER, LangStruct.KEYWORD.INTEGER);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.REAL, LangStruct.KEYWORD.REAL);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.BEGIN, LangStruct.KEYWORD.BEGIN);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.END, LangStruct.KEYWORD.END);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.FOR, LangStruct.KEYWORD.FOR);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.TO, LangStruct.KEYWORD.TO);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.DOWNTO, LangStruct.KEYWORD.DOWNTO);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.DO, LangStruct.KEYWORD.DO);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.READ, LangStruct.KEYWORD.READ);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.WRITE, LangStruct.KEYWORD.WRITE);
    symbolMap.put(LangStruct.MNEMONIC.KEYWORD.WRITELN, LangStruct.KEYWORD.WRITELN);
    symbolMap.put(LangStruct.MNEMONIC.OPERATOR.ASSIGN, LangStruct.OPERATOR.ASSIGN);
    symbolMap.put(LangStruct.MNEMONIC.OPERATOR.PLUS, LangStruct.OPERATOR.PLUS);
    symbolMap.put(LangStruct.MNEMONIC.OPERATOR.MINUS, LangStruct.OPERATOR.MINUS);
    symbolMap.put(LangStruct.MNEMONIC.OPERATOR.PRODUCT, LangStruct.OPERATOR.PRODUCT);
    symbolMap.put(LangStruct.MNEMONIC.OPERATOR.DIV, LangStruct.OPERATOR.DIV);
    symbolMap.put(LangStruct.MNEMONIC.OPERATOR.EQUAL, LangStruct.OPERATOR.EQUAL);
    symbolMap.put(LangStruct.MNEMONIC.OPERATOR.LT, LangStruct.OPERATOR.LT);
    symbolMap.put(LangStruct.MNEMONIC.OPERATOR.GT, LangStruct.OPERATOR.GT);
    symbolMap.put(LangStruct.MNEMONIC.DIVIDER.END_OP, LangStruct.DIVIDER.END_OP);
    symbolMap.put(LangStruct.MNEMONIC.DIVIDER.SEP_VL, LangStruct.DIVIDER.SEP_VL);
    symbolMap.put(LangStruct.MNEMONIC.DIVIDER.END_VL, LangStruct.DIVIDER.END_VL);
    symbolMap.put(LangStruct.MNEMONIC.DIVIDER.BEG_CALL, LangStruct.DIVIDER.BEG_CALL);
    symbolMap.put(LangStruct.MNEMONIC.DIVIDER.END_CALL, LangStruct.DIVIDER.END_CALL);
    symbolMap.put(LangStruct.MNEMONIC.DIVIDER.END_PROG, LangStruct.DIVIDER.END_PROG);
    initCompleted = true;
  }

  static class ProcessException extends Exception {
    ProcessException(String msg) { super(msg); }
  }
}
