package ru.didim99.tstu.ui.knowledge.neuralnet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.Locale;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.knowledge.neuralnet.Net;
import ru.didim99.tstu.ui.view.RangeBar;

/**
 * Created by didim99 on 11.04.19.
 */
public class InputStateAdapter extends RecyclerView.Adapter<InputStateAdapter.ViewHolder> {

  private final LayoutInflater inflater;
  private final String titleTpl, nameTpl;
  private final ValueChangeListener listener;
  private Net net;

  InputStateAdapter(Context context, ValueChangeListener listener) {
    this.titleTpl = context.getString(R.string.knowledge_inputValueTpl);
    this.nameTpl = context.getString(R.string.knowledge_inputNameTpl);
    this.inflater = LayoutInflater.from(context);
    this.listener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(inflater.inflate(R.layout.item_neural_input, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    String inputName = String.format(Locale.US, nameTpl, position + 1);
    holder.value.setTitle(String.format(Locale.US, titleTpl, inputName));
    holder.value.setValue(net.getInput(position));
    holder.value.setOnScaledValueChangedListener(value ->
      listener.onValueChanged(holder.getAdapterPosition(), value));
  }

  @Override
  public int getItemCount() {
    if (net == null) return 0;
    else return net.getConfig().getInputCount();
  }

  public void setDataSource(Net net) {
    this.net = net;
    notifyDataSetChanged();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final RangeBar value;

    ViewHolder(View itemView) {
      super(itemView);
      value = itemView.findViewById(R.id.rbInputValue);
      value.setFactor(Net.INPUT_FACTOR);
      value.setBounds(Net.INPUT_MIN, Net.INPUT_MAX);
    }
  }

  public interface ValueChangeListener {
    void onValueChanged(int index, double value);
  }
}
