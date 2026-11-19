package com.unimate.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.unimate.database.DatabaseHelper;
import com.unimate.model.StudyPlace;

import java.util.ArrayList;
import java.util.List;

public class StudySpotRepository {

    private static final String TAG = "UniMate_StudySpotRepo";
    private final DatabaseHelper dbHelper;

    public StudySpotRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insert(StudyPlace place) {
        if (place == null) return -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = placeToContentValues(place);
        long id = -1;
        try {
            id = db.insert(DatabaseHelper.TABLE_STUDY_PLACES, null, cv);
            if (id != -1) place.setId(id);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting study place: " + e.getMessage(), e);
        }
        return id;
    }

    public StudyPlace getById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_STUDY_PLACES, null,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToStudyPlace(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching study place by ID: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }

    public List<StudyPlace> getAllPlaces() {
        List<StudyPlace> places = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_STUDY_PLACES, null, null, null, null, null,
                    DatabaseHelper.COLUMN_PLACE_NAME + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    places.add(cursorToStudyPlace(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching all study places: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return places;
    }

    public List<StudyPlace> searchAndFilter(String query, String categoryFilter) {
        List<StudyPlace> places = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder selection = new StringBuilder("1=1");
        List<String> selectionArgs = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            selection.append(" AND (").append(DatabaseHelper.COLUMN_PLACE_NAME).append(" LIKE ? OR ")
                    .append(DatabaseHelper.COLUMN_PLACE_ADDRESS).append(" LIKE ?)");
            String qArg = "%" + query.trim() + "%";
            selectionArgs.add(qArg);
            selectionArgs.add(qArg);
        }

        if (categoryFilter != null && !categoryFilter.trim().isEmpty() && !"All".equalsIgnoreCase(categoryFilter)) {
            selection.append(" AND ").append(DatabaseHelper.COLUMN_PLACE_CATEGORY).append(" = ?");
            selectionArgs.add(categoryFilter.trim());
        }

        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_STUDY_PLACES, null,
                    selection.toString(),
                    selectionArgs.toArray(new String[0]), null, null,
                    DatabaseHelper.COLUMN_PLACE_NAME + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    places.add(cursorToStudyPlace(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error filtering study places: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return places;
    }

    private ContentValues placeToContentValues(StudyPlace p) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_PLACE_NAME, p.getName());
        cv.put(DatabaseHelper.COLUMN_PLACE_ADDRESS, p.getAddress());
        cv.put(DatabaseHelper.COLUMN_PLACE_PRICE_LEVEL, p.getPriceLevel());
        cv.put(DatabaseHelper.COLUMN_PLACE_NOISE_LEVEL, p.getNoiseLevel());
        cv.put(DatabaseHelper.COLUMN_PLACE_WIFI, p.isWifi() ? 1 : 0);
        cv.put(DatabaseHelper.COLUMN_PLACE_OUTLETS, p.isOutlets() ? 1 : 0);
        cv.put(DatabaseHelper.COLUMN_PLACE_HOURS, p.getOpeningHours());
        cv.put(DatabaseHelper.COLUMN_PLACE_LAT, p.getLatitude());
        cv.put(DatabaseHelper.COLUMN_PLACE_LNG, p.getLongitude());
        cv.put(DatabaseHelper.COLUMN_PLACE_CATEGORY, p.getCategory());
        return cv;
    }

    private StudyPlace cursorToStudyPlace(Cursor c) {
        StudyPlace p = new StudyPlace();
        p.setId(c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        p.setName(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLACE_NAME)));
        p.setAddress(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLACE_ADDRESS)));
        p.setPriceLevel(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLACE_PRICE_LEVEL)));
        p.setNoiseLevel(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLACE_NOISE_LEVEL)));
        p.setWifi(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLACE_WIFI)) == 1);
        p.setOutlets(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLACE_OUTLETS)) == 1);
        p.setOpeningHours(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLACE_HOURS)));
        p.setLatitude(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLACE_LAT)));
        p.setLongitude(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLACE_LNG)));
        p.setCategory(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLACE_CATEGORY)));
        return p;
    }
}
