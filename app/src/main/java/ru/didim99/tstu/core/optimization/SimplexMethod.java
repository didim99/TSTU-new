package ru.didim99.tstu.core.optimization;

import java.util.Random;

import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 03.10.19.
 */
class SimplexMethod implements OptTask.ExtremaFinderR2 {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_Simplex";

  private static final double EPSILON = 0.001;
  private static final double START_MIN = -10.0;
  private static final double START_MAX = 10.0;
  private static final double START_RANGE = 1.0;
  private static final double DEFAULT_STEP = 0.1;

  @Override
  public Result find(FunctionR2 fun) {
    Result result = new Result();
    Random random = new Random();

    PointD start1 = new PointD(
      Utils.randInRangeD(random, START_MIN, START_MAX),
      Utils.randInRangeD(random, START_MIN, START_MAX));
    PointD start2 = new PointD(
      Utils.randInRangeD(random, start1.get(0) - START_RANGE, start1.get(0) + START_RANGE),
      Utils.randInRangeD(random, start1.get(1) - START_RANGE, start1.get(1) + START_RANGE));
    PointD start3 = new PointD(
      Utils.randInRangeD(random, start1.get(0) - START_RANGE, start1.get(0) + START_RANGE),
      Utils.randInRangeD(random, start1.get(1) - START_RANGE, start1.get(1) + START_RANGE));

    

    return result;
  }
}
