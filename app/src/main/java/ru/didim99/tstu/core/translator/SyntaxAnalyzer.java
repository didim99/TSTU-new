package ru.didim99.tstu.core.translator;

import android.util.SparseArray;
import java.util.ArrayList;

/**
 * Created by didim99 on 24.09.18.
 */
abstract class SyntaxAnalyzer {

  ArrayList<String> rawSymbolTable;
  Result result;

  SyntaxAnalyzer(LexicalAnalyzer.Result input) {
    this.rawSymbolTable = input.getSymbolTable();
    this.result = new Result();
  }

  abstract Result analyze();

  static class Result {
    private SparseArray<Symbol> symbolTable;
    private AST.Program program;

    Result() {
      this.symbolTable = new SparseArray<>();
    }

    SparseArray<Symbol> getSymbolTable() {
      return symbolTable;
    }

    void setProgram(AST.Program program) {
      this.program = program;
    }
  }

  static class Symbol {
    private String name;
    private int type;

    Symbol(String name, int type) {
      this.name = name;
      this.type = type;
    }

    public String getName() {
      return name;
    }

    public int getType() {
      return type;
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
