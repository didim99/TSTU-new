package ru.didim99.tstu.ui.os.procinfo;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.os.procinfo.MappingInfo;
import ru.didim99.tstu.core.os.procinfo.ProcessList;
import ru.didim99.tstu.ui.UIManager;

/**
 * Created by didim99 on 25.03.19.
 */
public class MappingAdapter extends RecyclerView.Adapter<MappingAdapter.ViewHolder> {

  private final LayoutInflater inflater;
  private ArrayList<MappingInfo> items;
  private int colorNormal, colorGreen, colorYellow;

  MappingAdapter(Context context) {
    Resources res = context.getResources();
    UIManager uiManager = UIManager.getInstance();
    colorNormal = res.getColor(uiManager.resolveAttr(R.attr.colorTextNormal));
    colorGreen = res.getColor(uiManager.resolveAttr(R.attr.clr_green));
    colorYellow = res.getColor(uiManager.resolveAttr(R.attr.clr_yellow));
    this.inflater = LayoutInflater.from(context);
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(inflater.inflate(R.layout.item_os_mapping, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.topDivider.setVisibility(ImageView.INVISIBLE);
    MappingInfo mapping = items.get(position);
    String name = mapping.getName();
    holder.name.setText(name);

    int color = colorNormal;
    if (mapping.isExecutable())
      color = colorGreen;
    else if (name.startsWith(ProcessList.DEVDIR))
      color = colorYellow;

    holder.name.setTextColor(color);
  }

  @Override
  public int getItemCount() {
    return items == null ? 0 : items.size();
  }

  void refreshData(ArrayList<MappingInfo> items) {
    this.items = new ArrayList<>(items);
    notifyDataSetChanged();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final ImageView topDivider;
    final TextView name;

    ViewHolder(View itemView) {
      super(itemView);
      topDivider = itemView.findViewById(R.id.ivTopDivider);
      name = itemView.findViewById(R.id.tvName);
    }
  }
}
