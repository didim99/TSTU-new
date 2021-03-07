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
import ru.didim99.tstu.core.knowledge.rulebase.Condition;
import ru.didim99.tstu.core.knowledge.rulebase.Rule;
import ru.didim99.tstu.core.knowledge.rulebase.RuleBase;
import ru.didim99.tstu.core.knowledge.rulebase.RuleTarget;
import ru.didim99.tstu.ui.UIManager;

/**
 * Created by didim99 on 05.03.21.
 */

public class RuleAdapter extends RecyclerView.Adapter<RuleAdapter.ViewHolder> {

  private final int colorNormal, colorFalse;
  private final int colorPartially, colorTrue;
  private final LayoutInflater inflater;
  private RuleBase source;

  RuleAdapter(Context context) {
    this.inflater = LayoutInflater.from(context);
    Resources res = context.getResources();
    UIManager uiManager = UIManager.getInstance();
    colorNormal = res.getColor(uiManager.resolveAttr(R.attr.colorTextNormal));
    colorFalse = res.getColor(uiManager.resolveAttr(R.attr.colorTextRed));
    colorPartially = res.getColor(uiManager.resolveAttr(R.attr.colorTextYellow));
    colorTrue = res.getColor(uiManager.resolveAttr(R.attr.colorTextGreen));
  }

  public void setSource(RuleBase source) {
    this.source = source;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(inflater.inflate(R.layout.item_knowledge_rule, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    if (position == 0) {
      holder.topDivider.setVisibility(ImageView.VISIBLE);
      holder.pos.setText(R.string.position);
      holder.condition.setText(R.string.knowledge_if);
      holder.target.setText(R.string.knowledge_then);
      holder.condition.setTextColor(colorNormal);
      holder.target.setTextColor(colorNormal);
    } else {
      holder.topDivider.setVisibility(ImageView.INVISIBLE);
      holder.pos.setText(String.valueOf(position));
      Rule rule = source.getRules().get(position - 1);

      StringBuilder sb = new StringBuilder();
      for (Condition c : rule.getConditions())
        sb.append(c).append('\n');
      holder.condition.setText(sb.toString().trim());
      sb = new StringBuilder();
      for (RuleTarget rt : rule.getTarget())
        sb.append(rt).append('\n');
      holder.target.setText(sb.toString().trim());

      int textColor = colorNormal;
      Rule.Status status = rule.getStatus();
      if (status != null) {
        switch (status) {
          case FALSE: textColor = colorFalse; break;
          case PARTIALLY: textColor = colorPartially; break;
          case TRUE: textColor = colorTrue; break;
        }
      }

      holder.condition.setTextColor(textColor);
      holder.target.setTextColor(textColor);
    }
  }

  @Override
  public int getItemCount() {
    return source == null ? 1 : source.getRules().size() + 1;
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final ImageView topDivider;
    final TextView pos, condition, target;

    ViewHolder(View itemView) {
      super(itemView);
      topDivider = itemView.findViewById(R.id.ivTopDivider);
      pos = itemView.findViewById(R.id.tvPos);
      condition = itemView.findViewById(R.id.tvCondition);
      target = itemView.findViewById(R.id.tvTarget);
    }
  }
}
