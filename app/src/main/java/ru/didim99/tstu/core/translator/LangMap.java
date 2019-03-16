package ru.didim99.tstu.core.translator;

/**
 * Source language structure declaration
 * Created by didim99 on 27.11.18.
 */
public final class LangMap {
  public static final class MASK {
    public static final int KEYWORD   = 0x0000000f;
    public static final int OPERATOR  = 0x000000f0;
    public static final int DIVIDER   = 0x00000f00;
    public static final int CUSTOM    = 0x0000f000;
    public static final int INTERNAL  = 0xf0000000;
  }

  public static final class INTERNAL {
    public static final int NEWLINE   = 0x10000000;
    public static final int UNKNOWN   = 0xf0000000;
  }

  public static final class CUSTOM {
    public static final int ID        = 0x00001000; // Variable/program name
    public static final int LITERAL   = 0x00002000;
  }

  static final class KEYWORD {
    static final int PROGRAM   = 0x00000001;
    static final int VAR       = 0x00000002;
    static final int INTEGER   = 0x00000003;
    static final int REAL      = 0x00000004;
    static final int START     = 0x00000005;
    static final int END       = 0x00000006;
    static final int CYCLE     = 0x00000007;
    static final int TO        = 0x00000008;
    static final int DOWNTO    = 0x00000009;
    static final int DO        = 0x0000000A;
    static final int IN        = 0x0000000B;
    static final int OUT       = 0x0000000C;
    static final int OUTLINE   = 0x0000000D;
  }

  static final class OPERATOR {
    static final int ASSIGN    = 0x00000010; // :=
    static final int PLUS      = 0x00000020; // +
    static final int MINUS     = 0x00000030; // -
    static final int PRODUCT   = 0x00000040; // *
    static final int DIV       = 0x00000050; // /
    static final int EQUAL     = 0x00000060; // =
    static final int LT        = 0x00000070; // <
    static final int GT        = 0x00000080; // >
  }

  static final class DIVIDER {
    static final int DOT       = 0x00000100; // .
    static final int SEMICOLON = 0x00000200; // ;
    static final int COMMA     = 0x00000300; // ,
    static final int COLON     = 0x00000400; // :
    static final int LBR       = 0x00000500; // (
    static final int RBR       = 0x00000600; // )
  }

  static final class FORMAT {
    static final int ANY       = 0x00010000; // *
  }

  static class PASCAL {
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

    static final class CUSTOM {
      static final String ID       = "ID";
      static final String LITERAL  = "LITERAL";
    }
  }
}
