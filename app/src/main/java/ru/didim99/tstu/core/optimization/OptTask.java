package ru.didim99.tstu.core.optimization;

import android.content.Context;
import java.util.ArrayList;
import ru.didim99.tstu.core.CallbackTask;

/**
 * Created by didim99 on 15.09.18.
 */
public class OptTask extends CallbackTask<Void, ArrayList<Result>> {

  public OptTask(Context context) {
    super(context);
  }

  @Override
  protected ArrayList<Result> doInBackgroundInternal(Void config) {
    ArrayList<Result> results = new ArrayList<>();
    ExtremaFinder finder = new ExtremaFinder();
    for (int method : ExtremaFinder.Method.ALL)
      results.add(finder.solve(method));
    return results;
  }
}
