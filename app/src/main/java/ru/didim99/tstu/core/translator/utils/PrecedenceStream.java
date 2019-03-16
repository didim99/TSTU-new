package ru.didim99.tstu.core.translator.utils;

import java.util.ArrayList;
import ru.didim99.tstu.core.translator.LangMap;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 03.11.18.
 */
public class PrecedenceStream extends SyntaxStream {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_PStream";

  private PrecedenceTable pt = PrecedenceTable.getInstance();

  public PrecedenceStream(ArrayList<Integer> input) {
    super(input);
  }

  public int nextPrecedence() {
    if (!hasNextPrecedence())
      return PrecedenceTable.RT.HIGHER;

    Integer l1 = next();
    if (isCustom(l1)) next();
    int rel = pt.get(l1, pickNext());
    MyLog.v(LOG_TAG, "Precedence "
      + "[" + rel + "] " + l1 + " -> " + pickNext());
    if (rel == PrecedenceTable.RT.NONE)
      validateNext(LangMap.INTERNAL.UNKNOWN);
    return rel;
  }

  public ArrayList<Integer> removeRange(int start, int end) {
    if (start < 0 || start >= size)
      throw new IllegalArgumentException("Invalid start index: " + start);
    if (end < 0 || end >= size)
      throw new IllegalArgumentException("Invalid end index: " + end);
    int count = end - start + 1;
    if (count <= 0)
      throw new IllegalArgumentException("Invalid range: " + start + "..." + end);

    MyLog.v(LOG_TAG, "Before: " + inputStream);
    ArrayList<Integer> range = new ArrayList<>();
    int realStart = 0, realEnd;

    moveToStart();
    int factStart = start;
    for (Integer l : inputStream) {
      if (l != LangMap.INTERNAL.NEWLINE) {
        if (pos >= factStart) {
          if (pos == factStart)
            realStart = realPos;
          if (isCustom(l))
            count++;
          if (count-- == 0)
            break;
          range.add(l);
        } else if (isCustom(l))
          factStart++;
        pos++;
      } else
        lineNum++;
      realPos++;
    }

    realEnd = realPos;
    for (int i = realEnd - 1; i >= realStart; i--) {
      if (inputStream.get(i) != LangMap.INTERNAL.NEWLINE)
        inputStream.remove(i);
    }

    calcSize();
    MyLog.d(LOG_TAG, "Removing range: " + start + "..." + end
      + " [" + realStart + "..." + (realEnd - 1) + "]");
    MyLog.v(LOG_TAG, "After: " + inputStream);
    return range;
  }

  private boolean hasNextPrecedence() {
    return pos + 2 <= size;
  }

  private static boolean isCustom(Integer l) {
    return (l & LangMap.MASK.CUSTOM) > 0;
  }
}
