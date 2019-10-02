package ru.didim99.tstu.core.optimization;

import java.util.Random;

import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 02.10.19.
 */
class PaulMethod implements OptTask.ExtremaFinderR2 {
  private static final double START_MIN = -10.0;
  private static final double START_MAX = 10.0;
  private static final double STEP = 0.1;

  @Override
  public Result find(FunctionR2 fun) {
    Result result = new Result();
    Random random = new Random();

    PointD p = new PointD(
      Utils.randInRangeD(random, START_MIN, START_MAX),
      Utils.randInRangeD(random, START_MIN, START_MAX)
    );



    return result;
  }
}
