package com.example.habittracker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.habittracker.databinding.ActivityHistoryBinding;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    private ActivityHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("История привычек");

        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<Habit> history = HabitStorage.loadHistory(this);
        HistoryAdapter historyAdapter = new HistoryAdapter(history);
        binding.historyRecyclerView.setAdapter(historyAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}