package ru.didim99.tstu.core.translator;

/**
 * Source language structure declaration
 * Created by didim99 on 11.09.18.
 */
public final class LangStruct {
  public static final class MASK {
    public static final int KEYWORD  = 0x0000000f;
    public static final int OPERATOR = 0x000000f0;
    public static final int DIVIDER  = 0x00000f00;
    public static final int CUSTOM   = 0x0000f000;
    public static final int INTERNAL = 0xf0000000;
  }

  public static final class INTERNAL {
    public static final int NEWLINE = 0x10000000;
    public static final int UNKNOWN = 0xf0000000;
  }

  public static final class KEYWORD {
    public static final int PROGRAM  = 0x00000001;
    public static final int VAR      = 0x00000002;
    public static final int INTEGER  = 0x00000003;
    public static final int REAL     = 0x00000004;
    public static final int BEGIN    = 0x00000005;
    public static final int END      = 0x00000006;
    public static final int FOR      = 0x00000007;
    public static final int TO       = 0x00000008;
    public static final int DOWNTO   = 0x00000009;
    public static final int DO       = 0x0000000A;
    public static final int READ     = 0x0000000B;
    public static final int WRITE    = 0x0000000C;
    public static final int WRITELN  = 0x0000000D;
  }

  public static final class OPERATOR {
    public static final int ASSIGN   = 0x00000010; // :=
    public static final int PLUS     = 0x00000020; // +
    public static final int MINUS    = 0x00000030; // -
    public static final int PRODUCT  = 0x00000040; // *
    public static final int DIV      = 0x00000050; // /
    public static final int EQUAL    = 0x00000060; // =
    public static final int LT       = 0x00000070; // <
    public static final int GT       = 0x00000080; // >
  }

  public static final class DIVIDER {
    public static final int END_PROG = 0x00000100; // .
    public static final int END_OP   = 0x00000200; // ;
    public static final int SEP_VL   = 0x00000300; // ,
    public static final int END_VL   = 0x00000400; // :
    public static final int BEG_CALL = 0x00000500; // (
    public static final int END_CALL = 0x00000600; // )
  }

  public static final class CUSTOM {
    public static final int ID       = 0x00001000; // Variable/program name
    public static final int LITERAL  = 0x00002000;
  }

  public static class MNEMONIC {
    public static final class KEYWORD {
      public static final String PROGRAM  = "program";
      public static final String VAR      = "var";
      public static final String INTEGER  = "integer";
      public static final String REAL     = "real";
      public static final String BEGIN    = "begin";
      public static final String END      = "end";
      public static final String FOR      = "for";
      public static final String TO       = "to";
      public static final String DOWNTO   = "downto";
      public static final String DO       = "do";
      public static final String READ     = "read";
      public static final String WRITE    = "write";
      public static final String WRITELN  = "writeln";
    }

    public static final class OPERATOR {
      public static final String ASSIGN   = ":=";
      public static final String PLUS     = "+";
      public static final String MINUS    = "-";
      public static final String PRODUCT  = "*";
      public static final String DIV      = "/";
      public static final String EQUAL    = "=";
      public static final String LT       = "<";
      public static final String GT       = ">";
    }

    public static final class DIVIDER {
      public static final String END_PROG = ".";
      public static final String END_OP   = ";";
      public static final String SEP_VL   = ",";
      public static final String END_VL   = ":";
      public static final String BEG_CALL = "(";
      public static final String END_CALL = ")";
    }

    public static final class CUSTOM {
      public static final String ID       = "ID";
      public static final String LITERAL  = "LITERAL";
    }
  }

  static final class DictEntry {
    private String mnemonic;
    private int value;

    DictEntry(String mnemonic, int value) {
      this.mnemonic = mnemonic;
      this.value = value;
    }

    DictEntry(DictEntry other) {
      this(other.mnemonic, other.value);
    }

    String getMnemonic() { return mnemonic; }
    int getValue() { return value; }
  }
}
