package ru.didim99.tstu.core.translator;

import ru.didim99.tstu.core.translator.utils.SymbolTable;

/**
 * Created by didim99 on 24.09.18.
 */
public abstract class SyntaxAnalyzer {
  Result result;

  SyntaxAnalyzer(LexicalAnalyzer.Result input) {
    this.result = new Result(input.getSymbolTable());
  }

  abstract Result analyze();

  static class Result {
    private SymbolTable symbolTable;
    private AST.Program program;

    public Result(SymbolTable symbolTable) {
      this.symbolTable = symbolTable;
    }

    SymbolTable getSymbolTable() {
      return symbolTable;
    }

    public AST.Program getProgram() {
      return program;
    }

    void setProgram(AST.Program program) {
      this.program = program;
    }
  }

  public static class ProcessException extends IllegalStateException {
    private int lineNum;

    public ProcessException(String msg, int lineNum) {
      super(msg);
      this.lineNum = lineNum;
    }

    int getLineNum() { return lineNum; }
  }
}
