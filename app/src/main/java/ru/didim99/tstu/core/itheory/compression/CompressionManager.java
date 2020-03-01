package ru.didim99.tstu.core.itheory.compression;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import ru.didim99.tstu.core.CallbackTask;

/**
 * Created by didim99 on 28.02.20.
 */
public class CompressionManager implements CallbackTask.EventListener<Boolean> {
  public enum Event { TYPE_CHANGED, OPERATION_START, OPERATION_END }

  private int type;
  private Context appContext;
  private EventListener listener;
  private Compressor compressor;
  private String inFile, inFileName;
  private String message, fileMessage;
  private byte[] fileData;
  private String outFile;

  public CompressionManager(Context appContext) {
    this.appContext = appContext;
    setCompressionType(Compressor.Type.HUFFMAN);
  }

  public int getCompressionType() {
    return type;
  }

  public Compressor getCompressor() {
    return compressor;
  }

  public String getInFileName() {
    return inFileName;
  }

  public String getMessage() {
    return message;
  }

  String getInFile() {
    return inFile;
  }

  String getOutFile() {
    return outFile;
  }

  byte[] getFileData() {
    return fileData;
  }

  public void setEventListener(EventListener listener) {
    this.listener = listener;
  }

  public void setCompressionType(int type) {
    this.type = type;
    switch (type) {
      case Compressor.Type.HUFFMAN:
        compressor = new HuffmanCompressor();
        break;
      case Compressor.Type.ARITHMETIC:
        compressor = new ArithmeticCompressor();
        break;
    }

    publishEvent(Event.TYPE_CHANGED);
  }

  public void setMessage(String message) {
    this.message = message.isEmpty() ? fileMessage : message;
  }

  void setFileMessage(String fileMessage) {
    this.fileMessage = fileMessage;
    this.message = fileMessage;
    this.fileData = null;
  }

  void setFileData(byte[] fileData) {
    this.fileData = fileData;
    this.fileMessage = null;
    this.message = null;
  }

  public void loadFile(String path) {
    this.inFile = path;
    this.inFileName = new File(path).getName();
    this.inFileName = inFileName.substring(0,
      inFileName.lastIndexOf(Compressor.EXT_SEP));
    startTask(CompressionTask.Action.LOAD_FILE);
    this.compressor.clearBuffers();
  }

  public void saveToFile(String path) {
    this.outFile = path;
    startTask(CompressionTask.Action.SAVE_FILE);
  }

  public void start() {
    if ((message == null || message.isEmpty())
      && (fileData == null || fileData.length == 0)) return;
    startTask(CompressionTask.Action.PROCESS);
  }

  public void process() throws IOException {
    if (message != null && !message.isEmpty())
      fileData = compressor.compress(message);
    else if (fileData != null && fileData.length > 0)
      fileMessage = message = compressor.decompress(fileData);
  }

  private void startTask(CompressionTask.Action action) {
    CompressionTask task = new CompressionTask(appContext, this);
    task.registerEventListener(this);
    task.execute(action);
  }

  @Override
  public void onTaskEvent(CallbackTask.Event event, Boolean data) {
    publishEvent(event == CallbackTask.Event.START ?
      Event.OPERATION_START : Event.OPERATION_END);
  }

  private void publishEvent(Event event) {
    if (listener != null) listener.onCompressionEvent(event);
  }

  public interface EventListener {
    void onCompressionEvent(Event event);
  }
}
