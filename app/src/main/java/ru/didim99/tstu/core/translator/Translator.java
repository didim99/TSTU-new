package ru.didim99.tstu.core.translator;

import android.content.Context;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.translator.utils.PrecedenceTable;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 06.09.18.
 */
public class Translator {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Translator";
  private static final String BASEDIR = "/translator";
  private final String ROOTDIR;

  public static final class Mode {
    public static final int LEXICAL   = 1;
    public static final int SYNTAX    = 2;
    public static final int SYMBOLS   = 3;
    public static final int FULL      = 4;
  }

  public static final class SAType {
    public static final int DESCENT     = 1;
    public static final int PRECEDENCE  = 2;
  }

  private Context context;
  private Config config;
  private Result result;
  private LexicalAnalyzer la;

  Translator(Context context, Config config) {
    ROOTDIR = context.getExternalCacheDir()
      .getAbsolutePath() + BASEDIR;

    this.context = context;
    this.config = config;
    this.result = new Result();
  }

  Result translate() {
    if (!initStatic())
      return result;
    ArrayList<String> srcList;
    LexicalAnalyzer.Result laResult = null;
    SyntaxAnalyzer.Result saResult = null;
    SyntaxAnalyzer sa;

    try {
      srcList = Utils.readFile(config.inputFilename);
      result.inputCode = Utils.joinStr("\n", srcList);
    } catch (IOException e) {
      MyLog.e(LOG_TAG, "Can't read input file\n  " + e.toString());
      result.inputErr = context.getString(R.string.errGeneric_inputFileNotReadable);
      return result;
    }

    if (config.mode >= Mode.LEXICAL) {
      try {
        laResult = la.analyze(srcList, config.forTesting);
        if (config.forTesting)
          result.outputCode = Utils.joinStr("\n", laResult.getLines());
      } catch (LexicalAnalyzer.ProcessException e) {
        MyLog.e(LOG_TAG, "Lexical analysis error" +
          "(" + e.getLineNum() +  "}: " + e.toString());
        result.processErr = context.getString(
          R.string.errLexical_invalidStatement, e.getLineNum());
        return result;
      }
    }

    if (config.mode >= Mode.SYNTAX) {
      try {
        result.outputCode = null;
        switch (config.saType) {
          case SAType.DESCENT:
            sa = new DescentAnalyzer(laResult);
            saResult = sa.analyze();
            result.outputCode = "Синтаксический анализ успешно выполнен";
            break;
          case SAType.PRECEDENCE:
            sa = new PrecedenceAnalyzer(laResult);
            saResult = sa.analyze();
            result.outputCode = ((PrecedenceAnalyzer) sa).getOutput();
            break;
          default:
            MyLog.e(LOG_TAG, "Unknown syntax analysis type");
            result.processErr = context.getString(R.string.errSyntaxType);
            return result;
        }
      } catch (SyntaxAnalyzer.ProcessException e) {
        e.printStackTrace();
        MyLog.e(LOG_TAG, "Syntax analysis error" +
          "(" + e.getLineNum() +  "}: " + e.toString());
        result.processErr = context.getString(
          R.string.errSyntax, e.getLineNum(), e.getMessage());
        return result;
      }
    }

    if (config.mode >= Mode.SYMBOLS) {
      result.outputCode = saResult.getSymbolTable().toString();
      return result;
    }

    return result;
  }

  private boolean initStatic() {
    LangStruct ls = LangStruct.getInstance();
    if (!ls.initCompleted()) {
      try {
        String lsFile = ROOTDIR + "/inputLangStruct.txt";
        ls.initStatic(lsFile);
      } catch (IOException e) {
        MyLog.w(LOG_TAG, "Can't load input language structure: " + e);
        result.processErr = e.toString();
        return false;
      }
    }

    PrecedenceTable pt = PrecedenceTable.getInstance();
    if (!pt.isInitCompleted()) {
      try {
        String tableFile = ROOTDIR + "/precedence.txt";
        pt.initStatic(tableFile);
      } catch (IOException e) {
        MyLog.w(LOG_TAG, "Can't initialize precedence table: " + e);
        result.processErr = e.toString();
        return false;
      }
    }

    la = new LexicalAnalyzer();
    result.symbolSet = new ArrayList<>();
    for (LangStruct.DictEntry entry : la.getSortedSymbolSet()) {
      result.symbolSet.add(String.format(Locale.US, "%8s  %-4d  0x%04x",
        entry.getMnemonic(), entry.getValue(), entry.getValue()));
    }

    return true;
  }

  public static class Config {
    private boolean forTesting;
    private String inputFilename;
    private int mode, saType;

    public Config(int mode, int saType, String inputFilename, boolean forTesting) {
      this.mode = mode;
      this.saType = saType;
      this.inputFilename = inputFilename;
      this.forTesting = forTesting;
    }
  }

  public static class Result {
    private String inputCode, outputCode;
    private String inputErr, processErr;
    private ArrayList<String> symbolSet;

    public String getInputCode() { return inputCode; }
    public String getOutputCode() { return outputCode; }
    public String getInputErr() { return inputErr; }
    public String getProcessErr() { return processErr; }
    public ArrayList<String> getSymbolSet() { return symbolSet; }

    public boolean hasInputCode() { return inputCode != null; }
    public boolean hasOutputCode() { return outputCode != null; }
  }
}
