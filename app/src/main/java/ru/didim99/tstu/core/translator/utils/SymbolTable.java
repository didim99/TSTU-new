package ru.didim99.tstu.core.translator.utils;

import java.util.Locale;
import ru.didim99.tstu.core.translator.Symbol;

/**
 * Created by didim99 on 05.11.18.
 */
public class SymbolTable extends HashTable<String, Symbol> {

  public int add(String name) {
    add(name, new Symbol(name));
    return indexOfKey(name);
  }

  public String symbolList() {
    StringBuilder sb = new StringBuilder();

    int index = 0;
    for (Entry<String, Symbol> e : getEntries()) {
      sb.append(String.format(Locale.US, "%3d: %s\n",
        index++, e.getValue().getName()));
    }

    return sb.toString();
  }
}
