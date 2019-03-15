package ru.didim99.tstu.core.oop.shop;

import java.util.ArrayList;
import ru.didim99.tstu.core.oop.shop.products.Product;

/**
 * Created by didim99 on 13.03.19.
 */
public class Division {
  private ArrayList<Product> products;
  private String name;

  public Division(String name) {
    products = new ArrayList<>();
    this.name = name;
  }

  public ArrayList<Product> getProducts() {
    return products;
  }

  public String getName() {
    return name;
  }
}
