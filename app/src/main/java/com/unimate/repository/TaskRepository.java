package com.unimate.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.unimate.database.DatabaseHelper;
import com.unimate.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    private static final String TAG = "UniMate_TaskRepo";
    private final DatabaseHelper dbHelper;

    public TaskRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(Task task) {
        if (task == null) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = taskToContentValues(task);
        long id = -1;
        try {
            id = db.insert(DatabaseHelper.TABLE_TASKS, null, cv);
            if (id != -1) {
                task.setId(id);
                Log.i(TAG, "Task inserted successfully with ID: " + id);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert task: " + e.getMessage(), e);
        }
        return id;
    }

    public boolean update(Task task) {
        if (task == null || task.getId() <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = taskToContentValues(task);
        try {
            int rows = db.update(DatabaseHelper.TABLE_TASKS, cv,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(task.getId())});
            Log.i(TAG, "Task update status: " + (rows > 0));
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to update task: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean delete(long taskId) {
        if (taskId <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Delete linked STUDY events first, as specified in conception dossier
            db.delete(DatabaseHelper.TABLE_EVENTS,
                    DatabaseHelper.COLUMN_EVENT_TASK_ID + " = ?",
                    new String[]{String.valueOf(taskId)});

            int rows = db.delete(DatabaseHelper.TABLE_TASKS,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(taskId)});

            db.setTransactionSuccessful();
            Log.i(TAG, "Task " + taskId + " and linked study events deleted.");
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to delete task: " + e.getMessage(), e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public Task getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_TASKS, null,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToTask(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching task by ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_TASKS, null, null, null,
                    null, null, DatabaseHelper.COLUMN_TASK_DEADLINE + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    tasks.add(cursorToTask(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all tasks: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return tasks;
    }

    public List<Task> getPendingTasks() {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_TASKS, null,
                    DatabaseHelper.COLUMN_TASK_STATUS + " = ?",
                    new String[]{Task.Status.PENDING.name()}, null, null,
                    DatabaseHelper.COLUMN_TASK_DEADLINE + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    tasks.add(cursorToTask(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching pending tasks: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return tasks;
    }

    public boolean markStatus(long taskId, Task.Status status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_TASK_STATUS, status.name());
        try {
            int rows = db.update(DatabaseHelper.TABLE_TASKS, cv,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(taskId)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error marking task status: " + e.getMessage(), e);
            return false;
        }
    }

    private ContentValues taskToContentValues(Task task) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_STUDENT_ID, task.getStudentId());
        cv.put(DatabaseHelper.COLUMN_TASK_TITLE, task.getTitle());
        cv.put(DatabaseHelper.COLUMN_TASK_DESCRIPTION, task.getDescription());
        cv.put(DatabaseHelper.COLUMN_TASK_CATEGORY, task.getCategory() != null ? task.getCategory().name() : Task.Category.UNIVERSITY.name());
        cv.put(DatabaseHelper.COLUMN_TASK_PRIORITY, task.getPriority() != null ? task.getPriority().name() : Task.Priority.MEDIUM.name());
        cv.put(DatabaseHelper.COLUMN_TASK_DEADLINE, task.getDeadline());
        cv.put(DatabaseHelper.COLUMN_TASK_DURATION, task.getDuration());
        cv.put(DatabaseHelper.COLUMN_TASK_STATUS, task.getStatus() != null ? task.getStatus().name() : Task.Status.PENDING.name());
        cv.put(DatabaseHelper.COLUMN_TASK_HAS_REMINDER, task.isHasReminder() ? 1 : 0);
        cv.put(DatabaseHelper.COLUMN_TASK_REMINDER_TIME, task.getReminderTime());
        return cv;
    }

    private Task cursorToTask(Cursor c) {
        Task t = new Task();
        t.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        t.setStudentId(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STUDENT_ID)));
        t.setTitle(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_TITLE)));
        t.setDescription(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_DESCRIPTION)));

        String cat = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_CATEGORY));
        if (cat != null) {
            try { t.setCategory(Task.Category.valueOf(cat)); } catch (Exception ignored) {}
        }

        String prio = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_PRIORITY));
        if (prio != null) {
            try { t.setPriority(Task.Priority.valueOf(prio)); } catch (Exception ignored) {}
        }

        t.setDeadline(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_DEADLINE)));
        t.setDuration(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_DURATION)));

        String st = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_STATUS));
        if (st != null) {
            try { t.setStatus(Task.Status.valueOf(st)); } catch (Exception ignored) {}
        }

        t.setHasReminder(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_HAS_REMINDER)) == 1);
        t.setReminderTime(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_REMINDER_TIME)));
        return t;
    }
}
