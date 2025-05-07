package com.example.habittracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.habittracker.databinding.ActivityAddHabitBinding;

public class AddHabitActivity extends AppCompatActivity {
    private static final String KEY_HABIT_NAME = "habit_name";
    private static final String KEY_DAYS = "days";
    private ActivityAddHabitBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddHabitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Настройка NumberPicker
        binding.dayPicker.setMinValue(1);
        binding.dayPicker.setMaxValue(100);
        binding.dayPicker.setValue(21);

        // Обработчики кнопок
        binding.addButton.setOnClickListener(v -> addHabit());
        binding.cancelButton.setOnClickListener(v -> finish());

        // Восстановление состояния
        if (savedInstanceState != null) {
            binding.inputHabitName.setText(savedInstanceState.getString(KEY_HABIT_NAME, ""));
            binding.dayPicker.setValue(savedInstanceState.getInt(KEY_DAYS, 21));
        }
    }

    private void addHabit() {
        String habitName = binding.inputHabitName.getText().toString().trim();
        int days = binding.dayPicker.getValue();
        if (!habitName.isEmpty()) {
            Habit newHabit = new Habit(habitName, days);
            Intent result = new Intent();
            result.putExtra("new_habit", newHabit);
            setResult(RESULT_OK, result);
            finish();
        } else {
            Toast.makeText(this, "Введите название привычки", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_HABIT_NAME, binding.inputHabitName.getText().toString());
        outState.putInt(KEY_DAYS, binding.dayPicker.getValue());
    }
}