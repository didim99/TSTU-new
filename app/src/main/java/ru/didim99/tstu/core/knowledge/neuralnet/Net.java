package ru.didim99.tstu.core.knowledge.neuralnet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import ru.didim99.tstu.core.math.numeric.Matrix;

/**
 * Created by didim99 on 15.02.21.
 */

public class Net {
  public static final String FILE_MASK = ".json";
  private static final double DEFAULT_LEARN_SPD = 1.0;

  public static final int INPUT_MIN = 0;
  public static final int INPUT_MAX = 100;
  public static final int LSPD_MIN = 0;
  public static final int LSPD_MAX = 100;
  public static final double INPUT_FACTOR = 0.01;
  public static final double LSPD_FACTOR = 0.1;

  private final NetConfig config;
  private double learnSpeed;

  private final List<Matrix> weights;
  private final List<List<Double>> layers;

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
    return layers.get(0).get(index);
  }

  public double getNeuronOutput(int layer, int neuron) {
    List<Double> layerCache = layers.get(layer);
    if (neuron == layerCache.size()) return 1.0;
    else return layerCache.get(neuron);
  }

  public double getWeight(int layer, int neuron, int input) {
    return weights.get(layer - 1).get(neuron, input);
  }

  public List<Double> getOutputValues() {
    return layers.get(layers.size() - 1);
  }

  public void setLearnSpeed(double learnSpeed) {
    this.learnSpeed = learnSpeed;
  }

  public void setInputValue(int index, double value) {
    layers.get(0).set(index, value);
  }

  private void initLayers() {
    int[] allLayers = config.getAllLayers();
    for (int l = 0; l < allLayers.length; l++) {
      List<Double> layer = new ArrayList<>(allLayers[l]);
      for (int i = 0; i < allLayers[l]; i++)
        layer.add(0.0);
      layers.add(layer);

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
      layer.randomize(random);
  }

  public void calculateOutput() {
    for (int l = 1; l < layers.size(); l++) {
      List<Double> layer = layers.get(l);
      List<Double> inputs = layers.get(l - 1);

      for (int n = 0; n < layer.size(); n++) {
        double value = 0;
        for (int i = 0; i < inputs.size(); i++)
          value += inputs.get(i) * getWeight(l, n, i);
        if (config.isHasBias())
          value += getWeight(l, n, inputs.size());
        layer.set(n, sigma(value));
      }
    }
  }

  private static double sigma(double x) {
    return 1.0 / (1.0 + Math.exp(-x));
  }

  @Override
  public String toString() {
    return config.toString();
  }
}
