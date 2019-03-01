package ru.didim99.tstu.core.mp.radio;

/**
 * Created by didim99 on 01.03.19.
 */
class RadioResponse {
  private String result;
  private String info;

  String getInfo() {
    return info;
  }

  boolean isSuccessful() {
    return result != null && result.equals("success");
  }
}
