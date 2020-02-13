package ru.didim99.tstu.core.itheory;

import android.content.Context;
import android.preference.PreferenceManager;
import java.util.Locale;
import ru.didim99.tstu.R;

/**
 * Created by didim99 on 13.02.20.
 */
public class InformationCounter {
  private static final String KEY_CUSTOM_ALPHABET = "iCounter.customAlphabet";
  private static final String ALPHABET_LATIN = "qwertyuiopasdfghjklzxcvbnm";
  private static final String ALPHABET_CYRILLIC = "йцукенгшщзхъфывапролджэячсмитьбюё";
  private static final int LINE_LENGTH = 10;

  public static final class AlphabetType {
    public static final int LATIN     = 0;
    public static final int CYRILLIC  = 1;
    public static final int CUSTOM    = 2;
  }

  private int alphabetType;
  private String messageInfo;
  private String customAlphabet;
  private String alphabet;
  private double symbolWeight;

  public InformationCounter(Context context) {
    messageInfo = context.getString(R.string.iTheory_messageInfoContent);
    customAlphabet = PreferenceManager.getDefaultSharedPreferences(context)
      .getString(KEY_CUSTOM_ALPHABET, ALPHABET_LATIN);
    setAlphabetType(AlphabetType.LATIN);
  }

  public int getAlphabetType() {
    return alphabetType;
  }

  public String getCustomAlphabet() {
    return customAlphabet;
  }

  public void setAlphabetType(int alphabetType) {
    this.alphabetType = alphabetType;
    switch (alphabetType) {
      case AlphabetType.LATIN:
        alphabet = ALPHABET_LATIN;
        break;
      case AlphabetType.CYRILLIC:
        alphabet = ALPHABET_CYRILLIC;
        break;
      case AlphabetType.CUSTOM:
        alphabet = customAlphabet;
        break;
    }

    symbolWeight = Math.log(alphabet.length()) / Math.log(2.0);
  }

  public void setCustomAlphabet(Context context, String alphabet) {
    PreferenceManager.getDefaultSharedPreferences(context).edit()
      .putString(KEY_CUSTOM_ALPHABET, alphabet).apply();
    this.customAlphabet = alphabet;
    if (alphabetType == AlphabetType.CUSTOM)
      setAlphabetType(alphabetType);
  }

  public String getAlphabetInfo(Context context) {
    int total = alphabet.length(), line = 1;

    StringBuilder sb = new StringBuilder();
    sb.append(context.getString(R.string.iTheory_alphabetSymbols));
    sb.append("\n");
    for (int i = 0; i < total; i++) {
      sb.append(alphabet.charAt(i)).append(' ');
      if (line++ == LINE_LENGTH) {
        sb.append('\n');
        line = 1;
      }
    }

    sb.append("\n\n").append(context.getString(
      R.string.iTheory_alphabetPower, total));
    sb.append("\n").append(context.getString(
      R.string.iTheory_alphabetSymbolWeight, symbolWeight));
    return sb.toString();
  }

  public String getMessageInfo(String message) {
    if (message == null || message.isEmpty()) return null;
    char[] chars = message.toLowerCase().toCharArray();

    int iCount = 0;
    for (char aChar : chars) {
      if (alphabet.indexOf(aChar) > -1) iCount++;
    }

    return String.format(Locale.US, messageInfo,
      chars.length, iCount, iCount * symbolWeight);
  }
}
