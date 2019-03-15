package ru.didim99.tstu.core.oop.shop.products;

import java.util.Random;

/**
 * Created by didim99 on 13.03.19.
 */
public abstract class Product {
  protected int id;
  private String name;
  private String vendor;
  private int price;
  protected int count;

  Product(String name, String vendor, int price) {
    this.id = new Random().nextInt();
    this.name = name;
    this.vendor = vendor;
    this.price = price;
    this.count = 100;
  }

  Product(Product src) {
    this.id = src.id;
    this.name = src.name;
    this.vendor = src.vendor;
    this.price = src.price;
    this.count = 1;
    src.count--;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getVendor() {
    return vendor;
  }

  public int getPrice() {
    return price;
  }

  public int getCount() {
    return count;
  }

  public int getPriceSum() {
    return count * price;
  }

  public void duplicate() {
    count++;
  }

  public abstract Product copyForCard();

  @Override
  public String toString() {
    return vendor + " " + name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Product)) return false;
    Product product = (Product) obj;
    return id == product.id;
  }
}
