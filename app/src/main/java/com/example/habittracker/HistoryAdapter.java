package com.example.habittracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.widget.TextView;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<Habit> history;

    public HistoryAdapter(List<Habit> history) {
        this.history = history;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Habit habit = history.get(position);
        holder.habitName.setText(habit.getName());
        holder.habitDate.setText("Дата создания: " + habit.getCreationDate());
        holder.habitDays.setText("Дней: " + habit.getTargetDays());
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView habitName;
        TextView habitDate;
        TextView habitDays;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            habitName = itemView.findViewById(R.id.history_habit_name);
            habitDate = itemView.findViewById(R.id.history_habit_date);
            habitDays = itemView.findViewById(R.id.history_habit_days);
        }
    }
}