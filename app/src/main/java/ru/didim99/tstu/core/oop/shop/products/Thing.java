package ru.didim99.tstu.core.oop.shop.products;

/**
 * Created by didim99 on 14.03.19.
 */
public class Thing extends Product {
  public Thing(String name, String vendor, int price) {
    super(name, vendor, price);
  }

  private Thing(Thing electronics) {
    super(electronics);
  }

  @Override
  public Product copyForCard() {
    return new Thing(this);
  }
}
