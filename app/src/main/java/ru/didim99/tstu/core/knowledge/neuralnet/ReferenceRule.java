package ru.didim99.tstu.core.knowledge.neuralnet;

import com.google.gson.annotations.SerializedName;

/**
 * Created by didim99 on 15.02.21.
 */

class ReferenceRule {
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
}
