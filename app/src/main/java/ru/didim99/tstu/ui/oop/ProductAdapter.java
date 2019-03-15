package ru.didim99.tstu.ui.oop;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.oop.shop.products.Product;

/**
 * Created by didim99 on 23.02.19.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

  static final class Type {
    static final int SHOP = 1;
    static final int CARD = 2;
    static final int INVENTORY = 3;
  }

  private final int type;
  private final LayoutInflater inflater;
  private final NumberFormat format;
  private OnItemClickListener listener;
  private ArrayList<Product> items;

  ProductAdapter(Context context, int type, OnItemClickListener listener) {
    format = NumberFormat.getCurrencyInstance(new Locale("ru"));
    format.setCurrency(Currency.getInstance("RUB"));
    format.setMaximumFractionDigits(0);
    this.inflater = LayoutInflater.from(context);
    this.listener = listener;
    this.type = type;
  }

  @Override
  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(inflater.inflate(R.layout.item_oop_product,
      parent, false), type);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    //Enable top divider for first item
    if (position == 0) {
      holder.itemView.setOnClickListener(null);
      holder.topDivider.setVisibility(ImageView.VISIBLE);
      holder.name.setText(R.string.oop_shop_name);
      if (type == Type.SHOP)
        holder.vendor.setText(R.string.oop_shop_vendor);
      if (type != Type.INVENTORY)
        holder.price.setText(R.string.oop_shop_price);
      if (type != Type.SHOP)
        holder.count.setText(R.string.oop_shop_count);
      if (type == Type.CARD)
        holder.sum.setText(R.string.oop_shop_sum);
      holder.itemView.setFocusable(false);
      holder.itemView.setClickable(false);
    } else {
      holder.itemView.setOnClickListener(view ->
        listener.onItemClick(getItemAt(holder.getAdapterPosition() - 1)));
      holder.topDivider.setVisibility(ImageView.INVISIBLE);
      Product p = items.get(position - 1);
      holder.name.setText(p.getName());
      if (type == Type.SHOP)
        holder.vendor.setText(p.getVendor());
      if (type != Type.INVENTORY)
        holder.price.setText(formatMoney(p.getPrice()));
      if (type != Type.SHOP)
        holder.count.setText(String.valueOf(p.getCount()));
      if (type == Type.CARD)
        holder.sum.setText(formatMoney(p.getPriceSum()));
    }
  }

  private String formatMoney(int money) {
    return format.format(money);
  }

  @Override
  public int getItemCount() {
    return items == null ? 1 : items.size() + 1;
  }

  void refreshData(ArrayList<Product> items) {
    this.items = items;
    notifyDataSetChanged();
  }

  private Product getItemAt(int position) {
    return items.get(position);
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final TextView name, vendor, price;
    final TextView count, sum;
    final ImageView topDivider;

    ViewHolder(View itemView, int type) {
      super(itemView);
      topDivider = itemView.findViewById(R.id.ivTopDivider);
      name = itemView.findViewById(R.id.tvName);
      vendor = itemView.findViewById(R.id.tvVendor);
      price = itemView.findViewById(R.id.tvPrice);
      count = itemView.findViewById(R.id.tvCount);
      sum = itemView.findViewById(R.id.tvSum);
      switch (type) {
        case Type.CARD:
          vendor.setVisibility(View.GONE);
          count.setVisibility(View.VISIBLE);
          sum.setVisibility(View.VISIBLE);
          break;
        case Type.INVENTORY:
          vendor.setVisibility(View.GONE);
          price.setVisibility(View.GONE);
          count.setVisibility(View.VISIBLE);
          break;
      }
    }
  }

  interface OnItemClickListener {
    void onItemClick(Product item);
  }
}