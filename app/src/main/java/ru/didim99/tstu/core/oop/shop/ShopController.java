package ru.didim99.tstu.core.oop.shop;

import android.content.Context;
import java.util.ArrayList;
import ru.didim99.tstu.core.oop.shop.model.ShopLoader;
import ru.didim99.tstu.core.oop.shop.products.Product;

/**
 * Created by didim99 on 13.03.19.
 */
public class ShopController {
  private ArrayList<Product> inventory;
  private ArrayList<Shop> shopList;
  private int currentShop;
  private Card card;
  private Bank bank;

  public ShopController() {
    inventory = new ArrayList<>();
    card = new Card();
    bank = new Bank();
    currentShop = 0;
  }

  public void load(Context ctx, String path, OnLoadListener listener) {
    ShopLoader loader = new ShopLoader(ctx);
    loader.registerEventListener((e, d) -> {
      if (d != null) { shopList = d; listener.onDataLoaded(); } });
    loader.execute(path);
  }

  public Shop selectShop(int id) {
    return shopList.get(currentShop = id);
  }

  public Shop getCurrentShop() {
    return shopList.get(currentShop);
  }

  public ArrayList<String> getShopList() {
    ArrayList<String> list = new ArrayList<>();
    for (Shop shop : shopList)
      list.add(shop.getName());
    return list;
  }

  public void processBuy() {
    bank.purchase(card.getSum());
    ArrayList<Product> products = card.takeout();
    for (Product p : products) {
      int id = inventory.indexOf(p);
      if (id < 0) inventory.add(p);
      else inventory.get(id).duplicate();
    }
  }

  public ArrayList<Product> getInventory() {
    return inventory;
  }

  public Card getCard() {
    return card;
  }

  public Bank getBank() {
    return bank;
  }

  @FunctionalInterface
  public interface OnLoadListener {
    void onDataLoaded();
  }
}
