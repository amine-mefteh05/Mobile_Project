package com.unimate.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.unimate.database.DatabaseHelper;
import com.unimate.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventRepository {

    private static final String TAG = "UniMate_EventRepo";
    private final DatabaseHelper dbHelper;

    public EventRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(Event event) {
        if (event == null) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = eventToContentValues(event);
        long id = -1;
        try {
            id = db.insert(DatabaseHelper.TABLE_EVENTS, null, cv);
            if (id != -1) {
                event.setId(id);
                Log.i(TAG, "Event inserted successfully with ID: " + id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert event: " + e.getMessage(), e);
        }
        return id;
    }

    public boolean update(Event event) {
        if (event == null || event.getId() <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = eventToContentValues(event);
        try {
            int rows = db.update(DatabaseHelper.TABLE_EVENTS, cv,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(event.getId())});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to update event: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean delete(long eventId) {
        if (eventId <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rows = db.delete(DatabaseHelper.TABLE_EVENTS,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(eventId)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete event: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteByTaskId(long taskId) {
        if (taskId <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rows = db.delete(DatabaseHelper.TABLE_EVENTS,
                    DatabaseHelper.COLUMN_EVENT_TASK_ID + " = ?",
                    new String[]{String.valueOf(taskId)});
            Log.i(TAG, "Deleted " + rows + " study events linked to task " + taskId);
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete events by task ID: " + e.getMessage(), e);
            return false;
        }
    }

    public List<Event> getEventsByDate(String date) {
        List<Event> events = new ArrayList<>();
        if (date == null || date.isEmpty()) return events;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_EVENTS, null,
                    DatabaseHelper.COLUMN_EVENT_DATE + " = ?",
                    new String[]{date}, null, null,
                    DatabaseHelper.COLUMN_EVENT_START_TIME + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    events.add(cursorToEvent(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching events by date: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return events;
    }

    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_EVENTS, null, null, null, null, null,
                    DatabaseHelper.COLUMN_EVENT_DATE + " ASC, " + DatabaseHelper.COLUMN_EVENT_START_TIME + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    events.add(cursorToEvent(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all events: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return events;
    }

    private ContentValues eventToContentValues(Event event) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_STUDENT_ID, event.getStudentId());
        cv.put(DatabaseHelper.COLUMN_EVENT_TITLE, event.getTitle());
        cv.put(DatabaseHelper.COLUMN_EVENT_DATE, event.getDate());
        cv.put(DatabaseHelper.COLUMN_EVENT_START_TIME, event.getStartTime());
        cv.put(DatabaseHelper.COLUMN_EVENT_END_TIME, event.getEndTime());
        cv.put(DatabaseHelper.COLUMN_EVENT_LOCATION, event.getLocation());
        cv.put(DatabaseHelper.COLUMN_EVENT_TYPE, event.getType() != null ? event.getType().name() : Event.EventType.PERSONAL.name());
        cv.put(DatabaseHelper.COLUMN_EVENT_TASK_ID, event.getTaskId());
        return cv;
    }

    private Event cursorToEvent(Cursor c) {
        Event e = new Event();
        e.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        e.setStudentId(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STUDENT_ID)));
        e.setTitle(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_TITLE)));
        e.setDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_DATE)));
        e.setStartTime(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_START_TIME)));
        e.setEndTime(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_END_TIME)));
        e.setLocation(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_LOCATION)));

        String typeStr = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_TYPE));
        if (typeStr != null) {
            try { e.setType(Event.EventType.valueOf(typeStr)); } catch (Exception ignored) {}
        }

        e.setTaskId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EVENT_TASK_ID)));
        return e;
    }
}
