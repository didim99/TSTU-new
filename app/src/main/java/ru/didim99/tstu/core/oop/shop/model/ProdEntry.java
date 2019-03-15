package ru.didim99.tstu.core.oop.shop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by didim99 on 14.03.19.
 */
class ProdEntry {
  @SerializedName("id") int id;
  @SerializedName("shopId") int shopId;
  @SerializedName("divId") int divId;
  @SerializedName("name") String name;
  @SerializedName("vendor") String vendor;
  @SerializedName("price") int price;
}
