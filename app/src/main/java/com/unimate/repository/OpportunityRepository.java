package com.unimate.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.unimate.database.DatabaseHelper;
import com.unimate.model.Opportunity;

import java.util.ArrayList;
import java.util.List;

public class OpportunityRepository {

    private static final String TAG = "UniMate_OppRepo";
    private final DatabaseHelper dbHelper;

    public OpportunityRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(Opportunity opp) {
        if (opp == null) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = oppToContentValues(opp);
        long id = -1;
        try {
            id = db.insert(DatabaseHelper.TABLE_OPPORTUNITIES, null, cv);
            if (id != -1) opp.setId(id);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting opportunity: " + e.getMessage(), e);
        }
        return id;
    }

    public Opportunity getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_OPPORTUNITIES, null,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToOpportunity(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching opportunity by ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public List<Opportunity> getAllOpportunities() {
        List<Opportunity> opps = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_OPPORTUNITIES, null, null, null, null, null,
                    DatabaseHelper.COLUMN_OPP_DEADLINE + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    opps.add(cursorToOpportunity(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all opportunities: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return opps;
    }

    public List<Opportunity> searchAndFilter(String query, String categoryFilter) {
        List<Opportunity> opps = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder selection = new StringBuilder("1=1");
        List<String> selectionArgs = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            selection.append(" AND (").append(DatabaseHelper.COLUMN_OPP_TITLE).append(" LIKE ? OR ")
                    .append(DatabaseHelper.COLUMN_OPP_DESCRIPTION).append(" LIKE ? OR ")
                    .append(DatabaseHelper.COLUMN_OPP_ORG).append(" LIKE ?)");
            String qArg = "%" + query.trim() + "%";
            selectionArgs.add(qArg);
            selectionArgs.add(qArg);
            selectionArgs.add(qArg);
        }

        if (categoryFilter != null && !categoryFilter.trim().isEmpty() && !"All".equalsIgnoreCase(categoryFilter)) {
            selection.append(" AND ").append(DatabaseHelper.COLUMN_OPP_CATEGORY).append(" = ?");
            selectionArgs.add(categoryFilter.trim());
        }

        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_OPPORTUNITIES, null,
                    selection.toString(),
                    selectionArgs.toArray(new String[0]), null, null,
                    DatabaseHelper.COLUMN_OPP_DEADLINE + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    opps.add(cursorToOpportunity(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error filtering opportunities: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return opps;
    }

    private ContentValues oppToContentValues(Opportunity o) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_OPP_TITLE, o.getTitle());
        cv.put(DatabaseHelper.COLUMN_OPP_DESCRIPTION, o.getDescription());
        cv.put(DatabaseHelper.COLUMN_OPP_CATEGORY, o.getCategory() != null ? o.getCategory().name() : Opportunity.Category.WORKSHOP.name());
        cv.put(DatabaseHelper.COLUMN_OPP_DEADLINE, o.getDeadline());
        cv.put(DatabaseHelper.COLUMN_OPP_LINK, o.getLink());
        cv.put(DatabaseHelper.COLUMN_OPP_ORG, o.getOrganization());
        return cv;
    }

    private Opportunity cursorToOpportunity(Cursor c) {
        Opportunity o = new Opportunity();
        o.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        o.setTitle(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OPP_TITLE)));
        o.setDescription(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OPP_DESCRIPTION)));

        String catStr = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OPP_CATEGORY));
        if (catStr != null) {
            try { o.setCategory(Opportunity.Category.valueOf(catStr)); } catch (Exception ignored) {}
        }

        o.setDeadline(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OPP_DEADLINE)));
        o.setLink(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OPP_LINK)));
        o.setOrganization(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OPP_ORG)));
        return o;
    }
}
