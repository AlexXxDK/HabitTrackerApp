package com.example.habittracker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

//internal storage; assets чтоб что-то считывалось; binding
public class HabitTrackerApp extends AppCompatActivity {
    private HabitAdapter adapter;
    private ArrayList<Habit> habitList;
    private static final String KEY_HABIT_LIST = "habit_list";
    private ActivityResultLauncher<Intent> addHabitLauncher;
    private Handler notificationHandler;
    private Runnable notificationRunnable;
    private static final String CHANNEL_ID = "habit_channel";
    private static final int NOTIFICATION_ID = 1;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_history) {
                Intent intent = new Intent(this, HistoryActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawers();
                return true;
            }
            return false;
        });

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d("HabitTrackerApp", "Разрешение на уведомления получено");
                        startNotificationLoop();
                    } else {
                        Log.d("HabitTrackerApp", "Разрешение на уведомления отклонено");
                        Toast.makeText(this, "Разрешение на уведомления отклонено. Включи в настройках.", Toast.LENGTH_LONG).show();
                    }
                });

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_HABIT_LIST)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                habitList = savedInstanceState.getParcelableArrayList(KEY_HABIT_LIST, Habit.class);
            } else {
                habitList = savedInstanceState.getParcelableArrayList(KEY_HABIT_LIST);
            }
        } else {
            habitList = HabitStorage.loadHabits(this);
        }

        adapter = new HabitAdapter(this, habitList,
                this::onHabitCompleted,
                this::showDeleteConfirmation,
                () -> HabitStorage.saveHabits(this, habitList));

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        addHabitLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Habit newHabit = result.getData().getParcelableExtra("new_habit");
                        if (newHabit != null) {
                            habitList.add(newHabit);
                            HabitStorage.saveHabits(this, habitList);
                            adapter.notifyItemInserted(habitList.size() - 1);
                        }
                    }
                });

        FloatingActionButton addButton = findViewById(R.id.fab);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddHabitActivity.class);
            addHabitLauncher.launch(intent);
        });

        createNotificationChannel();
        requestNotificationPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            shareHabits();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareHabits() {
        if (habitList.isEmpty()) {
            Toast.makeText(this, "Нет привычек для отправки!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder shareText = new StringBuilder();
        shareText.append("Мои привычки:\n\n");
        for (Habit habit : habitList) {
            shareText.append("📌 ")
                    .append(habit.getName())
                    .append("\n")
                    .append("Прогресс: ")
                    .append(habit.getProgress())
                    .append("/")
                    .append(habit.getTargetDays())
                    .append(" дней\n")
                    .append("Дата создания: ")
                    .append(habit.getCreationDate())
                    .append("\n\n");
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Мои привычки");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        startActivity(Intent.createChooser(shareIntent, "Поделиться привычками"));
    }

    private void onHabitCompleted(String habitName, int position) {
        Habit alma = habitList.get(position);
        HabitStorage.addToHistory(this, alma);
        habitList.remove(position);
        HabitStorage.saveHabits(this, habitList);
        adapter.notifyItemRemoved(position);
        showCompletionMessage(habitName);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Напоминания о привычках",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Напоминания о привычках");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            Log.d("HabitTrackerApp", "Канал уведомлений создан");
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("HabitTrackerApp", "Запрашиваем разрешение на уведомления");
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                Log.d("HabitTrackerApp", "Разрешение на уведомления уже есть");
                startNotificationLoop();
            }
        } else {
            startNotificationLoop();
        }
    }

    private void sendNotification() {
        Log.d("HabitTrackerApp", "Отправляем уведомление");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Habit Tracker")
                .setContentText("Бро, зачекинься!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void startNotificationLoop() {
        Log.d("HabitTrackerApp", "Запускаем цикл уведомлений");
        notificationHandler = new Handler(Looper.getMainLooper());
        notificationRunnable = new Runnable() {
            @Override
            public void run() {
                sendNotification();
                notificationHandler.postDelayed(this, 60 * 60 * 1000);
            }
        };
        notificationHandler.post(notificationRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationHandler != null && notificationRunnable != null) {
            notificationHandler.removeCallbacks(notificationRunnable);
            Log.d("HabitTrackerApp", "Цикл уведомлений остановлен");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_HABIT_LIST, habitList);
    }

    private void showCompletionMessage(String habitName) {
        Toast.makeText(this,
                "Отличная работа! Привычка '" + habitName + "' сформирована!",
                Toast.LENGTH_LONG).show();
    }

    private void showDeleteConfirmation(int position) {
        String habitName = habitList.get(position).getName();

        new AlertDialog.Builder(this)
                .setTitle("Удаление привычки")
                .setMessage("Вы уверены, что хотите удалить привычку \"" + habitName + "\"?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    Habit deletedHabit = habitList.get(position);
                    HabitStorage.addToHistory(this, deletedHabit);
                    habitList.remove(position);
                    HabitStorage.saveHabits(this, habitList);
                    adapter.notifyItemRemoved(position);
                })
                .setNegativeButton("Отмена", (dialog, which) -> {
                    adapter.notifyItemChanged(position);
                })
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        HabitStorage.saveHabits(this, habitList);
    }
}