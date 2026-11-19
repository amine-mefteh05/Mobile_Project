package com.unimate.database;

import android.content.Context;
import android.content.SharedPreferences;

import com.unimate.dto.Weights;
import com.unimate.model.Student;

public class PreferencesManager {

    private static final String PREF_NAME = "unimate_prefs";

    // Keys matching conception dossier specification
    private static final String KEY_STUDENT_NAME = "student_name";
    private static final String KEY_STUDENT_UNIV = "student_university";
    private static final String KEY_STUDENT_LEVEL = "student_level";
    private static final String KEY_ENVIRONMENT = "environment";
    private static final String KEY_WEEKLY_BUDGET = "weekly_budget";
    private static final String KEY_DAY_START = "day_start";
    private static final String KEY_DAY_END = "day_end";
    private static final String KEY_WEIGHT_QUIET = "weight_quiet";
    private static final String KEY_WEIGHT_WIFI = "weight_wifi";
    private static final String KEY_WEIGHT_BUDGET = "weight_budget";
    private static final String KEY_WEIGHT_DISTANCE = "weight_distance";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

    private static PreferencesManager instance;
    private final SharedPreferences prefs;

    public static synchronized PreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesManager(context.getApplicationContext());
        }
        return instance;
    }

    public PreferencesManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public Student getStudentProfile() {
        Student student = new Student();
        student.setName(prefs.getString(KEY_STUDENT_NAME, "Alex Dev"));
        student.setUniversity(prefs.getString(KEY_STUDENT_UNIV, "State University"));
        student.setStudyLevel(prefs.getString(KEY_STUDENT_LEVEL, "Bachelor (Year 3)"));
        student.setPreferredEnvironment(prefs.getString(KEY_ENVIRONMENT, "QUIET"));
        student.setWeeklyBudget(Double.longBitsToDouble(prefs.getLong(KEY_WEEKLY_BUDGET, Double.doubleToLongBits(100.0))));
        student.setDayStart(prefs.getString(KEY_DAY_START, "08:00"));
        student.setDayEnd(prefs.getString(KEY_DAY_END, "20:00"));
        student.setNotificationsEnabled(prefs.getBoolean(KEY_NOTIFICATIONS, true));
        return student;
    }

    public void saveStudentProfile(Student student) {
        if (student == null) return;
        prefs.edit()
                .putString(KEY_STUDENT_NAME, student.getName())
                .putString(KEY_STUDENT_UNIV, student.getUniversity())
                .putString(KEY_STUDENT_LEVEL, student.getStudyLevel())
                .putString(KEY_ENVIRONMENT, student.getPreferredEnvironment())
                .putLong(KEY_WEEKLY_BUDGET, Double.doubleToRawLongBits(student.getWeeklyBudget()))
                .putString(KEY_DAY_START, student.getDayStart())
                .putString(KEY_DAY_END, student.getDayEnd())
                .putBoolean(KEY_NOTIFICATIONS, student.isNotificationsEnabled())
                .apply();
    }

    public double getWeeklyBudget() {
        return Double.longBitsToDouble(prefs.getLong(KEY_WEEKLY_BUDGET, Double.doubleToLongBits(100.0)));
    }

    public void setWeeklyBudget(double budget) {
        prefs.edit().putLong(KEY_WEEKLY_BUDGET, Double.doubleToRawLongBits(budget)).apply();
    }

    public String getDayStart() {
        return prefs.getString(KEY_DAY_START, "08:00");
    }

    public String getDayEnd() {
        return prefs.getString(KEY_DAY_END, "20:00");
    }

    public void setDayWindow(String start, String end) {
        prefs.edit()
                .putString(KEY_DAY_START, start)
                .putString(KEY_DAY_END, end)
                .apply();
    }

    public Weights getWeights() {
        int quiet = prefs.getInt(KEY_WEIGHT_QUIET, 50);
        int wifi = prefs.getInt(KEY_WEIGHT_WIFI, 50);
        int budget = prefs.getInt(KEY_WEIGHT_BUDGET, 50);
        int distance = prefs.getInt(KEY_WEIGHT_DISTANCE, 50);
        return new Weights(quiet, wifi, budget, distance);
    }

    public void saveWeights(Weights weights) {
        if (weights == null) return;
        prefs.edit()
                .putInt(KEY_WEIGHT_QUIET, weights.getQuietWeight())
                .putInt(KEY_WEIGHT_WIFI, weights.getWifiWeight())
                .putInt(KEY_WEIGHT_BUDGET, weights.getBudgetWeight())
                .putInt(KEY_WEIGHT_DISTANCE, weights.getDistanceWeight())
                .apply();
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }
}
