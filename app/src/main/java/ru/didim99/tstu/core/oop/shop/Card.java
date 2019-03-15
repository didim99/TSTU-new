package ru.didim99.tstu.core.oop.shop;

import java.util.ArrayList;
import ru.didim99.tstu.core.oop.shop.products.Product;

/**
 * Created by didim99 on 13.03.19.
 */
public class Card {
  private ArrayList<Product> products;
  private int priceSum;

  Card() {
    products = new ArrayList<>();
  }

  public boolean hasItems() {
    return !products.isEmpty();
  }

  public void addProduct(Product p) {
    int index = products.indexOf(p);
    if (index < 0) {
      Product product = p.copyForCard();
      products.add(product);
    } else
      products.get(index).duplicate();
    priceSum += p.getPrice();
  }

  public void removeProduct(Product p) {
    int index = products.indexOf(p);
    if (index >= 0) {
      Product product = products.get(index);
      priceSum -= product.getPriceSum();
      products.remove(product);
    }
  }

  public ArrayList<Product> getProducts() {
    return products;
  }

  ArrayList<Product> takeout() {
    ArrayList<Product> all = new ArrayList<>(products);
    products.clear();
    priceSum = 0;
    return all;
  }

  public int getSum() {
    return priceSum;
  }

  public void clear() {
    products.clear();
  }
}
