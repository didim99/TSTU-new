package ru.didim99.tstu.ui.oop;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.oop.Abiturient;
import ru.didim99.tstu.core.oop.GradeList;

/**
 * Created by didim99 on 23.02.19.
 */
public class AbiturientAdapter extends RecyclerView.Adapter<AbiturientAdapter.ViewHolder> {

  private final LayoutInflater inflater;
  private OnItemClickListener listener;
  private ArrayList<Abiturient> items;

  AbiturientAdapter(Context context, OnItemClickListener listener) {
    this.inflater = LayoutInflater.from(context);
    this.listener = listener;
  }

  @Override
  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(inflater.inflate(R.layout.item_oop_abit, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    //Enable top divider for first item
    if (position == 0) {
      holder.itemView.setOnClickListener(null);
      holder.topDivider.setVisibility(ImageView.VISIBLE);
      holder.name.setText(R.string.oop_fullName);
      holder.address.setText(R.string.oop_address);
      holder.lang.setText(R.string.oop_lang);
      holder.math.setText(R.string.oop_math);
      holder.physics.setText(R.string.oop_physics);
      holder.itemView.setFocusable(false);
      holder.itemView.setClickable(false);
    } else {
      holder.itemView.setOnClickListener(view ->
        listener.onItemClick(getItemAt(holder.getAdapterPosition() - 1)));
      holder.topDivider.setVisibility(ImageView.INVISIBLE);
      Abiturient a = items.get(position - 1);
      GradeList grades = a.getGrades();
      holder.name.setText(a.getFullName());
      holder.address.setText(a.getAddress());
      holder.address.setText(a.getAddress());
      holder.lang.setText(String.valueOf(grades.getLang()));
      holder.math.setText(String.valueOf(grades.getMath()));
      holder.physics.setText(String.valueOf(grades.getPhysics()));
    }
  }

  @Override
  public int getItemCount() {
    return items == null ? 0 : items.size() + 1;
  }

  void refreshData(ArrayList<Abiturient> items) {
    this.items = items;
    notifyDataSetChanged();
  }

  private int getItemAt(int position) {
    return items.get(position).getId();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final TextView name, address;
    final TextView lang, math, physics;
    final ImageView topDivider;

    ViewHolder(View itemView) {
      super(itemView);
      topDivider = itemView.findViewById(R.id.ivTopDivider);
      name = itemView.findViewById(R.id.tvName);
      address = itemView.findViewById(R.id.tvAddress);
      lang = itemView.findViewById(R.id.tvLang);
      math = itemView.findViewById(R.id.tvMath);
      physics = itemView.findViewById(R.id.tvPhysics);
    }
  }

  interface OnItemClickListener {
    void onItemClick(int itemId);
  }
}