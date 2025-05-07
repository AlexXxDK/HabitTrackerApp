package com.example.habittracker;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Habit implements Parcelable {
    private String name;
    private int progress;
    private int targetDays;
    private String creationDate;

    public Habit(String name, int targetDays) {
        this.name = name;
        this.progress = 0;
        this.targetDays = targetDays;
        this.creationDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
    }

    protected Habit(Parcel in) {
        name = in.readString();
        progress = in.readInt();
        targetDays = in.readInt();
        creationDate = in.readString();
    }

    public static final Creator<Habit> CREATOR = new Creator<Habit>() {
        @Override
        public Habit createFromParcel(Parcel in) {
            return new Habit(in);
        }

        @Override
        public Habit[] newArray(int size) {
            return new Habit[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(progress);
        dest.writeInt(targetDays);
        dest.writeString(creationDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getName() {
        return name;
    }

    public int getProgress() {
        return progress;
    }

    public int getTargetDays() {
        return targetDays;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTargetDays(int targetDays) {
        this.targetDays = targetDays;
    }

    public void incrementProgress() {
        if (progress < targetDays) {
            progress++;
        }
    }

    public void resetProgress() {
        this.progress = 0;
    }
}