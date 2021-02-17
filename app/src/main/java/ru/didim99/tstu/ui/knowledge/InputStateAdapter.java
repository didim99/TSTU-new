package ru.didim99.tstu.ui.knowledge;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.knowledge.neuralnet.Net;
import ru.didim99.tstu.ui.view.RangeBar;

/**
 * Created by didim99 on 11.04.19.
 */
public class InputStateAdapter extends RecyclerView.Adapter<InputStateAdapter.ViewHolder> {

  private final Context context;
  private final LayoutInflater inflater;
  private final String titleTpl, nameTpl;

  InputStateAdapter(Context context) {
    this.titleTpl = context.getString(R.string.knowledge_inputValueTpl);
    this.nameTpl = context.getString(R.string.knowledge_inputNameTpl);
    this.inflater = LayoutInflater.from(context);
    this.context = context;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(inflater.inflate(R.layout.item_neural_input, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    if (position == 0) {
      holder.topDivider.setVisibility(ImageView.VISIBLE);
    } else {
      holder.topDivider.setVisibility(ImageView.INVISIBLE);
    }

    String inputName = String.format(Locale.US, nameTpl, position + 1);
    holder.value.setTitle(String.format(Locale.US, titleTpl, inputName));
  }

  @Override
  public int getItemCount() {
    //return items == null ? 0 : items.size();
    return 0;
  }

  /*void refreshData(ArrayList<Player> items) {
    this.items = new ArrayList<>(items);
    Collections.sort(this.items, InputStateAdapter::playerCompare);
    notifyDataSetChanged();
  }*/

  static class ViewHolder extends RecyclerView.ViewHolder {
    final ImageView topDivider;
    final RangeBar value;

    ViewHolder(View itemView) {
      super(itemView);
      topDivider = itemView.findViewById(R.id.ivTopDivider);
      value = itemView.findViewById(R.id.rbInputValue);
      value.setFactor(Net.INPUT_FACTOR);
      value.setBounds(Net.INPUT_MIN, Net.INPUT_MAX);
    }
  }
}
