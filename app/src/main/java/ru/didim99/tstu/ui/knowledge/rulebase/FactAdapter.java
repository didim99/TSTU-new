package ru.didim99.tstu.ui.knowledge.rulebase;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import ru.didim99.tstu.R;
import ru.didim99.tstu.core.knowledge.rulebase.RuleBase;
import ru.didim99.tstu.core.knowledge.rulebase.Variable;
import ru.didim99.tstu.ui.UIManager;

/**
 * Created by didim99 on 05.03.21.
 */

public class FactAdapter extends RecyclerView.Adapter<FactAdapter.ViewHolder> {

  private final int colorNormal, colorGreen;
  private final LayoutInflater inflater;
  private RuleBase source;

  FactAdapter(Context context) {
    this.inflater = LayoutInflater.from(context);
    Resources res = context.getResources();
    UIManager uiManager = UIManager.getInstance();
    colorNormal = res.getColor(uiManager.resolveAttr(R.attr.colorTextNormal));
    colorGreen = res.getColor(uiManager.resolveAttr(R.attr.colorTextGreen));
  }

  public void setSource(RuleBase source) {
    this.source = source;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(inflater.inflate(R.layout.item_knowledge_fact, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    if (position == 0) {
      holder.topDivider.setVisibility(ImageView.VISIBLE);
      holder.pos.setText(R.string.position);
      holder.var.setText(R.string.knowledge_variable);
      holder.val.setText(R.string.knowledge_value);
      holder.var.setTextColor(colorNormal);
      holder.val.setTextColor(colorNormal);
    } else {
      holder.topDivider.setVisibility(ImageView.INVISIBLE);
      Variable fact = source.getFacts().get(position - 1);
      holder.pos.setText(String.valueOf(position));
      holder.var.setText(fact.getName());
      holder.val.setText(fact.getValue());

      int textColor = fact.isUserDefined() ? colorGreen : colorNormal;
      holder.var.setTextColor(textColor);
      holder.val.setTextColor(textColor);
    }
  }

  @Override
  public int getItemCount() {
    return source == null ? 1 : source.getFacts().size() + 1;
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final ImageView topDivider;
    final TextView pos, var, val;

    ViewHolder(View itemView) {
      super(itemView);
      topDivider = itemView.findViewById(R.id.ivTopDivider);
      pos = itemView.findViewById(R.id.tvPos);
      var = itemView.findViewById(R.id.tvVariable);
      val = itemView.findViewById(R.id.tvValue);
    }
  }
}
