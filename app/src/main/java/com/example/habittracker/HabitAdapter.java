package com.example.habittracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittracker.databinding.ItemHabitBinding;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {
    private List<Habit> habits;
    private CompletionCallback completionCallback;
    private DeleteCallback deleteCallback;
    private SaveCallback saveCallback;
    private Context context;

    public interface CompletionCallback {
        void onHabitCompleted(String habitName, int position);
    }

    public interface DeleteCallback {
        void onHabitDeleted(int position);
    }

    public interface SaveCallback {
        void onHabitProgressChanged();
    }

    public HabitAdapter(Context context, List<Habit> habits,
                        CompletionCallback completionCallback,
                        DeleteCallback deleteCallback,
                        SaveCallback saveCallback) {
        this.context = context;
        this.habits = habits;
        this.completionCallback = completionCallback;
        this.deleteCallback = deleteCallback;
        this.saveCallback = saveCallback;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemHabitBinding binding = ItemHabitBinding.inflate(inflater, parent, false);
        return new HabitViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        Context context = holder.binding.getRoot().getContext();

        int habitTargetDays = habit.getTargetDays();

        holder.binding.habitName.setText(habit.getName());
        holder.binding.habitProgress.setMax(habitTargetDays);
        holder.binding.habitProgress.setProgress(habit.getProgress());
        holder.binding.habitProgressText.setText(habit.getProgress() + "/" + habitTargetDays);

        boolean isDarkTheme = (context.getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        if (isDarkTheme) {
            holder.binding.habitProgress.setProgressTintList(ContextCompat.getColorStateList(context, R.color.progress_dark));
            holder.binding.habitName.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            holder.binding.habitProgressText.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        } else {
            holder.binding.habitProgress.setProgressTintList(ContextCompat.getColorStateList(context, R.color.progress_light));
            holder.binding.habitName.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            holder.binding.habitProgressText.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }

        holder.binding.habitDone.setOnClickListener(v -> {
            if (habit.getProgress() < habitTargetDays) {
                habit.incrementProgress();
                holder.binding.habitProgress.setProgress(habit.getProgress());
                holder.binding.habitProgressText.setText(habit.getProgress() + "/" + habitTargetDays);
                notifyItemChanged(position);

                if (habit.getProgress() >= habitTargetDays && completionCallback != null) {
                    completionCallback.onHabitCompleted(habit.getName(), position);
                }

                if (saveCallback != null) {
                    saveCallback.onHabitProgressChanged();
                }
            } else {
                Toast.makeText(context, "Привычка уже завершена!", Toast.LENGTH_SHORT).show();
            }
        });

        holder.binding.buttonRemoveHabit.setOnClickListener(v -> {
            if (deleteCallback != null) {
                deleteCallback.onHabitDeleted(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        ItemHabitBinding binding;

        public HabitViewHolder(ItemHabitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}