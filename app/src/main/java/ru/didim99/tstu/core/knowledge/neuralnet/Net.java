package ru.didim99.tstu.core.knowledge.neuralnet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.didim99.tstu.core.math.common.PointD;
import ru.didim99.tstu.core.math.common.Matrix;

/**
 * Created by didim99 on 15.02.21.
 */

public class Net {
  public static final String FILE_MASK = ".json";
  public static final double LEARNING_THRESHOLD = 0.001;
  private static final double DEFAULT_LEARN_SPD = 1.0;
  private static final double RANDOM_MIN = -1.0;
  private static final double RANDOM_MAX = 1.0;

  public static final int INPUT_MIN = 0;
  public static final int INPUT_MAX = 100;
  public static final int LSPD_MIN = 1;
  public static final int LSPD_MAX = 100;
  public static final double INPUT_FACTOR = 0.01;
  public static final double LSPD_FACTOR = 0.1;

  private final NetConfig config;
  private double learnSpeed;

  private final List<Matrix> weights;
  private final List<LayerCache> layers;
  private double minWeight, maxWeight;

  public Net(NetConfig config) {
    this.config = config;
    this.layers = new ArrayList<>();
    this.weights = new ArrayList<>();
    this.learnSpeed = DEFAULT_LEARN_SPD;

    initLayers();
    randomizeWeights();
    calculateOutput();
  }

  public NetConfig getConfig() {
    return config;
  }

  public double getLearnSpeed() {
    return learnSpeed;
  }

  public double getInput(int index) {
    return layers.get(0).out.get(index);
  }

  public double getNeuronOutput(int layer, int neuron) {
    LayerCache layerCache = layers.get(layer);
    if (neuron == layerCache.size()) return 1.0;
    else return layerCache.out.get(neuron);
  }

  public double getWeight(int layer, int neuron, int input) {
    return weights.get(layer - 1).get(neuron, input);
  }

  public double getMinWeight() {
    return minWeight;
  }

  public double getMaxWeight() {
    return maxWeight;
  }

  public PointD getOutputValues() {
    return layers.get(lastLayerIndex()).out.copy();
  }

  public void setLearnSpeed(double learnSpeed) {
    this.learnSpeed = learnSpeed;
  }

  public void setInputValue(int index, double value) {
    LayerCache layer = layers.get(0);
    layer.out.set(index, value);
  }

  public void setInputValues(double... values) {
    for (int i = 0; i < layers.get(0).size(); i++)
      setInputValue(i, values[i]);
  }

  private void initLayers() {
    int[] allLayers = config.getAllLayers();
    for (int l = 0; l < allLayers.length; l++) {
      layers.add(new LayerCache(allLayers[l]));

      if (l > 0) {
        int inputs = layers.get(l-1).size();
        if (config.isHasBias()) inputs++;
        Matrix matrix = new Matrix("layer" + l,
          allLayers[l], inputs);
        weights.add(matrix);
      }
    }
  }

  public void randomizeWeights() {
    Random random = new Random();
    for (Matrix layer : weights)
      layer.randomize(random, RANDOM_MIN, RANDOM_MAX);
    updateWeightsRange();
  }

  private void updateWeightsRange() {
    minWeight = 0.0;
    maxWeight = 0.0;
    for (Matrix layer : weights) {
      double pos = layer.maxDeviation(true);
      double neg = layer.maxDeviation(false);
      if (pos > maxWeight) maxWeight = pos;
      if (neg < minWeight) minWeight = neg;
    }
  }

  public void calculateOutput() {
    for (int l = 1; l < layers.size(); l++) {
      LayerCache layer = layers.get(l);
      LayerCache inputs = layers.get(l - 1);

      for (int n = 0; n < layer.size(); n++) {
        double value = 0;
        for (int i = 0; i < inputs.size(); i++)
          value += inputs.out.get(i) * getWeight(l, n, i);
        if (config.isHasBias())
          value += getWeight(l, n, inputs.size());
        value = sigma(value);
        layer.dOut.set(n, dSigma(value));
        layer.out.set(n, value);
      }
    }
  }

  public double calculateError(double[] reference) {
    LayerCache outputs = layers.get(lastLayerIndex());

    double err, sum = 0;
    for (int i = 0; i < outputs.size(); i++) {
      err = outputs.out.get(i) - reference[i];
      outputs.delta.set(i, err * outputs.dOut.get(i));
      sum += err * err;
    }

    return sum / 2;
  }

  public void propagateError() {
    for (int l = lastLayerIndex() - 1; l > 0; l--) {
      LayerCache layer = layers.get(l);

      layer.delta.set(weights.get(l)
        .multiplyTransposed(layers.get(l + 1).delta));
      layer.delta = layer.delta.mul(layer.dOut);
    }
  }

  public void correctWeights() {
    for (int l = 1; l < layers.size(); l++) {
      LayerCache cache = layers.get(l);
      Matrix layer = weights.get(l - 1);

      for (int n = 0; n < layer.getRowCount(); n++) {
        for (int i = 0; i < layer.getColumnCount(); i++) {
          layer.add(n, i, -learnSpeed
            * cache.delta.get(n) * getNeuronOutput(l - 1, i));
        }
      }
    }

    updateWeightsRange();
  }

  private static double sigma(double x) {
    return 1.0 / (1.0 + Math.exp(-x));
  }

  private static double dSigma(double x) {
    return x * (1 - x);
  }

  private int lastLayerIndex() {
    return layers.size() - 1;
  }

  @Override
  public String toString() {
    return config.toString();
  }
}
