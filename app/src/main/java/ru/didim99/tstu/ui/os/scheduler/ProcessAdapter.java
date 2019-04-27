package ru.didim99.tstu.ui.os.scheduler;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.os.scheduler.Process;
import ru.didim99.tstu.utils.Utils;

/**
 * Created by didim99 on 25.03.19.
 */
public class ProcessAdapter extends RecyclerView.Adapter<ProcessAdapter.ViewHolder> {
  private final LayoutInflater inflater;
  private final Resources res;
  private List<Process> items;
  private int pbColor;

  ProcessAdapter(Context context) {
    this.res = context.getResources();
    this.inflater = LayoutInflater.from(context);
    setProcessColor(R.color.colorAccent);
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(inflater.inflate(R.layout.item_scheduler_process,
      parent, false), pbColor);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Process process = items.get(position);
    holder.name.setText(process.getName());
    holder.id.setText(String.valueOf(process.getId()));
    holder.ram.setText(Utils.formatBytes(process.getRamUsage() << 10));
    holder.state.setProgress(process.getProgress());
  }

  @Override
  public int getItemCount() {
    return items == null ? 0 : items.size();
  }

  void setProcessColor(@ColorRes int colorId) {
    pbColor = res.getColor(colorId);
  }

  void refreshData(List<Process> items) {
    this.items = new ArrayList<>(items);
    notifyDataSetChanged();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final TextView id, name, ram;
    final ProgressBar state;

    ViewHolder(View itemView, int pbColor) {
      super(itemView);
      state = itemView.findViewById(R.id.pbState);
      name = itemView.findViewById(R.id.tvName);
      ram = itemView.findViewById(R.id.tvRAM);
      id = itemView.findViewById(R.id.tvId);
      state.setMax(Process.MAX_VIEW_TICKS);
      state.getProgressDrawable().setColorFilter(
        pbColor, PorterDuff.Mode.SRC_IN);
    }
  }
}
