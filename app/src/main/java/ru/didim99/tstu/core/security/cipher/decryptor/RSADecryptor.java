package ru.didim99.tstu.core.security.cipher.decryptor;

import android.content.Context;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.itheory.encryption.math.FastMath;
import ru.didim99.tstu.core.itheory.encryption.rsa.RSAKey;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 20.09.20.
 */

public class RSADecryptor extends Decryptor {
  private static final String ALPHABET = ALPHABET_RUS;

  private RSAKey key;

  @Override
  public void configure(String[] params) {
    checkArgumentsCount(params, 2);
    checkEmptyArguments(params);
    long n = Long.parseLong(params[0]);
    long d = Long.parseLong(params[1]);
    key = new RSAKey(0, d, n);
  }

  @Override
  public String getDescription(Context context) {
    if (!isConfigured()) return null;
    return context.getString(R.string.is_cipher_key, key);
  }

  @Override
  public String decrypt(String data) {
    long[] sequence = Utils.stringArrayToLongArray(data.split(" "));
    if (sequence == null) throw new IllegalArgumentException("Invalid input data");
    StringBuilder sb = new StringBuilder();
    long d = key.getD().longValue();
    long n = key.getN().longValue();

    for (long c : sequence) {
      int pos = (int) FastMath.powm(c, d, n);
      sb.append(ALPHABET.charAt(pos));
    }

    return sb.toString();
  }

  @Override
  public boolean isConfigured() {
    return key != null;
  }
}
