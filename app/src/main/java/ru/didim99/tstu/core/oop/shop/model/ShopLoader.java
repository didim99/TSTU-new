package ru.didim99.tstu.core.oop.shop.model;

import android.content.Context;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import ru.didim99.tstu.core.CallbackTask;
import ru.didim99.tstu.core.oop.shop.Division;
import ru.didim99.tstu.core.oop.shop.Shop;
import ru.didim99.tstu.core.oop.shop.products.Clothes;
import ru.didim99.tstu.core.oop.shop.products.Electronics;
import ru.didim99.tstu.core.oop.shop.products.Food;
import ru.didim99.tstu.core.oop.shop.products.Product;
import ru.didim99.tstu.core.oop.shop.products.Thing;
import ru.didim99.tstu.utils.MyLog;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 14.03.19.
 */
public class ShopLoader extends CallbackTask<String, ArrayList<Shop>> {
  private static final String LOG_TAG = MyLog.LOG_TAG_BASE + "_ShopLoader";
  private static final String ID_SEP = "::";
  private static final String ID_SHOP = "shop";
  private static final String ID_DIV = "div";
  private static final String ID_PROD = "prod";
  private static final String ID_FOOD = "food";
  private static final String ID_CLOTHES = "clothes";
  private static final String ID_DEV = "dev";
  private static final String ID_OTHER = "other";

  public ShopLoader(Context context) {
    super(context);
  }

  @Override
  protected ArrayList<Shop> doInBackgroundInternal(String path) {
    MyLog.d(LOG_TAG, "Loading shop database...");
    ArrayList<Shop> result = new ArrayList<>();
    Gson parser = new Gson();
    ArrayList<String> data;

    try {
      data = Utils.readFile(path);
    } catch (IOException e) {
      MyLog.e(LOG_TAG, "Can't read file: " + e);
      return null;
    }

    String[] parts;
    for (String s : data) {
      parts = s.split(ID_SEP);
      switch (parts[0]) {
        case ID_SHOP:
          ShopEntry se = parser.fromJson(parts[1], ShopEntry.class);
          Shop shop = new Shop(se.name, se.type);
          result.add(se.id, shop);
          break;
        case ID_DIV:
          DivEntry de = parser.fromJson(parts[1], DivEntry.class);
          Division div = new Division(de.name);
          result.get(de.shopId).getDivisions().add(de.id, div);
          break;
        case ID_PROD:
          Product product = null;
          ProdEntry pe = parser.fromJson(parts[2], ProdEntry.class);
          switch (parts[1]) {
            case ID_FOOD:
              product = new Food(pe.name, pe.vendor, pe.price);
              break;
            case ID_CLOTHES:
              product = new Clothes(pe.name, pe.vendor, pe.price);
              break;
            case ID_DEV:
              product = new Electronics(pe.name, pe.vendor, pe.price);
              break;
            case ID_OTHER:
              product = new Thing(pe.name, pe.vendor, pe.price);
              break;
          }

          result.get(pe.shopId).getDivisions().get(pe.divId)
            .getProducts().add(pe.id, product);
          break;
      }
    }

    MyLog.d(LOG_TAG, "Database loaded: " + result.size());
    return result;
  }
}
