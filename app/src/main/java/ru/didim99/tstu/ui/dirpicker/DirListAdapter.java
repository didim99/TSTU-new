package ru.didim99.tstu.ui.dirpicker;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import ru.didim99.tstu.R;

/**
 * Created by didim99 on 17.06.18.
 */
class DirListAdapter extends RecyclerView.Adapter<DirListAdapter.ViewHolder> {

  private OnItemClickListener listener;
  private LayoutInflater inflater;
  private int colorDir, colorFile;
  private List<DirEntry> files;

  DirListAdapter(Context context, OnItemClickListener listener,
                 List<DirEntry> files) {
    Resources res = context.getResources();
    colorDir = res.getColor(R.color.dirPicker_textActive);
    colorFile = res.getColor(R.color.dirPicker_textInactive);
    this.inflater = LayoutInflater.from(context);
    this.listener = listener;
    this.files = files;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = inflater.inflate(R.layout.item_dir_picker, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    DirEntry file = getItemAt(position);

    //Enable top divider for first item
    holder.topDivider.setVisibility(
      position == 0 ?View.VISIBLE : View.INVISIBLE);

    holder.itemView.setOnClickListener(view ->
      listener.onItemClick(getItemAt(holder.getAdapterPosition())));
    holder.name.setText(file.getName());

    if (file.isDir()) {
      holder.icon.setVisibility(ImageView.VISIBLE);
      holder.icon.setImageResource(R.drawable.ic_folder_24dp);
      holder.name.setTextColor(colorDir);
    } else if (file.isLevelUp()) {
      holder.icon.setVisibility(ImageView.VISIBLE);
      holder.icon.setImageResource(R.drawable.ic_back_24dp);
      holder.name.setText(R.string.dirPicker_levelUp);
      holder.name.setTextColor(colorFile);
    } else {
      holder.icon.setVisibility(ImageView.INVISIBLE);
      holder.name.setTextColor(colorFile);
    }
  }

  @Override
  public int getItemCount() {
    return files == null ? 0 : files.size();
  }

  private DirEntry getItemAt(int position) {
    return files.get(position);
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final ImageView topDivider;
    final ImageView icon;
    final TextView name;

    ViewHolder(View view) {
      super(view);
      topDivider = view.findViewById(R.id.ivTopDivider);
      icon = view.findViewById(R.id.ivFolderIcon);
      name = view.findViewById(R.id.tvFileName);
    }
  }

  interface OnItemClickListener {
    void onItemClick(DirEntry item);
  }
}
