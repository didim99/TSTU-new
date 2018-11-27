package ru.didim99.tstu.core.translator;

import java.util.ArrayList;
import ru.didim99.tstu.core.translator.utils.SyntaxStream;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 01.11.18.
 */
class DescentAnalyzer extends SyntaxAnalyzer {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_DSA";

  private SyntaxStream inputStream;

  DescentAnalyzer(LexicalAnalyzer.Result input) {
    super(input);
    this.inputStream = new SyntaxStream(input.getLexicalStream());
  }

  @Override
  Result analyze() {
    result.setProgram(checkProgram());
    return result;
  }

  /* ======== SYNTAX IMPLEMENTATION ========== */

  private AST.Program checkProgram() {
    AST.Identifier name = checkHeader();
    inputStream.validateNext(InLang.DIVIDER.END_OP);
    ArrayList<AST.VarDef> vars = checkSectionVar();
    AST.OpList operations = checkSectionOp();
    inputStream.validateNext(InLang.DIVIDER.END_PROG);
    MyLog.v(LOG_TAG, "parsed element: PROGRAM");
    return new AST.Program(name, vars, operations);
  }

  private AST.Identifier checkHeader() {
    inputStream.validateNext(InLang.KEYWORD.PROGRAM);
    AST.Identifier name = checkVarName();
    MyLog.v(LOG_TAG, "parsed element: HEADER");
    return name;
  }

  private AST.Identifier checkVarName() {
    inputStream.validateNext(InLang.CUSTOM.ID);
    MyLog.v(LOG_TAG, "parsed element: IDENTIFIER");
    return new AST.Identifier(inputStream.next());
  }

  private ArrayList<AST.VarDef> checkSectionVar() {
    inputStream.validateNext(InLang.KEYWORD.VAR);
    ArrayList<AST.VarDef> varDef = checkVarDeclaration();
    MyLog.v(LOG_TAG, "parsed element: VAR SECTION");
    return varDef;
  }

  private ArrayList<AST.VarDef> checkVarDeclaration() {
    ArrayList<AST.VarDef> varDefs = new ArrayList<>();
    ArrayList<AST.Identifier> vars = checkVarList();
    inputStream.validateNext(InLang.DIVIDER.END_VL);
    int type = checkType();

    for (AST.Identifier var : vars)
      result.getSymbolTable().get(var.getIndex()).setType(type);
    varDefs.add(new AST.VarDef(vars, type));

    inputStream.validateNext(InLang.DIVIDER.END_OP);
    if (inputStream.pickNext() == InLang.CUSTOM.ID) {
      varDefs.addAll(checkVarDeclaration());
    }

    MyLog.v(LOG_TAG, "parsed element: VAR DECLARATION");
    return varDefs;
  }

  private ArrayList<AST.Identifier> checkVarList() {
    ArrayList<AST.Identifier> vars = new ArrayList<>();
    vars.add(checkVarName());

    if (inputStream.pickNext() == InLang.DIVIDER.SEP_VL) {
      inputStream.next();
      vars.addAll(checkVarList());
    }

    MyLog.v(LOG_TAG, "parsed element: VAR LIST");
    return vars;
  }

  private int checkType() {
    switch (inputStream.pickNext()) {
      case InLang.KEYWORD.INTEGER:
      case InLang.KEYWORD.REAL:
        MyLog.v(LOG_TAG, "parsed element: VAR TYPE");
        return inputStream.next();
      default:
        inputStream.validateNext(InLang.INTERNAL.UNKNOWN);
        return InLang.INTERNAL.UNKNOWN;
    }
  }

  private AST.OpList checkSectionOp() {
    inputStream.validateNext(InLang.KEYWORD.BEGIN);
    ArrayList<AST.Operation> operations = checkOpList();
    inputStream.validateNext(InLang.KEYWORD.END);
    MyLog.v(LOG_TAG, "parsed element: OPERATOR SECTION");
    return new AST.OpList(operations);
  }

  private ArrayList<AST.Operation> checkOpList() {
    ArrayList<AST.Operation> operations = new ArrayList<>();
    operations.add(checkOperator());
    if (inputStream.pickNext() == InLang.DIVIDER.END_OP) {
      inputStream.next();
      operations.addAll(checkOpList());
    }

    MyLog.v(LOG_TAG, "parsed element: OPERATOR LIST");
    return operations;
  }

