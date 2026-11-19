package com.unimate.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "UniMate_DB";
    private static final String DATABASE_NAME = "unimate.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_TASKS = "tasks";
    public static final String TABLE_EVENTS = "events";
    public static final String TABLE_STUDY_PLACES = "study_places";
    public static final String TABLE_OPPORTUNITIES = "opportunities";
    public static final String TABLE_EXPENSES = "expenses";
    public static final String TABLE_FAVORITES = "favorites";

    // Common columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_STUDENT_ID = "student_id";

    // Tasks columns
    public static final String COLUMN_TASK_TITLE = "title";
    public static final String COLUMN_TASK_DESCRIPTION = "description";
    public static final String COLUMN_TASK_CATEGORY = "category";
    public static final String COLUMN_TASK_PRIORITY = "priority";
    public static final String COLUMN_TASK_DEADLINE = "deadline";
    public static final String COLUMN_TASK_DURATION = "duration";
    public static final String COLUMN_TASK_STATUS = "status";
    public static final String COLUMN_TASK_HAS_REMINDER = "has_reminder";
    public static final String COLUMN_TASK_REMINDER_TIME = "reminder_time";

    // Events columns
    public static final String COLUMN_EVENT_TITLE = "title";
    public static final String COLUMN_EVENT_DATE = "date";
    public static final String COLUMN_EVENT_START_TIME = "start_time";
    public static final String COLUMN_EVENT_END_TIME = "end_time";
    public static final String COLUMN_EVENT_LOCATION = "location";
    public static final String COLUMN_EVENT_TYPE = "type";
    public static final String COLUMN_EVENT_TASK_ID = "task_id";

    // StudyPlaces columns
    public static final String COLUMN_PLACE_NAME = "name";
    public static final String COLUMN_PLACE_ADDRESS = "address";
    public static final String COLUMN_PLACE_PRICE_LEVEL = "price_level";
    public static final String COLUMN_PLACE_NOISE_LEVEL = "noise_level";
    public static final String COLUMN_PLACE_WIFI = "wifi";
    public static final String COLUMN_PLACE_OUTLETS = "outlets";
    public static final String COLUMN_PLACE_HOURS = "opening_hours";
    public static final String COLUMN_PLACE_LAT = "latitude";
    public static final String COLUMN_PLACE_LNG = "longitude";
    public static final String COLUMN_PLACE_CATEGORY = "category";

    // Opportunities columns
    public static final String COLUMN_OPP_TITLE = "title";
    public static final String COLUMN_OPP_DESCRIPTION = "description";
    public static final String COLUMN_OPP_CATEGORY = "category";
    public static final String COLUMN_OPP_DEADLINE = "deadline";
    public static final String COLUMN_OPP_LINK = "link";
    public static final String COLUMN_OPP_ORG = "organization";

    // Expenses columns
    public static final String COLUMN_EXPENSE_AMOUNT = "amount";
    public static final String COLUMN_EXPENSE_CATEGORY = "category";
    public static final String COLUMN_EXPENSE_DATE = "date";
    public static final String COLUMN_EXPENSE_DESCRIPTION = "description";

    // Favorites columns (Polymorphic: item_type tells PLACE or OPPORTUNITY)
    public static final String COLUMN_FAV_ITEM_TYPE = "item_type";
    public static final String COLUMN_FAV_ITEM_ID = "item_id";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Creating SQLite database tables...");

        // Create tasks table
        String createTasks = "CREATE TABLE " + TABLE_TASKS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_STUDENT_ID + " INTEGER DEFAULT 1, " +
                COLUMN_TASK_TITLE + " TEXT NOT NULL, " +
                COLUMN_TASK_DESCRIPTION + " TEXT, " +
                COLUMN_TASK_CATEGORY + " TEXT, " +
                COLUMN_TASK_PRIORITY + " TEXT, " +
                COLUMN_TASK_DEADLINE + " TEXT, " +
                COLUMN_TASK_DURATION + " INTEGER, " +
                COLUMN_TASK_STATUS + " TEXT, " +
                COLUMN_TASK_HAS_REMINDER + " INTEGER DEFAULT 0, " +
                COLUMN_TASK_REMINDER_TIME + " TEXT);";
        db.execSQL(createTasks);

        // Create events table
        String createEvents = "CREATE TABLE " + TABLE_EVENTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_STUDENT_ID + " INTEGER DEFAULT 1, " +
                COLUMN_EVENT_TITLE + " TEXT NOT NULL, " +
                COLUMN_EVENT_DATE + " TEXT NOT NULL, " +
                COLUMN_EVENT_START_TIME + " TEXT NOT NULL, " +
                COLUMN_EVENT_END_TIME + " TEXT NOT NULL, " +
                COLUMN_EVENT_LOCATION + " TEXT, " +
                COLUMN_EVENT_TYPE + " TEXT, " +
                COLUMN_EVENT_TASK_ID + " INTEGER DEFAULT -1);";
        db.execSQL(createEvents);

        // Create study_places table
        String createPlaces = "CREATE TABLE " + TABLE_STUDY_PLACES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PLACE_NAME + " TEXT NOT NULL, " +
                COLUMN_PLACE_ADDRESS + " TEXT, " +
                COLUMN_PLACE_PRICE_LEVEL + " INTEGER, " +
                COLUMN_PLACE_NOISE_LEVEL + " INTEGER, " +
                COLUMN_PLACE_WIFI + " INTEGER, " +
                COLUMN_PLACE_OUTLETS + " INTEGER, " +
                COLUMN_PLACE_HOURS + " TEXT, " +
                COLUMN_PLACE_LAT + " REAL, " +
                COLUMN_PLACE_LNG + " REAL, " +
                COLUMN_PLACE_CATEGORY + " TEXT);";
        db.execSQL(createPlaces);

        // Create opportunities table
        String createOpps = "CREATE TABLE " + TABLE_OPPORTUNITIES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_OPP_TITLE + " TEXT NOT NULL, " +
                COLUMN_OPP_DESCRIPTION + " TEXT, " +
                COLUMN_OPP_CATEGORY + " TEXT, " +
                COLUMN_OPP_DEADLINE + " TEXT, " +
                COLUMN_OPP_LINK + " TEXT, " +
                COLUMN_OPP_ORG + " TEXT);";
        db.execSQL(createOpps);

        // Create expenses table
        String createExpenses = "CREATE TABLE " + TABLE_EXPENSES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_STUDENT_ID + " INTEGER DEFAULT 1, " +
                COLUMN_EXPENSE_AMOUNT + " REAL NOT NULL, " +
                COLUMN_EXPENSE_CATEGORY + " TEXT, " +
                COLUMN_EXPENSE_DATE + " TEXT NOT NULL, " +
                COLUMN_EXPENSE_DESCRIPTION + " TEXT);";
        db.execSQL(createExpenses);

        // Create favorites table
        String createFavorites = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_STUDENT_ID + " INTEGER DEFAULT 1, " +
                COLUMN_FAV_ITEM_TYPE + " TEXT NOT NULL, " +
                COLUMN_FAV_ITEM_ID + " INTEGER NOT NULL);";
        db.execSQL(createFavorites);

        // Seed initial data
        SeedData.seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        // Non-destructive schema migration
    }
}
