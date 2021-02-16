package ru.didim99.tstu.core.knowledge.neuralnet;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by didim99 on 15.02.21.
 */

class NetConfig {
  @SerializedName("inputs")
  private int inputs;
  @SerializedName("outputs")
  private int outputs;
  @SerializedName("layers")
  private int[] layers;
  @SerializedName("learningBase")
  private List<ReferenceRule> learningBase;

  public int getInputCount() {
    return inputs;
  }

  public int getOutputCount() {
    return outputs;
  }

  public int getLayerCount() {
    return layers.length;
  }

  public int getLayerItemCount(int layerIndex) {
    if (layerIndex < 0)
      throw new IllegalArgumentException("layerIndex must be > 0 (got " + layerIndex + ")");
    if (layerIndex > layers.length)
      throw new IllegalArgumentException("layerIndex must be < "
        + layers.length + " (got " + layerIndex + ")");
    return layers[layerIndex];
  }

  public List<ReferenceRule> getLearningBase() {
    return learningBase;
  }
}
