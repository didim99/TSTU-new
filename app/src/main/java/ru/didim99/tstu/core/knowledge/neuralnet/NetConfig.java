package ru.didim99.tstu.core.knowledge.neuralnet;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;

/**
 * Created by didim99 on 15.02.21.
 */

public class NetConfig {
  @SerializedName("inputs")
  private int inputs;
  @SerializedName("outputs")
  private int outputs;
  @SerializedName("layers")
  private int[] layers;
  @SerializedName("hasBias")
  private boolean hasBias;
  @SerializedName("learningBase")
  private List<ReferenceRule> learningBase;

  private int[] allLayers;

  public int getInputCount() {
    return inputs;
  }

  public int getOutputCount() {
    return outputs;
  }

  public boolean isHasBias() {
    return hasBias;
  }

  public List<ReferenceRule> getLearningBase() {
    return learningBase;
  }

  public int getHideLayerCount() {
    return layers.length;
  }

  public int getLayersCount() {
    return layers.length + 2;
  }

  public int getMaxLayerSize() {
    int max = hasBias ? inputs + 1 : inputs;
    max = Math.max(max, outputs);
    for (int value : layers) {
      if (hasBias) value++;
      if (value > max) max = value;
    }

    return max;
  }

  public int[] getAllLayers() {
    if (allLayers == null) {
      int size = getLayersCount();
      allLayers = new int[size];
      allLayers[0] = getInputCount();
      System.arraycopy(layers, 0, allLayers,
        1, getHideLayerCount());
      allLayers[size - 1] = getOutputCount();
    }

    return allLayers;
  }

  public boolean isEmpty() {
    return layers == null && learningBase == null;
  }

  @Override
  public String toString() {
    return "NetConfig{" +
      "inputs=" + inputs +
      ", outputs=" + outputs +
      ", layers=" + Arrays.toString(layers) +
      ", bias=" + hasBias +
      '}';
  }
}
