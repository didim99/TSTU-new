package ru.didim99.tstu.core.translator;

import android.content.Context;
import java.io.IOException;
import java.util.ArrayList;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 06.09.18.
 */
public class Translator {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Translator";

  public static final class Mode {
    public static final int LEXICAL = 1;
    public static final int SYNTAX = 2;
    public static final int SYMBOLS = 3;
    public static final int FULL = 4;
  }

  private Context context;
  private Config config;

  Translator(Context context, Config config) {
    this.context = context;
    this.config = config;
  }

  Result translate() {
    Result result = new Result();
    ArrayList<String> srcList, tmpList;

    try {
      srcList = Utils.readFile(config.inputFilename);
      result.inputCode = Utils.joinStr("\n", srcList);
    } catch (IOException e) {
      MyLog.e(LOG_TAG, "Can't read input file\n  " + e.toString());
      result.inputErr = context.getString(R.string.errGeneric_inputFileNotReadable);
      return result;
    }

    if (config.mode >= Mode.LEXICAL) {
      LexicalAnalyzer la = new LexicalAnalyzer();
      try {
        tmpList = la.analyze(srcList);
        result.outputCode = Utils.joinStr("\n", tmpList);
      } catch (LexicalAnalyzer.ProcessException e) {
        MyLog.e(LOG_TAG, "Lexical analysis error\n  " + e.toString());
        result.processErr = context.getString(R.string.errLexical_invalidStatement);
        return result;
      }
    }

    return result;
  }

  public static class Config {
    private String inputFilename;
    private int mode;

    public Config(int mode, String inputFilename) {
      this.inputFilename = inputFilename;
      this.mode = mode;
    }
  }

  public static class Result {
    private String inputCode, outputCode;
    private String inputErr, processErr;

    Result() {}

    public String getInputCode() { return inputCode; }
    public String getOutputCode() { return outputCode; }
    public String getInputErr() { return inputErr; }
    public String getProcessErr() { return processErr; }

    public boolean hasInputCode() {
      return inputCode != null;
    }

    public boolean hasOutputCode() {
      return outputCode != null;
    }
  }
}
