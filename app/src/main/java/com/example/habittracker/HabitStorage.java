package com.example.habittracker;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class HabitStorage {
    private static final String PREFS_NAME = "habit_data";
    private static final String HABIT_KEY = "habits";
    private static final String HISTORY_KEY = "completed_habits";
    private static final String HABIT_FILE_NAME = "habits_backup.json";
    private static final String DEFAULT_HABITS_FILE = "default_habits.json";

    public static void saveHabits(Context context, ArrayList<Habit> habits) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(habits);
        editor.putString(HABIT_KEY, json);
        editor.apply();
        saveHabitsToFile(context, habits);
    }

    public static ArrayList<Habit> loadHabits(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(HABIT_KEY, null);
        Type type = new TypeToken<ArrayList<Habit>>() {}.getType();
        ArrayList<Habit> habits = json == null ? null : new Gson().fromJson(json, type);
        if (habits == null) {
            habits = loadHabitsFromFile(context);
        }
        if (habits.isEmpty()) {
            habits = loadDefaultHabitsFromAssets(context); // Загружаем из assets, если ничего нет
        }
        return habits;
    }

    public static void saveHistory(Context context, ArrayList<Habit> history) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(history);
        editor.putString(HISTORY_KEY, json);
        editor.apply();
    }

    public static ArrayList<Habit> loadHistory(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(HISTORY_KEY, null);
        Type type = new TypeToken<ArrayList<Habit>>() {}.getType();
        return json == null ? new ArrayList<>() : new Gson().fromJson(json, type);
    }

    public static void addToHistory(Context context, Habit habit) {
        ArrayList<Habit> history = loadHistory(context);
        history.add(habit);
        saveHistory(context, history);
    }

    private static void saveHabitsToFile(Context context, ArrayList<Habit> habits) {
        try (FileOutputStream fos = context.openFileOutput(HABIT_FILE_NAME, Context.MODE_PRIVATE)) {
            String json = new Gson().toJson(habits);
            fos.write(json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<Habit> loadHabitsFromFile(Context context) {
        try (FileInputStream fis = context.openFileInput(HABIT_FILE_NAME);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            Type type = new TypeToken<ArrayList<Habit>>() {}.getType();
            ArrayList<Habit> habits = new Gson().fromJson(json.toString(), type);
            return habits != null ? habits : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static ArrayList<Habit> loadDefaultHabitsFromAssets(Context context) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open(DEFAULT_HABITS_FILE)))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            Type type = new TypeToken<ArrayList<Habit>>() {}.getType();
            ArrayList<Habit> habits = new Gson().fromJson(json.toString(), type);
            return habits != null ? habits : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}