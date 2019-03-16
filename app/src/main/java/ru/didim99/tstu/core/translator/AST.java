package ru.didim99.tstu.core.translator;

import java.util.ArrayList;

/**
 * Abstract syntax tree
 * Created by didim99 on 13.10.18.
 */
class AST {
  static class Program {
    private Identifier name;
    private ArrayList<VarDef> vars;
    private OpList operations;

    Program(Identifier name, ArrayList<VarDef> vars, OpList operations) {
      this.name = name;
      this.vars = vars;
      this.operations = operations;
    }

    Identifier getName() {
      return name;
    }

    ArrayList<VarDef> getVars() {
      return vars;
    }

    OpList getOperations() {
      return operations;
    }
  }

  static class VarDef {
    private int type;
    private VarList vars;

    VarDef(ArrayList<Identifier> vars, int type) {
      this.vars = new VarList(vars);
      this.type = type;
    }

    int getType() {
      return type;
    }

    VarList getVars() {
      return vars;
    }
  }

  static class VarList {
    private ArrayList<Identifier> list;

    VarList(ArrayList<Identifier> list) {
      this.list = list;
    }

    ArrayList<Identifier> getList() {
      return list;
    }
  }

  static class OpList {
    private ArrayList<Operation> list;

    OpList(ArrayList<Operation> operations) {
      this.list = operations;
    }

    ArrayList<Operation> getList() {
      return list;
    }
  }

  static class Expression implements Multiplier {
    private Summand left;
    private Expression right;
    private int operation;

    Expression(Summand left, Expression right, int operation) {
      this.left = left;
      this.right = right;
      this.operation = operation;
    }

    Summand getLeft() {
      return left;
    }

    Expression getRight() {
      return right;
    }

    int getOperation() {
      return operation;
    }
  }

  static class Summand {
    private Multiplier left;
    private Summand right;
    private int operation;

    Summand(Multiplier left, Summand right, int operation) {
      this.left = left;
      this.right = right;
      this.operation = operation;
    }

    Multiplier getLeft() {
      return left;
    }

    Summand getRight() {
      return right;
    }

    int getOperation() {
      return operation;
    }
  }

  static class Identifier implements Multiplier {
    private int index;

    Identifier(int index) {
      this.index = index;
    }

    int getIndex() {
      return index;
    }
  }

  static class Literal implements Multiplier {
    private int value;

    Literal(Integer value) {
      this.value = value;
    }

    int getValue() {
      return value;
    }
  }

  static class Input implements Operation {
    private VarList vars;

    Input(ArrayList<Identifier> vars) {
      this.vars = new VarList(vars);
    }

    VarList getVars() {
      return vars;
    }
  }

  static class Output implements Operation {
    private int action;
    private VarList vars;

    Output(int action, ArrayList<Identifier> vars) {
      this.action = action;
      this.vars = new VarList(vars);
    }

    VarList getVars() {
      return vars;
    }

    int getAction() {
      return action;
    }
  }

  static class Assignment implements Operation {
    private Identifier target;
    private Expression expr;

    Assignment(Identifier target, Expression expr) {
      this.target = target;
      this.expr = expr;
    }

    Identifier getTarget() {
      return target;
    }

    Expression getExpr() {
      return expr;
    }
  }

  static class Cycle implements Operation {
    private Assignment start;
    private Expression end;
    private OpList operations;
    private int type;

    public Cycle(Assignment start, Expression end,
                 int type, OpList operations) {
      this.start = start;
      this.end = end;
      this.type = type;
      this.operations = operations;
    }

    Assignment getStart() {
      return start;
    }

    Expression getEnd() {
      return end;
    }

    OpList getOperations() {
      return operations;
    }

    int getType() {
      return type;
    }
  }

  interface Operation {}
  interface Multiplier {}
}
