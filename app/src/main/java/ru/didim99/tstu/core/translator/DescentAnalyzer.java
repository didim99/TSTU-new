package ru.didim99.tstu.core.translator;

import java.util.ArrayList;
import ru.didim99.tstu.core.translator.AST.*;
import ru.didim99.tstu.core.translator.LangMap.*;
import ru.didim99.tstu.core.translator.utils.SymbolTable;
import ru.didim99.tstu.core.translator.utils.SyntaxStream;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 01.11.18.
 */
class DescentAnalyzer extends SyntaxAnalyzer {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_DSA";

  private SyntaxStream inputStream;
  private ArrayList<VarDef> vars;

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

  private Program checkProgram() {
    Identifier name = checkHeader();
    inputStream.validateNext(DIVIDER.SEMICOLON);
    vars = checkSectionVar();
    OpList operations = checkSectionOp();
    inputStream.validateNext(DIVIDER.DOT);
    MyLog.v(LOG_TAG, "parsed element: PROGRAM");
    return new Program(name, vars, operations);
  }

  private Identifier checkHeader() {
    inputStream.validateNext(KEYWORD.PROGRAM);
    Identifier name = checkVarName(true);
    MyLog.v(LOG_TAG, "parsed element: HEADER");
    return name;
  }

  private Identifier checkVarName(boolean init) {
    inputStream.validateNext(CUSTOM.ID);
    MyLog.v(LOG_TAG, "parsed element: IDENTIFIER");
    Identifier id = new Identifier(inputStream.next());
    if (!init) checkDefinition(id);
    return id;
  }

  private ArrayList<VarDef> checkSectionVar() {
    inputStream.validateNext(KEYWORD.VAR);
    ArrayList<VarDef> varDef = checkVarDeclaration();
    MyLog.v(LOG_TAG, "parsed element: VAR SECTION");
    return varDef;
  }

  private ArrayList<VarDef> checkVarDeclaration() {
    ArrayList<VarDef> varDefs = new ArrayList<>();
    ArrayList<Identifier> vars = checkVarList(true);
    SymbolTable table = result.getSymbolTable();
    inputStream.validateNext(DIVIDER.COLON);
    int type = checkType();

    for (Identifier var : vars)
      table.get(var.getIndex()).setType(type);

    varDefs.add(new VarDef(vars, type));
    inputStream.validateNext(DIVIDER.SEMICOLON);
    if (inputStream.pickNext() == CUSTOM.ID) {
      varDefs.addAll(checkVarDeclaration());
    }

    MyLog.v(LOG_TAG, "parsed element: VAR DECLARATION");
    return varDefs;
  }

  private ArrayList<Identifier> checkVarList(boolean init) {
    ArrayList<Identifier> vars = new ArrayList<>();
    vars.add(checkVarName(init));

    if (inputStream.pickNext() == DIVIDER.COMMA) {
      inputStream.next();
      vars.addAll(checkVarList(init));
    }

    MyLog.v(LOG_TAG, "parsed element: VAR LIST");
    return vars;
  }

  private int checkType() {
    switch (inputStream.pickNext()) {
      case KEYWORD.INTEGER:
      case KEYWORD.REAL:
        MyLog.v(LOG_TAG, "parsed element: VAR TYPE");
        return inputStream.next();
      default:
        inputStream.validateNext(INTERNAL.UNKNOWN);
        return INTERNAL.UNKNOWN;
    }
  }

  private OpList checkSectionOp() {
    inputStream.validateNext(KEYWORD.START);
    ArrayList<Operation> operations = checkOpList();
    inputStream.validateNext(KEYWORD.END);
    MyLog.v(LOG_TAG, "parsed element: OPERATOR SECTION");
    return new OpList(operations);
  }

  private ArrayList<Operation> checkOpList() {
    ArrayList<Operation> operations = new ArrayList<>();
    operations.add(checkOperator());
    if (inputStream.pickNext() == DIVIDER.SEMICOLON) {
      inputStream.next();
      operations.addAll(checkOpList());
    }

    MyLog.v(LOG_TAG, "parsed element: OPERATOR LIST");
    return operations;
  }

