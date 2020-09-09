package ru.didim99.tstu.core.security.datatransfer;

import android.os.AsyncTask;
import java.util.ArrayList;
import java.util.Random;
import ru.didim99.tstu.core.security.datatransfer.encoder.CRCEncoder;
import ru.didim99.tstu.core.security.datatransfer.encoder.Encoder;
import ru.didim99.tstu.core.security.datatransfer.encoder.HammingEncoder;
import ru.didim99.tstu.core.security.datatransfer.encoder.SingleBitEncoder;
import ru.didim99.tstu.utils.MyLog;

/**
 * Created by didim99 on 21.04.19.
 */
public class DataExchanger {
  private static final long INTERVAL = 2000;
  private static final boolean DEFAULT_NOISE = false;
  private static final int DEFAULT_TYPE = Type.SINGLE_BIT;
  private static final int DEFAULT_LENGTH = 5;
  private static final int DEFAULT_BB = 1;
  public static final int MIN_BB = 1;
  public static final int MIN_ML = 1;
  public static final int MAX_ML = 10;

  private static final class Type {
    private static final int SINGLE_BIT  = 0;
    private static final int CRC         = 1;
    private static final int HAMMING     = 2;
  }

  public static final class State {
    public static final int IDLE    = 0;
    public static final int RUNNING = 1;
    public static final int PAUSED  = 2;
  }

  // Configuration
  private int codeType;
  private int messageLength;
  private boolean useNoise;
  private int brokenBlocks;
  private int brokenBits;
  // Workflow
  private TransferTask task;
  private EventListener listener;
  private DataChannel channel;
  private Encoder encoder;
  private Random random;
  private String txInfo;
  private String rxInfo;

  public DataExchanger() {
    this.random = new Random();
    this.channel = new DataChannel();
    onCodeTypeChanged(DEFAULT_TYPE);
    this.messageLength = DEFAULT_LENGTH;
    this.brokenBlocks = DEFAULT_BB;
    this.brokenBits = DEFAULT_BB;
    this.useNoise = DEFAULT_NOISE;
    initTask();
  }

  public int getDataBits() {
    return encoder.dataBits();
  }

  public String getCodeInfo() {
    return encoder.codeInfo();
  }

  public void configure(int msgLen, int brBlocks, int brBits, boolean noise) {
    channel.configure(brokenBlocks = brBlocks, brokenBits = brBits);
    channel.setHasNoise(useNoise = noise);
    messageLength = msgLen;
  }

  public void start() {
    task.start();
    publishState();
  }

  public void pause(boolean paused) {
    task.pause(paused);
    publishState();
  }

  public void togglePause() {
    pause(!task.paused);
  }

  public void finish() {
    task.finish();
  }

  public int getCodeType() {
    return codeType;
  }

  public int getMessageLength() {
    return messageLength;
  }

  public boolean isUseNoise() {
    return useNoise;
  }

  public int getBrokenBlocks() {
    return brokenBlocks;
  }

  public int getBrokenBits() {
    return brokenBits;
  }

  public boolean isRunning() {
    return task.running;
  }

  public void setEventListener(EventListener listener) {
    this.listener = listener;
    publishState();
  }

  public void onCodeTypeChanged(int codeType) {
    switch (this.codeType = codeType) {
      case Type.SINGLE_BIT:
        encoder = new SingleBitEncoder();
        break;
      case Type.CRC:
        encoder = new CRCEncoder();
        break;
      case Type.HAMMING:
        encoder = new HammingEncoder();
        break;
    }
  }

  private void initTask() {
    task = new TransferTask(this);
  }

  private void doTransfer() {
    ArrayList<Integer> src = generate(messageLength);
    ArrayList<Integer> tmp = new ArrayList<>(messageLength);
    StringBuilder sb = new StringBuilder();
    int buffer;

    for (Integer frame : src) {
      tmp.add(encoder.encode(frame, sb));
      sb.append("\n");
    }

    txInfo = sb.toString();
    channel.transfer(tmp, encoder.dataBits());
    sb = new StringBuilder();
    int pos = 0;

    for (Integer frame : tmp) {
      try {
        buffer = encoder.decode(frame, sb);
        if (src.get(pos) != buffer)
          sb.append(" [FAILED]");
      } catch (Encoder.ProcessException ignored) {
      } finally { pos++; }
      sb.append("\n");
    }

    rxInfo = sb.toString();
  }

  private ArrayList<Integer> generate(int length) {
    ArrayList<Integer> data = new ArrayList<>(length);
    while (length-- > 0) data.add(encoder.generate(random));
    return data;
  }

  private void publishState() {
    if (listener == null) return;
    int state = task.running ? (task.paused ?
      State.PAUSED : State.RUNNING) : State.IDLE;
    listener.OnExchangerStateChanged(state);
  }

  private void publishProgress() {
    listener.onPackageTransferred(txInfo, rxInfo);
  }

  private static class TransferTask extends AsyncTask<Void, Void, Void> {
    private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_TTask";

    private boolean running, paused;
    private DataExchanger exchanger;

    TransferTask(DataExchanger exchanger) {
      this.exchanger = exchanger;
      this.running = false;
      this.paused = false;
    }

    @Override
    protected Void doInBackground(Void... voids) {
      try {
        exchanger.doTransfer();
        publishProgress();
        while (running) {
          Thread.sleep(INTERVAL);
          if (paused) continue;
          exchanger.doTransfer();
          publishProgress();
        }
      } catch (InterruptedException e) {
        MyLog.e(LOG_TAG, "Interrupted by: " + e);
      }

      return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
      exchanger.publishProgress();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
      exchanger.publishState();
      exchanger.initTask();
      exchanger = null;
    }

    public void start() {
      this.running = true;
      this.paused = false;
      execute();
    }

    public void pause(boolean paused) {
      this.paused = paused;
    }

    public void finish() {
      running = false;
    }
  }

  public interface EventListener {
    void OnExchangerStateChanged(int state);
    void onPackageTransferred(String txInfo, String rxInfo);
  }
}
