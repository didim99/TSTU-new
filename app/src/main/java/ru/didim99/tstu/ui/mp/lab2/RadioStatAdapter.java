package ru.didim99.tstu.ui.mp.lab2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.mp.radio.Song;

/**
 * Created by didim99 on 23.02.19.
 */
public class RadioStatAdapter extends RecyclerView.Adapter<RadioStatAdapter.ViewHolder> {

  private final LayoutInflater inflater;
  private ArrayList<Song> items;
  private SimpleDateFormat df;

  RadioStatAdapter(Context context) {
    df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
    this.inflater = LayoutInflater.from(context);
  }

  @Override
  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(inflater.inflate(R.layout.item_mp_song, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    //Enable top divider for first item
    if (position == 0) {
      holder.topDivider.setVisibility(ImageView.VISIBLE);
      holder.id.setText(R.string.mp_id);
      holder.singer.setText(R.string.mp_singer);
      holder.name.setText(R.string.mp_songName);
      holder.date.setText(R.string.mp_date);
    } else {
      holder.topDivider.setVisibility(ImageView.INVISIBLE);
      Song song = items.get(items.size() - position);
      holder.id.setText(String.valueOf(song.getId()));
      holder.singer.setText(song.getSinger());
      holder.name.setText(song.getName());
      holder.date.setText(String.valueOf(
        df.format(new Date(song.getDate()))));
    }
  }

  @Override
  public int getItemCount() {
    return items == null ? 0 : items.size() + 1;
  }

  void refreshData(ArrayList<Song> items) {
    this.items = items;
    notifyDataSetChanged();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final TextView id, singer, name, date;
    final ImageView topDivider;

    ViewHolder(View itemView) {
      super(itemView);
      topDivider = itemView.findViewById(R.id.ivTopDivider);
      id = itemView.findViewById(R.id.tvId);
      singer = itemView.findViewById(R.id.tvSinger);
      name = itemView.findViewById(R.id.tvName);
      date = itemView.findViewById(R.id.tvDatetime);
    }
  }
}