  private Operation checkOperator() {
    switch (inputStream.pickNext()) {
      case KEYWORD.IN:
        return checkInput();
      case KEYWORD.OUTLINE:
      case KEYWORD.OUT:
        return checkOutput();
      case KEYWORD.CYCLE:
        return checkCycle();
      case CUSTOM.ID:
        return checkAssign();
      default:
        inputStream.validateNext(INTERNAL.UNKNOWN);
        return null;
    }
  }

  private Operation checkInput() {
    inputStream.validateNext(KEYWORD.IN);
    inputStream.validateNext(DIVIDER.LBR);
    ArrayList<Identifier> vars = checkVarList(false);
    inputStream.validateNext(DIVIDER.RBR);
    MyLog.v(LOG_TAG, "parsed element: IN");
    return new Input(vars);
  }

  private Operation checkOutput() {
    int operator = inputStream.validateNext(
      KEYWORD.OUT, KEYWORD.OUTLINE);
    inputStream.validateNext(DIVIDER.LBR);
    ArrayList<Identifier> vars = checkVarList(false);
    inputStream.validateNext(DIVIDER.RBR);
    MyLog.v(LOG_TAG, "parsed element: OUTPUT");
    return new Output(operator, vars);
  }

  private Assignment checkAssign() {
    Identifier target = checkVarName(false);
    inputStream.validateNext(OPERATOR.ASSIGN);
    Expression expr = checkExpression();
    MyLog.v(LOG_TAG, "parsed element: ASSIGNMENT");
    return new Assignment(target, expr);
  }

  private Expression checkExpression() {
    Summand left = checkSummand();
    Expression expr = null;
    int operation = 0;

    if ((inputStream.pickNext()
      & (OPERATOR.PLUS
      | OPERATOR.MINUS)) > 0) {
      operation = inputStream.next();
      expr = checkExpression();
    }

    MyLog.v(LOG_TAG, "parsed element: EXPRESSION");
    return new Expression(left, expr, operation);
  }

  private Summand checkSummand() {
    Multiplier left = checkMultiplier();
    Summand sum = null;
    int operation = 0;

    if ((inputStream.pickNext()
      & (OPERATOR.PRODUCT
      | OPERATOR.DIV)) > 0) {
      operation = inputStream.next();
      sum = checkSummand();
    }

    MyLog.v(LOG_TAG, "parsed element: SUMMAND");
    return new Summand(left, sum, operation);
  }

  private Multiplier checkMultiplier() {
    Multiplier multiplier;

    switch (inputStream.pickNext()) {
      case DIVIDER.LBR:
        inputStream.next();
        multiplier = checkExpression();
        inputStream.validateNext(DIVIDER.RBR);
        break;
      case CUSTOM.ID:
        multiplier = checkVarName(false);
        break;
      case CUSTOM.LITERAL:
        inputStream.next();
        multiplier = new Literal(inputStream.next());
        break;
      default:
        inputStream.validateNext(INTERNAL.UNKNOWN);
        return null;
    }

    MyLog.v(LOG_TAG, "parsed element: MULTIPLIER");
    return multiplier;
  }

  private Cycle checkCycle() {
    inputStream.validateNext(KEYWORD.CYCLE);
    Assignment start = checkAssign();
    int type = inputStream.validateNext(
      KEYWORD.TO, KEYWORD.DOWNTO);
    Expression end = checkExpression();
    inputStream.validateNext(KEYWORD.DO);
    OpList opList;

    if (inputStream.pickNext() == KEYWORD.START) {
      opList = checkSectionOp();
    } else {
      ArrayList<Operation> operations = new ArrayList<>();
      operations.add(checkOperator());
      opList = new OpList(operations);
    }

    MyLog.v(LOG_TAG, "parsed element: CYCLE");
    return new Cycle(start, end, type, opList);
  }

  private void checkDefinition(Identifier id) {
    for (VarDef list : vars) {
      for (Identifier defined : list.getVars().getList()) {
        if (defined.getIndex() == id.getIndex()) return;
      }
    }

    Symbol s = result.getSymbolTable().get(id.getIndex());
    throw new ProcessException("Undefined variable: "
      + s.getName(), inputStream.getLineNim());
  }
}
