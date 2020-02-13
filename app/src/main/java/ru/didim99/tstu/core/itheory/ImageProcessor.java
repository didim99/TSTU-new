package ru.didim99.tstu.core.itheory;

/**
 * Created by didim99 on 13.02.20.
 */
public class ImageProcessor {
  public static final int MIN_SIZE = 4;
  public static final int MAX_SIZE = 512;
  public enum State { INITIAL, LOADING, READY, PROCESSING, SAVING }

  private StateChangeListener listener;
  private State state;

  public ImageProcessor() {
    applyState(State.INITIAL);
  }

  public void setStateChangeListener(StateChangeListener listener) {
    this.listener = listener;
    publishState();
  }

  private void applyState(State state) {
    this.state = state;
    publishState();
  }

  private void publishState() {
    if (listener != null) listener.onStateChanged(state);
  }

  public interface StateChangeListener {
    void onStateChanged(State state);
  }
}
