package com.unimate.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.unimate.database.DatabaseHelper;
import com.unimate.model.Expense;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository {

    private static final String TAG = "UniMate_ExpenseRepo";
    private final DatabaseHelper dbHelper;

    public ExpenseRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(Expense expense) {
        if (expense == null) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = expenseToContentValues(expense);
        long id = -1;
        try {
            id = db.insert(DatabaseHelper.TABLE_EXPENSES, null, cv);
            if (id != -1) expense.setId(id);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting expense: " + e.getMessage(), e);
        }
        return id;
    }

    public boolean delete(long id) {
        if (id <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rows = db.delete(DatabaseHelper.TABLE_EXPENSES,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting expense: " + e.getMessage(), e);
            return false;
        }
    }

    public List<Expense> getAllExpenses() {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_EXPENSES, null, null, null, null, null,
                    DatabaseHelper.COLUMN_EXPENSE_DATE + " DESC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    expenses.add(cursorToExpense(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching expenses: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return expenses;
    }

    public List<Expense> getCurrentWeekExpenses() {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(DayOfWeek.MONDAY);
            LocalDate sunday = monday.plusDays(6);

            String mondayStr = monday.toString();
            String sundayStr = sunday.toString();

            Cursor cursor = db.query(DatabaseHelper.TABLE_EXPENSES, null,
                    DatabaseHelper.COLUMN_EXPENSE_DATE + " >= ? AND " + DatabaseHelper.COLUMN_EXPENSE_DATE + " <= ?",
                    new String[]{mondayStr, sundayStr}, null, null,
                    DatabaseHelper.COLUMN_EXPENSE_DATE + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    expenses.add(cursorToExpense(cursor));
                } while (cursor.moveToNext());
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error fetching current week expenses: " + e.getMessage(), e);
        }
        return expenses;
    }

    private ContentValues expenseToContentValues(Expense e) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_STUDENT_ID, e.getStudentId());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_AMOUNT, e.getAmount());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_CATEGORY, e.getCategory() != null ? e.getCategory().name() : Expense.Category.FOOD.name());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_DATE, e.getDate());
        cv.put(DatabaseHelper.COLUMN_EXPENSE_DESCRIPTION, e.getDescription());
        return cv;
    }

    private Expense cursorToExpense(Cursor c) {
        Expense e = new Expense();
        e.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        e.setStudentId(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STUDENT_ID)));
        e.setAmount(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_AMOUNT)));

        String catStr = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_CATEGORY));
        if (catStr != null) {
            try { e.setCategory(Expense.Category.valueOf(catStr)); } catch (Exception ignored) {}
        }

        e.setDate(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_DATE)));
        e.setDescription(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXPENSE_DESCRIPTION)));
        return e;
    }
}
