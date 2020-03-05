package ru.didim99.tstu.utils;

import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigInteger;

import ru.didim99.tstu.BuildConfig;

/**
 * Input values validator and converter
 * Created by didim99 on 16.08.18.
 */
public class InputValidator {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_InputValidator";
  private static InputValidator instance = new InputValidator();
  public static InputValidator getInstance() { return instance; }
  private InputValidator() {}

  private Toast toastMsg;

  public void init(Context appContext) {
    toastMsg = Toast.makeText(appContext, "", Toast.LENGTH_LONG);
  }

  public BigInteger checkBigInteger(EditText src, int msgId_empty,
                                    int msgId_incorrect, String logMsg)
    throws ValidationException {
    String strValue = checkEmptyStr(src, msgId_empty, logMsg);
    if (strValue == null) return null;

    try {
      return new BigInteger(strValue);
    } catch (IllegalArgumentException e) {
      MyLog.w(LOG_TAG, "Incorrect value: " + logMsg);
      if (BuildConfig.DEBUG) e.printStackTrace();
      showToast(msgId_incorrect);
      throw new ValidationException(e);
    }
  }

  public Integer checkInteger(EditText src, Integer minValue,
                              int msgId_empty, int msgId_incorrect, String logMsg)
    throws ValidationException {
    return checkInteger(src, minValue, null,
      msgId_empty, msgId_incorrect, logMsg);
  }

  public Integer checkInteger(EditText src, Integer minValue, Integer maxValue,
                              int msgId_empty, int msgId_incorrect, String logMsg)
    throws ValidationException {
    if (minValue == null) minValue = Integer.MIN_VALUE;
    if (maxValue == null) maxValue = Integer.MAX_VALUE;

    String strValue = checkEmptyStr(src, msgId_empty, logMsg);
    if (strValue == null) return null;

    try {
      Integer result = Integer.parseInt(strValue);
      if (result < minValue || result > maxValue)
        throw new IllegalArgumentException("Incorrect value "
          + "(min: " + minValue + ", max: " + maxValue + ", value: " + result + ")");
      return result;
    } catch (IllegalArgumentException e) {
      MyLog.w(LOG_TAG, "Incorrect value: " + logMsg);
      if (BuildConfig.DEBUG) e.printStackTrace();
      showToast(msgId_incorrect);
      throw new ValidationException(e);
    }
  }

  public Float checkFloat(EditText src, Number multiplier,
                          int msgId_empty, int msgId_incorrect, String logMsg)
    throws ValidationException {
    return checkFloat(src, multiplier, null, null,
      msgId_empty, msgId_incorrect, logMsg);
  }

  public Float checkFloat(EditText src, Number multiplier,
                          int msgId_incorrect, String logMsg)
    throws ValidationException {
    return checkFloat(src, multiplier, null, null,
      0, msgId_incorrect, logMsg);
  }

  public Float checkFloat(EditText src, Number multiplier, Number minValue, Number maxValue,
                          int msgId_empty, int msgId_incorrect, String logMsg)
    throws ValidationException {
    if (multiplier == null) multiplier = 1;
    if (minValue == null) minValue = -Double.MAX_VALUE;
    if (maxValue == null) maxValue = Double.MAX_VALUE;

    String strValue = checkEmptyStr(src, msgId_empty, logMsg);
    if (strValue == null) return null;

    try {
      Double result = Double.parseDouble(strValue) * multiplier.doubleValue();
      if (result < minValue.doubleValue() || result > maxValue.doubleValue())
        throw new IllegalArgumentException("Incorrect value "
          + "(min: " + minValue + ", max: " + maxValue + ", value: " + result + ")");
      return result.floatValue();
    } catch (IllegalArgumentException e) {
      MyLog.w(LOG_TAG, "Incorrect value: " + logMsg);
      if (BuildConfig.DEBUG) e.printStackTrace();
      showToast(msgId_incorrect);
      throw new ValidationException(e);
    }
  }

  public String checkEmptyStr(EditText src, int msgId_empty, String logMsg)
    throws ValidationException {
    String strValue = src.getText().toString();
    if (strValue.isEmpty()) {
      if (msgId_empty != 0) {
        MyLog.w(LOG_TAG, "Empty value: " + logMsg);
        showToast(msgId_empty);
        throw new ValidationException();
      } else
        return null;
    } else return strValue;
  }

  private void showToast(int msgId) {
    toastMsg.setText(msgId);
    toastMsg.show();
  }

  public static class ValidationException extends IllegalArgumentException {
    ValidationException(Throwable cause) { super(cause); }
    ValidationException() { super(); }
  }
}
