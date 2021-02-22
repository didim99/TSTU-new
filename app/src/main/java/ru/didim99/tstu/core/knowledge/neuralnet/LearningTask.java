package ru.didim99.tstu.core.knowledge.neuralnet;

import android.content.Context;
import java.util.ArrayList;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.math.common.PointD;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 22.02.21.
 */

public class LearningTask extends CallbackTask<Net, Void> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_NNLT";

  private LearningProgressListener listener;
  private ArrayList<PointD> learningCurve;

  public LearningTask(Context context) {
    super(context);
  }

  public void setLearningProgressListener(LearningProgressListener listener) {
    this.listener = listener;
  }

  @Override
  protected Void doInBackgroundInternal(Net net) {
    learningCurve = new ArrayList<>();
    double error = 1.0;
    int cycle = 0;

    while (error > Net.LEARNING_THRESHOLD) {
      error = 0.0;
      for (ReferenceRule rule : net.getConfig().getLearningBase()) {
        net.setInputValues(rule.inputs());
        net.calculateOutput();
        error += net.calculateError(rule.outputs());
        publishProgress();
        net.propagateError();
        net.correctWeights();
      }

      MyLog.d(LOG_TAG, "cycle: " + cycle + ", err: " + error);
      learningCurve.add(new PointD((double) cycle, error));
      cycle++;
    }

    return null;
  }

  @Override
  protected void onProgressUpdate(Void... values) {
    if (listener != null)
      listener.onLearningStep();
  }

  public ArrayList<PointD> getLearningCurve() {
    return learningCurve;
  }

  public interface LearningProgressListener {
    void onLearningStep();
  }
}
