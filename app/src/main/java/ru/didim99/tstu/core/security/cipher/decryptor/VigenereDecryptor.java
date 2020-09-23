package ru.didim99.tstu.core.security.cipher.decryptor;

import ru.didim99.tstu.utils.CyclicBuffer;

/**
 * Created by didim99 on 20.09.20.
 */

public class VigenereDecryptor extends Decryptor {

  private String alphsbet, keyword;

  @Override
  public void configure(String[] params) {
    checkArgumentsCount(params, 1);
    checkEmptyArguments(params);
    String keyword = params[0];
    checkAlphabet(keyword, ALPHABET_RUS);
    this.alphsbet = ALPHABET_RUS;
    this.keyword = keyword;
  }

  @Override
  public String decrypt(String data) {
    CyclicBuffer<Character> keyBuffer =
      new CyclicBuffer<>(Character.class, keyword.length());
    for (char c : keyword.toCharArray()) keyBuffer.push(c);
    StringBuilder sb = new StringBuilder();
    int n = alphsbet.length();

    for (int i = 0; i < data.length(); i++) {
      int input = alphsbet.indexOf(data.charAt(i));
      int offset = alphsbet.indexOf(keyBuffer.get(i));
      int pos = (input + offset + n) % n;
      sb.append(alphsbet.charAt(pos));
    }

    return sb.toString();
  }

  @Override
  public boolean isConfigured() {
    return this.keyword != null && !this.keyword.isEmpty();
  }
}
