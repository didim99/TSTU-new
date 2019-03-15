package ru.didim99.tstu.core.oop.shop.products;

/**
 * Created by didim99 on 14.03.19.
 */
public class Food extends Product {
  public Food(String name, String vendor, int price) {
    super(name, vendor, price);
  }

  private Food(Food electronics) {
    super(electronics);
  }

  @Override
  public Product copyForCard() {
    return new Food(this);
  }

  public boolean eat() {
    return --count > 0;
  }
}
