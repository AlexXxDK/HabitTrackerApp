package com.example.habittracker;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.habittracker.databinding.FragmentHistoryBinding;
import java.util.ArrayList;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;

    public HistoryFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ArrayList<Habit> history = HabitStorage.loadHistory(getContext());
        HistoryAdapter historyAdapter = new HistoryAdapter(history);
        binding.historyRecyclerView.setAdapter(historyAdapter);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Избегаем утечек памяти
    }
}