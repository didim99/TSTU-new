package ru.didim99.tstu.core.oop.shop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by didim99 on 14.03.19.
 */
class ShopEntry {
  @SerializedName("id") int id;
  @SerializedName("name") String name;
  @SerializedName("type") int type;
}
