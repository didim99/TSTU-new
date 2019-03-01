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
import ru.didim99.tstu.core.mp.students.Student;

/**
 * Created by didim99 on 23.02.19.
 */
public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

  private final LayoutInflater inflater;
  private ArrayList<Student> items;
  private SimpleDateFormat df;

  StudentAdapter(Context context) {
    df = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT);
    this.inflater = LayoutInflater.from(context);
  }

  @Override
  @NonNull
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ViewHolder(inflater.inflate(R.layout.item_mp_student, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    //Enable top divider for first item
    if (position == 0) {
      holder.topDivider.setVisibility(ImageView.VISIBLE);
      holder.id.setText(R.string.mp_id);
      holder.name.setText(R.string.oop_fullName);
      holder.date.setText(R.string.mp_date);
    } else {
      holder.topDivider.setVisibility(ImageView.INVISIBLE);
      Student s = items.get(position - 1);
      holder.name.setText(s.getName());
      holder.id.setText(String.valueOf(s.getId()));
      holder.date.setText(String.valueOf(
        df.format(new Date(s.getDatetime()))));
    }
  }

  @Override
  public int getItemCount() {
    return items == null ? 0 : items.size() + 1;
  }

  void refreshData(ArrayList<Student> items) {
    this.items = items;
    notifyDataSetChanged();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final TextView id, name, date;
    final ImageView topDivider;

    ViewHolder(View itemView) {
      super(itemView);
      topDivider = itemView.findViewById(R.id.ivTopDivider);
      id = itemView.findViewById(R.id.tvId);
      name = itemView.findViewById(R.id.tvName);
      date = itemView.findViewById(R.id.tvDatetime);
    }
  }
}