  private AST.Operation checkOperator() {
    switch (inputStream.pickNext()) {
      case InLang.KEYWORD.READ:
        return checkInput();
      case InLang.KEYWORD.WRITELN:
      case InLang.KEYWORD.WRITE:
        return checkOutput();
      case InLang.KEYWORD.FOR:
        return checkCycle();
      case InLang.CUSTOM.ID:
        return checkAssign();
      default:
        inputStream.validateNext(InLang.INTERNAL.UNKNOWN);
        return null;
    }
  }

  private AST.Operation checkInput() {
    inputStream.validateNext(InLang.KEYWORD.READ);
    inputStream.validateNext(InLang.DIVIDER.BEG_CALL);
    ArrayList<AST.Identifier> vars = checkVarList();
    inputStream.validateNext(InLang.DIVIDER.END_CALL);
    MyLog.v(LOG_TAG, "parsed element: INPUT");
    return new AST.Input(vars);
  }

  private AST.Operation checkOutput() {
    int operator = inputStream.validateNext(
      InLang.KEYWORD.WRITE, InLang.KEYWORD.WRITELN);
    inputStream.validateNext(InLang.DIVIDER.BEG_CALL);
    ArrayList<AST.Identifier> vars = checkVarList();
    inputStream.validateNext(InLang.DIVIDER.END_CALL);
    MyLog.v(LOG_TAG, "parsed element: OUTPUT");
    return new AST.Output(operator, vars);
  }

  private AST.Assignment checkAssign() {
    AST.Identifier target = checkVarName();
    inputStream.validateNext(InLang.OPERATOR.ASSIGN);
    AST.Expression expr = checkExpression();
    MyLog.v(LOG_TAG, "parsed element: ASSIGNMENT");
    return new AST.Assignment(target, expr);
  }

  private AST.Expression checkExpression() {
    AST.Summand left = checkSummand();
    AST.Expression expr = null;
    int operation = 0;

    if ((inputStream.pickNext()
      & (InLang.OPERATOR.PLUS
      | InLang.OPERATOR.MINUS)) > 0) {
      operation = inputStream.next();
      expr = checkExpression();
    }

    MyLog.v(LOG_TAG, "parsed element: EXPRESSION");
    return new AST.Expression(left, expr, operation);
  }

  private AST.Summand checkSummand() {
    AST.Multiplier left = checkMultiplier();
    AST.Summand sum = null;
    int operation = 0;

    if ((inputStream.pickNext()
      & (InLang.OPERATOR.PRODUCT
      | InLang.OPERATOR.DIV)) > 0) {
      operation = inputStream.next();
      sum = checkSummand();
    }

    MyLog.v(LOG_TAG, "parsed element: SUMMAND");
    return new AST.Summand(left, sum, operation);
  }

  private AST.Multiplier checkMultiplier() {
    AST.Multiplier multiplier;

    switch (inputStream.pickNext()) {
      case InLang.DIVIDER.BEG_CALL:
        inputStream.next();
        multiplier = checkExpression();
        inputStream.validateNext(InLang.DIVIDER.END_CALL);
        break;
      case InLang.CUSTOM.ID:
        multiplier = checkVarName();
        break;
      case InLang.CUSTOM.LITERAL:
        inputStream.next();
        multiplier = new AST.Literal(inputStream.next());
        break;
      default:
        inputStream.validateNext(InLang.INTERNAL.UNKNOWN);
        return null;
    }

    MyLog.v(LOG_TAG, "parsed element: MULTIPLIER");
    return multiplier;
  }

  private AST.For checkCycle() {
    inputStream.validateNext(InLang.KEYWORD.FOR);
    AST.Assignment start = checkAssign();
    inputStream.validateNext(InLang.KEYWORD.TO, InLang.KEYWORD.DOWNTO);
    AST.Expression end = checkExpression();
    inputStream.validateNext(InLang.KEYWORD.DO);
    AST.OpList opList;

    if (inputStream.pickNext() == InLang.KEYWORD.BEGIN) {
      opList = checkSectionOp();
    } else {
      ArrayList<AST.Operation> operations = new ArrayList<>();
      operations.add(checkOperator());
      opList = new AST.OpList(operations);
    }

    MyLog.v(LOG_TAG, "parsed element: FOR");
    return new AST.For(start, end, opList);
  }
}
