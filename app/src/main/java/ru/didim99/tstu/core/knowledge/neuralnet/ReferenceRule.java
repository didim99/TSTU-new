package ru.didim99.tstu.core.knowledge.neuralnet;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 15.02.21.
 */

public class ReferenceRule {
  @SerializedName("inputs")
  private double[] inputs;
  @SerializedName("outputs")
  private double[] outputs;

  public double[] inputs() {
    return inputs;
  }

  public double[] outputs() {
    return outputs;
  }

  @Override
  public String toString() {
    return
      Utils.joinStr(", ",
        Utils.doubleArrayToStringArray(inputs, 3)) +
      " -> " +
      Utils.joinStr(", ",
        Utils.doubleArrayToStringArray(outputs, 3));
  }
}
