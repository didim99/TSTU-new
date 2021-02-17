package ru.didim99.tstu.core.knowledge.neuralnet;

/**
 * Created by didim99 on 15.02.21.
 */

public class Net {
  public static final int INPUT_MIN = 0;
  public static final int INPUT_MAX = 100;
  public static final int LSPD_MIN = 0;
  public static final int LSPD_MAX = 100;
  public static final double INPUT_FACTOR = 0.01;
  public static final double LSPD_FACTOR = 0.1;

  private NetConfig config;

  public Net(NetConfig config) {
    this.config = config;
  }

  public NetConfig getConfig() {
    return config;
  }
}
