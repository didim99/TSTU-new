package ru.didim99.tstu.core.os.expanse;

import android.graphics.Point;

/**
 * Created by didim99 on 12.04.19.
 */
public class Position extends Point {

  Position() {}
  Position(int x, int y) { this.x = x; this.y = y; }
  Position(Position src) { this.x = src.x; this.y = src.y; }

  final int distance(Position pos) {
    int dx = Math.abs(x - pos.x);
    int dy = Math.abs(y - pos.y);
    return Math.max(dx, dy);
  }

  final void offset(Position pos) {
    offset(pos.x, pos.y);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Position pos = (Position) obj;
    return x == pos.x && y == pos.y;
  }
}
