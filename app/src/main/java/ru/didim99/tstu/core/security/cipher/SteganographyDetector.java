package ru.didim99.tstu.core.security.cipher;

import android.content.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.security.cipher.decryptor.Decryptor;

/**
 * Created by didim99 on 26.09.20.
 */

public class SteganographyDetector extends Decryptor {
  private static final char COLOR_MARKER = '#';

  private String secretColor;

  @Override
  public void configure(String[] params) {
    checkArgumentsCount(params, 1);
    checkEmptyArguments(params);
    String color = params[0].toUpperCase();
    checkArgumentLength(color, 6, 7);
    try {
      Integer.decode(color.charAt(0) == COLOR_MARKER ?
        color : COLOR_MARKER + color);
      secretColor = color.charAt(0) == COLOR_MARKER ?
        color.substring(1) : color;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("incorrect color string: " + color);
    }
  }

  @Override
  public String getSampleConfig() {
    return "<color:[#]hexstr>";
  }

  @Override
  public String getDescription(Context context) {
    if (!isConfigured()) return null;
    return context.getString(R.string.is_secret_color, secretColor);
  }

  @Override
  public String decrypt(String data) {
    Document document = Jsoup.parse(data);
    StringBuilder sb = new StringBuilder();

    Pattern pattern = Pattern.compile("background\\s*:\\s*#([0-9a-f]{6});?");
    Elements spans = document.getElementsByTag("span");
    for (Element span : spans) {
      String style = span.attr("style").toLowerCase();
      Matcher matcher = pattern.matcher(style);
      if (!matcher.find()) continue;
      String color = matcher.group(1).toUpperCase();
      if (secretColor.equals(color))
        sb.append(span.wholeText());
    }

    return sb.toString();
  }

  @Override
  public boolean isConfigured() {
    return secretColor != null && !secretColor.isEmpty();
  }
}
