package ru.didim99.tstu.core.math.modeling.poxipol;

import java.util.ArrayList;
import java.util.List;
import ru.didim99.tstu.core.math.common.GenericFunctionRN;
import ru.didim99.tstu.core.math.common.PointD;

/**
 * Differential equations system
 * Created by didim99 on 02.10.20.
 */

public abstract class DiffSystem<T extends PointD> {

  protected T point;
  protected List<Entry<T>> system;

  public DiffSystem() {
    this.system = new ArrayList<>();
  }

  public T getPoint() {
    return point;
  }

  protected abstract T getStartPoint();
  protected abstract boolean completed();
  protected abstract double getStep();

  public void integrate(StepListener listener) {
    double step = getStep();
    point = getStartPoint();
    T prev = getStartPoint();

    while (!completed()) {
      for (Entry<T> e : system) {
        int index = e.targetIndex;
        double val = e.function.f(prev);
        if (!e.isDelta) prev.set(index, val);
        else point.add(index, val * step);
      }

      if (listener != null)
        listener.onStep(prev.copy());
      prev.set(point);
      point.add(0, step);
    }
  }

  public static class Entry<T extends PointD> {
    private final GenericFunctionRN<T> function;
    private final int targetIndex;
    private final boolean isDelta;

    public Entry(Enum<?> targetIndex, boolean isDelta, GenericFunctionRN<T> function) {
      this.targetIndex = targetIndex.ordinal();
      this.isDelta = isDelta;
      this.function = function;
    }
  }

  public interface StepListener {
    void onStep(PointD point);
  }
}
