package ru.didim99.tstu.core.security.cipher.decryptor;

import android.content.Context;

/**
 * Created by didim99 on 20.09.20.
 */

public class ADFGVXDecryptor extends Decryptor {

  @Override
  public void configure(String[] params) {

  }

  @Override
  public String getDescription(Context context) {
    StringBuilder sb = new StringBuilder();
    return sb.toString();
  }

  @Override
  public String decrypt(String data) {
    return data;
  }

  @Override
  public boolean isConfigured() {
    return false;
  }
}
