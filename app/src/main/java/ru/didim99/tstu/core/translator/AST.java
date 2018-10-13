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
    private OpList operaions;

    Program(Identifier name, ArrayList<VarDef> vars, OpList operations) {
      this.name = name;
      this.vars = vars;
      this.operaions = operations;
    }
  }

  static class VarDef {
    private int type;
    private VarList vars;

    VarDef(ArrayList<Identifier> vars, int type) {
      this.vars = new VarList(vars);
      this.type = type;
    }
  }

  static class VarList {
    private ArrayList<Identifier> vars;

    VarList(ArrayList<Identifier> vars) {
      this.vars = vars;
    }
  }

  static class OpList {
    private ArrayList<Operation> operations;

    OpList(ArrayList<Operation> operations) {
      this.operations = operations;
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
  }

  static class Summand {
    private Multiplier left;
    private Summand right;
    private int operation;

    Summand(Multiplier left) {
      this(left, null, 0);
    }

    Summand(Multiplier left, Summand right, int operation) {
      this.left = left;
      this.right = right;
      this.operation = operation;
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
  }

  static class Input implements Operation {
    private VarList vars;

    Input(ArrayList<Identifier> vars) {
      this.vars = new VarList(vars);
    }
  }

  static class Output implements Operation {
    private int action;
    private VarList vars;

    Output(int action, ArrayList<Identifier> vars) {
      this.action = action;
      this.vars = new VarList(vars);
    }
  }

  static class Assignment implements Operation {
    private Identifier target;
    private Expression expr;

    Assignment(Identifier target, Expression expr) {
      this.target = target;
      this.expr = expr;
    }
  }

  static class For implements Operation {
    private Assignment start;
    private Expression end;
    private OpList operations;

    public For(Assignment start, Expression end, OpList operations) {
      this.start = start;
      this.end = end;
      this.operations = operations;
    }
  }

  interface Operation {}
  interface Multiplier {}
}
