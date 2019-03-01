package ru.didim99.tstu.core.mp.radio;

/**
 * Created by didim99 on 01.03.19.
 */
public class Song {
  private int id;
  private String singer;
  private String name;
  private long date;

  Song() {}

  Song(RadioResponse response) {
    if (!response.isSuccessful())
      throw new IllegalArgumentException("Response must be successful");
    String[] info = response.getInfo().split(" - ");
    this.date = System.currentTimeMillis();
    this.singer = info[0];
    this.name = info[1];
  }

  void setId(int id) {
    this.id = id;
  }

  void setSinger(String singer) {
    this.singer = singer;
  }

  void setName(String name) {
    this.name = name;
  }

  void setDate(long date) {
    this.date = date;
  }

  public int getId() {
    return id;
  }

  public String getSinger() {
    return singer;
  }

  public String getName() {
    return name;
  }

  public long getDate() {
    return date;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (this == obj) return true;
    if (!(obj instanceof Song)) return false;
    Song other = (Song) obj;
    return other.name.equals(name)
      && other.singer.equals(singer);
  }

  @Override
  public String toString() {
    return singer + " - " + name;
  }
}
