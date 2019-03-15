package ru.didim99.tstu.core.oop.shop.products;

/**
 * Created by didim99 on 14.03.19.
 */
public class Electronics extends Product {
  public Electronics(String name, String vendor, int price) {
    super(name, vendor, price);
  }

  private Electronics(Electronics electronics) {
    super(electronics);
  }

  @Override
  public Product copyForCard() {
    return new Electronics(this);
  }
}
