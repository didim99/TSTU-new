package ru.didim99.tstu.core.oop.shop.products;

/**
 * Created by didim99 on 14.03.19.
 */
public class Clothes extends Product {
  public Clothes(String name, String vendor, int price) {
    super(name, vendor, price);
  }

  private Clothes(Clothes electronics) {
    super(electronics);
  }

  @Override
  public Product copyForCard() {
    return new Clothes(this);
  }
}
