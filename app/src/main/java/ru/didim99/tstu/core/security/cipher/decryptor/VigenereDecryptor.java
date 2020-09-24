package ru.didim99.tstu.core.security.cipher.decryptor;

import android.content.Context;
import ru.didim99.tstu.R;
import ru.didim99.tstu.utils.CyclicBuffer;

/**
 * Created by didim99 on 20.09.20.
 */

public class VigenereDecryptor extends Decryptor {

  private String alphabet, keyword;

  @Override
  public void configure(String[] params) {
    checkArgumentsCount(params, 1);
    checkEmptyArguments(params);
    String keyword = params[0];
    checkAlphabet(keyword, ALPHABET_RUS);
    this.alphabet = ALPHABET_RUS;
    this.keyword = keyword;
  }

  @Override
  public String getDescription(Context context) {
    return context.getString(R.string.is_cipher_keyword, keyword);
  }

  @Override
  public String decrypt(String data) {
    CyclicBuffer<Character> keyBuffer =
      new CyclicBuffer<>(keyword.length(), '\00');
    for (char c : keyword.toCharArray()) keyBuffer.push(c);
    StringBuilder sb = new StringBuilder();
    int n = alphabet.length();

    for (int i = 0; i < data.length(); i++) {
      int input = alphabet.indexOf(data.charAt(i));
      int offset = alphabet.indexOf(keyBuffer.get(i));
      int pos = (input - offset + n) % n;
      sb.append(alphabet.charAt(pos));
    }

    return sb.toString();
  }

  @Override
  public boolean isConfigured() {
    return this.keyword != null && !this.keyword.isEmpty();
  }
}
