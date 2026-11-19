package com.unimate.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.unimate.database.DatabaseHelper;
import com.unimate.model.Favorite;

import java.util.ArrayList;
import java.util.List;

public class FavoriteRepository {

    private static final String TAG = "UniMate_FavRepo";
    private final DatabaseHelper dbHelper;

    public FavoriteRepository(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public boolean addFavorite(Favorite.FavoriteType itemType, long itemId) {
        if (itemType == null || itemId <= 0) return false;
        if (isFavorite(itemType, itemId)) return true; // Already favorite

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_STUDENT_ID, 1);
        cv.put(DatabaseHelper.COLUMN_FAV_ITEM_TYPE, itemType.name());
        cv.put(DatabaseHelper.COLUMN_FAV_ITEM_ID, itemId);

        try {
            long id = db.insert(DatabaseHelper.TABLE_FAVORITES, null, cv);
            Log.i(TAG, "Added favorite " + itemType + ":" + itemId);
            return id != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error adding favorite: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean removeFavorite(Favorite.FavoriteType itemType, long itemId) {
        if (itemType == null || itemId <= 0) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            int rows = db.delete(DatabaseHelper.TABLE_FAVORITES,
                    DatabaseHelper.COLUMN_FAV_ITEM_TYPE + " = ? AND " + DatabaseHelper.COLUMN_FAV_ITEM_ID + " = ?",
                    new String[]{itemType.name(), String.valueOf(itemId)});
            Log.i(TAG, "Removed favorite " + itemType + ":" + itemId);
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error removing favorite: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean isFavorite(Favorite.FavoriteType itemType, long itemId) {
        if (itemType == null || itemId <= 0) return false;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_FAVORITES, new String[]{DatabaseHelper.COLUMN_ID},
                    DatabaseHelper.COLUMN_FAV_ITEM_TYPE + " = ? AND " + DatabaseHelper.COLUMN_FAV_ITEM_ID + " = ?",
                    new String[]{itemType.name(), String.valueOf(itemId)}, null, null, null);
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking favorite: " + e.getMessage(), e);
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public List<Long> getFavoriteItemIds(Favorite.FavoriteType itemType) {
        List<Long> ids = new ArrayList<>();
        if (itemType == null) return ids;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_FAVORITES, new String[]{DatabaseHelper.COLUMN_FAV_ITEM_ID},
                    DatabaseHelper.COLUMN_FAV_ITEM_TYPE + " = ?",
                    new String[]{itemType.name()}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ids.add(cursor.getLong(0));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting favorite item IDs: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return ids;
    }
}
