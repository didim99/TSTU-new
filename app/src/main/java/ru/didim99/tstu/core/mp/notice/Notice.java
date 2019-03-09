package ru.didim99.tstu.core.mp.notice;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by didim99 on 08.03.19.
 */
public class Notice {
  private int id;
  private long datetime;
  private String header;
  private String text;

  public Notice() {}

  public Notice(long datetime, String header, String text) {
    this.datetime = datetime;
    this.header = header;
    this.text = text;
  }

  public int getId() {
    return id;
  }

  public long getDatetime() {
    return datetime;
  }

  public String getHeader() {
    return header;
  }

  public String getText() {
    return text;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setDatetime(long datetime) {
    this.datetime = datetime;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String toString(SimpleDateFormat df) {
    return df.format(new Date(datetime)) + ": " + header;
  }

  @Override
  public String toString() {
    return id + ": " + header;
  }
}
