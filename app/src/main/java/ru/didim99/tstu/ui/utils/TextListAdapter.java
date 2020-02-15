package ru.didim99.tstu.ui.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import ru.didim99.tstu.R;

/**
 * Created by didim99 on 06.09.19.
 */
public class TextListAdapter extends RecyclerView.Adapter<TextListAdapter.ViewHolder> {
  private ArrayList<String> data;
  private LayoutInflater inflater;

  public TextListAdapter(Context context) {
    this.inflater = LayoutInflater.from(context);
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(inflater.inflate(R.layout.item_out_data, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.tvData.setText(data.get(position));
  }

  @Override
  public int getItemCount() {
    return data == null ? 0 : data.size();
  }

  public void refreshData(ArrayList<String> data) {
    this.data = data;
    notifyDataSetChanged();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final TextView tvData;

    ViewHolder(View itemView) {
      super(itemView);
      this.tvData = itemView.findViewById(R.id.tvData);
    }
  }
}
