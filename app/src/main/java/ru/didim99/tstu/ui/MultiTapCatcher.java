package ru.didim99.tstu.ui;

import android.view.View;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Timer;

/**
 * Created by didim99 on 15.11.18.
 */
public class MultiTapCatcher implements View.OnClickListener {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_MTC";
  private static final int THRESHOLD = 300;

  private MultiTapEventListener eventListener;
  private View.OnClickListener clickListener;
  private View view;
  private Timer timer;
  private int taps, maxTaps;

  public MultiTapCatcher() {
    this.timer = new Timer();
    this.taps = 0;
  }

  public void attachToView(View v, int maxTaps, MultiTapEventListener listener) {
    attachToView(v, maxTaps, listener, null);
  }

  void attachToView(View v, int maxTaps, MultiTapEventListener listener,
                    View.OnClickListener clickListener) {
    this.eventListener = listener;
    this.maxTaps = maxTaps;
    this.taps = 0;
    if (view != null)
      view.setOnClickListener(this.clickListener);
    this.view = v;
    this.clickListener = clickListener;
    view.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    if (taps > 0) {
      timer.stop();
      if (timer.getMillis() < THRESHOLD) {
        if (++taps == maxTaps) {
          MyLog.d(LOG_TAG, "Catched multi-tap event: " + v);
          eventListener.onMultiTapEvent(v);
          taps = 0;
        }
      } else
        taps = 1;
    } else
      taps++;

    timer.start();
    if (clickListener != null)
      clickListener.onClick(v);
  }

  public interface MultiTapEventListener {
    void onMultiTapEvent(View v);
  }
}
