package ru.didim99.tstu.core.translator;

/**
 * Source language structure declaration
 * Created by didim99 on 11.09.18.
 */
final class LangStruct {
  static final class MASK {
    static final int KEYWORD  = 0x0000000f;
    static final int OPERATOR = 0x000000f0;
    static final int DIVIDER  = 0x00000f00;
    static final int CUSTOM   = 0x0000f000;
  }

  static final class KEYWORD {
    static final int PROGRAM  = 0x00000001;
    static final int VAR      = 0x00000002;
    static final int INTEGER  = 0x00000003;
    static final int REAL     = 0x00000004;
    static final int BEGIN    = 0x00000005;
    static final int END      = 0x00000006;
    static final int FOR      = 0x00000007;
    static final int TO       = 0x00000008;
    static final int DOWNTO   = 0x00000009;
    static final int DO       = 0x0000000A;
    static final int READ     = 0x0000000B;
    static final int WRITE    = 0x0000000C;
    static final int WRITELN  = 0x0000000D;
  }

  static final class OPERATOR {
    static final int ASSIGN   = 0x00000010; // :=
    static final int PLUS     = 0x00000020; // +
    static final int MINUS    = 0x00000030; // -
    static final int PRODUCT  = 0x00000040; // *
    static final int DIV      = 0x00000050; // /
    static final int EQUAL    = 0x00000060; // =
    static final int LT       = 0x00000070; // <
    static final int GT       = 0x00000080; // >
  }

  static final class DIVIDER {
    static final int END_OP   = 0x00000100; // ;
    static final int SEP_VL   = 0x00000200; // ,
    static final int END_VL   = 0x00000300; // :
    static final int BEG_CALL = 0x00000400; // (
    static final int END_CALL = 0x00000500; // )
    static final int END_PROG = 0x00000600; // .
  }

  static final class CUSTOM {
    static final int ID       = 0x00001000; // Variable/program name
    static final int LITERAL  = 0x00002000;
  }

  static class MNEMONIC {
    static final class KEYWORD {
      static final String PROGRAM  = "program";
      static final String VAR      = "var";
      static final String INTEGER  = "integer";
      static final String REAL     = "real";
      static final String BEGIN    = "begin";
      static final String END      = "end";
      static final String FOR      = "for";
      static final String TO       = "to";
      static final String DOWNTO   = "downto";
      static final String DO       = "do";
      static final String READ     = "read";
      static final String WRITE    = "write";
      static final String WRITELN  = "writeln";
    }

    static final class OPERATOR {
      static final String ASSIGN   = ":=";
      static final String PLUS     = "+";
      static final String MINUS    = "-";
      static final String PRODUCT  = "*";
      static final String DIV      = "/";
      static final String EQUAL    = "=";
      static final String LT       = "<";
      static final String GT       = ">";
    }

    static final class DIVIDER {
      static final String END_PROG = ".";
      static final String END_OP   = ";";
      static final String SEP_VL   = ",";
      static final String END_VL   = ":";
      static final String BEG_CALL = "(";
      static final String END_CALL = ")";
    }
  }

  static final String[] SYMBOL_SEARCH_ORDER = {
    MNEMONIC.KEYWORD.PROGRAM, MNEMONIC.KEYWORD.VAR, MNEMONIC.KEYWORD.INTEGER,
    MNEMONIC.KEYWORD.REAL, MNEMONIC.KEYWORD.BEGIN, MNEMONIC.KEYWORD.END,
    MNEMONIC.KEYWORD.FOR, MNEMONIC.KEYWORD.DOWNTO, MNEMONIC.KEYWORD.DO,
    MNEMONIC.KEYWORD.TO, MNEMONIC.KEYWORD.READ, MNEMONIC.KEYWORD.WRITELN,
    MNEMONIC.KEYWORD.WRITE, MNEMONIC.OPERATOR.ASSIGN, MNEMONIC.DIVIDER.END_VL,
    MNEMONIC.DIVIDER.END_OP, MNEMONIC.DIVIDER.END_PROG, MNEMONIC.DIVIDER.SEP_VL,
    MNEMONIC.DIVIDER.BEG_CALL, MNEMONIC.DIVIDER.END_CALL, MNEMONIC.OPERATOR.PLUS,
    MNEMONIC.OPERATOR.MINUS, MNEMONIC.OPERATOR.PRODUCT, MNEMONIC.OPERATOR.DIV,
    MNEMONIC.OPERATOR.EQUAL, MNEMONIC.OPERATOR.LT, MNEMONIC.OPERATOR.GT,
  };
}
