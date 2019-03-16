package ru.didim99.tstu.core.translator;

import java.util.ArrayList;
import ru.didim99.tstu.core.translator.utils.SymbolTable;
import ru.didim99.tstu.core.translator.LangMap.*;
import ru.didim99.tstu.core.translator.AST.*;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 16.03.19.
 */
class FortranGenerator implements Translator.CodeGenerator {
  private Program program;
  private Builder builder;

  @Override
  public String generate(SyntaxAnalyzer.Result src) {
    builder = new Builder(src.getSymbolTable());
    program = src.getProgram();
    generateProgram();
    return builder.toString();
  }

  private void generateProgram() {
    builder.startLine()
      .appendToken(KEYWORD.PROGRAM).sep()
      .appendSymbol(program.getName()).newLine();
    generateVarDefs();
    generateOpList(program.getOperations());
    builder.startLine().appendToken(KEYWORD.END);
  }

  private void generateVarDefs() {
    for (VarDef vd : program.getVars()) {
      builder.startLine().appendToken(vd.getType())
        .sep().appendSymbolList(vd.getVars()).newLine();
    }
  }

  private void generateOpList(OpList operations) {
    builder.levelUp();
    for (Operation op : operations.getList()) {
      builder.startLine();
      if (op instanceof Assignment) generateAssignment(op);
      else if (op instanceof Output) generateOutput(op);
      else if (op instanceof Input) generateInput(op);
      else if (op instanceof Cycle) generateCycle(op);
      builder.newLine();
    }
    builder.levelDown();
  }

  private void generateAssignment(Operation op) {
    Assignment statement = (Assignment) op;
    builder.appendSymbol(statement.getTarget())
      .sep().appendToken(OPERATOR.ASSIGN).sep();
    generateExpression(statement.getExpr());
  }

  private void generateOutput(Operation op) {
    Output statement = (Output) op;
    builder.appendToken(KEYWORD.OUT).sep()
      .appendToken(FORMAT.ANY).appendToken(DIVIDER.COMMA)
      .sep().appendSymbolList(statement.getVars());
  }

  private void generateInput(Operation op) {
    Input statement = (Input) op;
    builder.appendToken(KEYWORD.IN).sep()
      .appendToken(FORMAT.ANY).appendToken(DIVIDER.COMMA)
      .sep().appendSymbolList(statement.getVars());
  }

  private void generateCycle(Operation op) {
    Cycle statement = (Cycle) op;
    boolean up = statement.getType() == KEYWORD.TO;
    builder.appendToken(KEYWORD.CYCLE).sep();
    generateAssignment(statement.getStart());
    builder.appendToken(DIVIDER.COMMA).sep();
    generateExpression(statement.getEnd());
    builder.appendToken(DIVIDER.COMMA).sep()
      .appendLiteral(new Literal(up ? 1 : -1)).newLine();
    generateOpList(statement.getOperations());
    builder.startLine().appendToken(KEYWORD.END)
      .sep().appendToken(KEYWORD.CYCLE);
  }

  private void generateExpression(Expression expr) {
    generateSummand(expr.getLeft());
    if (expr.getRight() == null) return;
    builder.appendToken(expr.getOperation());
    generateExpression(expr.getRight());
  }

  private void generateSummand(Summand summand) {
    generateMultiplier(summand.getLeft());
    if (summand.getRight() == null) return;
    builder.appendToken(summand.getOperation());
    generateSummand(summand.getRight());
  }

  private void generateMultiplier(Multiplier expr) {
    if (expr instanceof Expression)
      generateExpression((Expression) expr);
    else if (expr instanceof Identifier)
      builder.appendSymbol((Identifier) expr);
    else if (expr instanceof Literal)
      builder.appendLiteral((Literal) expr);
  }

  private static class Builder {
    private static final String LINE_START = "      ";
    private static final String NEW_LINE = "\n";
    private static final String INDENT = "  ";
    private static final String SEP = " ";

    private StringBuilder sb;
    private SymbolTable symbolTable;
    private LangStruct ls;
    private int level;

    Builder(SymbolTable symbolTable) {
      this.symbolTable = symbolTable;
      ls = LangStruct.getInstance();
      sb = new StringBuilder();
      level = 0;
    }

    void levelUp() {
      level++;
    }

    void levelDown() {
      level--;
    }

    void newLine() {
      append(NEW_LINE);
    }

    Builder startLine() {
      append(LINE_START);
      int myLevel = level;
      while (myLevel-- > 0)
        append(INDENT);
      return this;
    }

    Builder sep() {
      return append(SEP);
    }

    Builder appendToken(int id) {
      return append(getToken(id));
    }

    Builder appendSymbol(Identifier id) {
      return append(getSymbol(id));
    }

    Builder appendSymbolList(VarList vars) {
      ArrayList<Identifier> list = vars.getList();
      ArrayList<String> out = new ArrayList<>(list.size());
      for (Identifier id : list) out.add(getSymbol(id));
      String sep = getToken(DIVIDER.COMMA).concat(SEP);
      return append(Utils.joinStr(sep, out));
    }

    Builder appendLiteral(Literal literal) {
      return append(Integer.toString(literal.getValue()));
    }

    Builder append(String str) {
      sb.append(str);
      return this;
    }

    private String getToken(int index) {
      return ls.getOutMnemonic(index);
    }

    private String getSymbol(Identifier id) {
      return symbolTable.get(id.getIndex()).getName();
    }

    @Override
    public String toString() {
      return sb.toString();
    }
  }
}
