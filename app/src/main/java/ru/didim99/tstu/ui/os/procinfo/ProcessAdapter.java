package ru.didim99.tstu.ui.os.procinfo;

import android.annotation.SuppressLint;
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
import ru.didim99.tstu.core.os.procinfo.ProcessInfo;

/**
 * Created by didim99 on 25.03.19.
 */
public class ProcessAdapter extends RecyclerView.Adapter<ProcessAdapter.ViewHolder> {
  private static final String BYTES = "bKMGT";

  static final class Mode {
    static final int PROCESS = 1;
    static final int THREAD = 2;
  }

  private int mode;
  private final LayoutInflater inflater;
  private ArrayList<ProcessInfo> items;
  private OnItemClickListener listener;
  private int colorNormal, colorGreen, colorYellow, colorRed;

  ProcessAdapter(Context context, OnItemClickListener listener, int mode) {
    colorNormal = context.getResources().getColor(R.color.os_normal);
    colorGreen = context.getResources().getColor(R.color.os_green);
    colorYellow = context.getResources().getColor(R.color.os_yellow);
    colorRed = context.getResources().getColor(R.color.os_red);
    this.inflater = LayoutInflater.from(context);
    this.listener = listener;
    this.mode = mode;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(inflater.inflate(mode == Mode.THREAD ?
        R.layout.item_os_thread : R.layout.item_os_process,
      parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    boolean isThread = mode == Mode.THREAD;
    boolean clickable = listener != null;
    if (position == 0) {
      holder.itemView.setOnClickListener(null);
      holder.topDivider.setVisibility(ImageView.VISIBLE);
      holder.pos.setText(R.string.os_position);
      holder.pid.setText(isThread ? R.string.os_tid : R.string.os_pid);
      holder.name.setText(isThread ? R.string.os_name : R.string.os_cmd);
      holder.ppid.setText(R.string.os_ppid);
      holder.nice.setText(R.string.os_nice);
      holder.threads.setText(R.string.os_threads);
      holder.mem.setText(R.string.os_memory);
      holder.name.setTextColor(colorNormal);
      holder.nice.setTextColor(colorNormal);
      holder.pid.setTextColor(colorNormal);
      holder.itemView.setFocusable(false);
      holder.itemView.setClickable(false);
    } else {
      if (clickable)
        holder.itemView.setOnClickListener(view ->
          listener.onItemClick(getItemAt(holder.getAdapterPosition() - 1)));
      holder.itemView.setFocusable(clickable);
      holder.itemView.setClickable(clickable);
      holder.topDivider.setVisibility(ImageView.INVISIBLE);
      ProcessInfo process = items.get(position - 1);
      holder.pos.setText(String.valueOf(position));
      holder.pid.setText(String.valueOf(process.getPid()));
      holder.ppid.setText(String.valueOf(process.getPpid()));
      holder.nice.setText(String.valueOf(process.getNice()));
      holder.threads.setText(String.valueOf(process.getThreads()));
      holder.mem.setText(formatBytes(process.getMRes()));
      holder.name.setText(process.getName());
      int nice = process.getNice();
      holder.nice.setTextColor(nice < 0 ? colorRed
        : (nice == 0 ? colorNormal : colorGreen));
      holder.pid.setTextColor(process.isKernelThread()
        ? colorGreen : colorNormal);

      int color = colorRed;
      switch (process.getState()) {
        case 'S': color = colorNormal; break;
        case 'R': color = colorGreen; break;
        case 'D': color = colorYellow; break;
      }

      holder.name.setTextColor(color);
    }
  }

  @Override
  public int getItemCount() {
    return items == null ? 0 : items.size() + 1;
  }

  private ProcessInfo getItemAt(int pos) {
    return items.get(pos);
  }

  void refreshData(ArrayList<ProcessInfo> items) {
    this.items = new ArrayList<>(items);
    notifyDataSetChanged();
  }

  @SuppressLint("DefaultLocale")
  private String formatBytes(int kBytes) {
    int offset = 1;
    while (kBytes > 1024) { kBytes >>= 10; offset++; }
    return String.format("%d%c", kBytes, BYTES.charAt(offset));
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final TextView pos, pid, ppid, nice, threads, mem, name;
    final ImageView topDivider;

    ViewHolder(View itemView) {
      super(itemView);
      topDivider = itemView.findViewById(R.id.ivTopDivider);
      pos = itemView.findViewById(R.id.tvPos);
      pid = itemView.findViewById(R.id.tvPid);
      ppid = itemView.findViewById(R.id.tvPpid);
      nice = itemView.findViewById(R.id.tvNice);
      threads = itemView.findViewById(R.id.tvThreads);
      mem = itemView.findViewById(R.id.tvMem);
      name = itemView.findViewById(R.id.tvName);
    }
  }

  interface OnItemClickListener {
    void onItemClick(ProcessInfo item);
  }
}
