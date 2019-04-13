package ru.didim99.tstu.ui.os.expanse;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.os.expanse.Player;

/**
 * Created by didim99 on 11.04.19.
 */
public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.ViewHolder> {

  private final Context context;
  private final LayoutInflater inflater;
  private ArrayList<Player> items;

  PlayerAdapter(Context context) {
    this.inflater = LayoutInflater.from(context);
    this.context = context;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(inflater.inflate(R.layout.item_os_player, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    if (position == 0) {
      holder.topDivider.setVisibility(ImageView.VISIBLE);
    } else {
      holder.topDivider.setVisibility(ImageView.INVISIBLE);
    }

    Player player = items.get(position);
    holder.name.setText(context.getString(
      R.string.os_expanse_player, player.getId()));
    holder.score.setText(String.valueOf(player.getScore()));
    holder.color.setColorFilter(player.getColor(), PorterDuff.Mode.SRC);
  }

  @Override
  public int getItemCount() {
    return items == null ? 0 : items.size();
  }

  void refreshData(ArrayList<Player> items) {
    this.items = new ArrayList<>(items);
    Collections.sort(this.items, PlayerAdapter::playerCompare);
    notifyDataSetChanged();
  }

  private static int playerCompare(Player p1, Player p2) {
    int s = Integer.compare(p2.getScore(), p1.getScore());
    return s == 0 ? Integer.compare(p1.getId(), p2.getId()) : s;
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final ImageView topDivider, color;
    final TextView name, score;

    ViewHolder(View itemView) {
      super(itemView);
      topDivider = itemView.findViewById(R.id.ivTopDivider);
      color = itemView.findViewById(R.id.ivColor);
      name = itemView.findViewById(R.id.tvName);
      score = itemView.findViewById(R.id.tvScore);
    }
  }
}
