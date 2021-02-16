package ru.didim99.tstu.core.knowledge.neuralnet;

/**
 * Created by didim99 on 15.02.21.
 */

public class Net {
  private NetConfig config;

  public Net(NetConfig config) {
    this.config = config;
  }

  public NetConfig getConfig() {
    return config;
  }
}
