package ru.didim99.tstu.core.os.expanse;

/**
 * Created by didim99 on 11.04.19.
 */
public class GameField {

  static final class Direction {
    static final int UP     = 0x00000001;
    static final int RIGHT  = 0x00000002;
    static final int DOWN   = 0x00000004;
    static final int LEFT   = 0x00000008;
    static final int[] ANY = { UP, RIGHT, DOWN, LEFT };
    static final Position[] OFFSET = {
      new Position(0, -1), new Position(1, 0),
      new Position(0, 1), new Position(-1, 0)
    };
  }

  private Cell[] data;
  private int width, height;
  private int maxX, maxY;
  private int emptyCells;

  GameField(int width, int height) {
    this.width = width;
    this.height = height;
    this.maxX = width - 1;
    this.maxY = height - 1;
    data = new Cell[width * height];
    emptyCells = getArea();

    for (int i = 0; i < data.length; i++)
      data[i] = new Cell();
  }

  public void clear() {
    for (Cell cell : data)
      cell.status = Cell.EMPTY;
  }

  public Cell getCell(int x, int y) {
    return data[y * width + x];
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getArea() {
    return width * height;
  }

  public int getProgress() {
    return getArea() - emptyCells;
  }

  boolean hasEmptyCells() {
    return emptyCells > 0;
  }

  void acquire(Position pos, Player p) {
    getCell(pos.x, pos.y).setColor(p.getColor());
    emptyCells--;
  }

  int isBoundary(Position pos) {
    int res = 0;
    if (pos.x > 0 && getCell(pos.x - 1, pos.y).isEmpty()) res |= Direction.LEFT;
    if (pos.y > 0 && getCell(pos.x, pos.y - 1).isEmpty()) res |= Direction.UP;
    if (pos.x < maxX && getCell(pos.x + 1, pos.y).isEmpty()) res |= Direction.RIGHT;
    if (pos.y < maxY && getCell(pos.x, pos.y + 1).isEmpty()) res |= Direction.DOWN;
    return res;
  }

  @Override
  public String toString() {
    return "GameField " + width + "x" + height
      + " (empty: " + emptyCells + "/" + getArea() + ")";
  }

  public static class Cell {
    private static final int EMPTY = 0;
    private int status = EMPTY;

    public boolean isEmpty() {
      return status == EMPTY;
    }

    public int getColor() {
      return status;
    }

    private void setColor(int color) {
      this.status = color;
    }
  }
}
