package com.example.lostconnect;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "lost_found.db";
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "items";

    public static final String COL_ID = "id";
    public static final String COL_TYPE = "type";
    public static final String COL_TITLE = "name";
    public static final String COL_CATEGORY = "category";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_LOCATION = "address";
    public static final String COL_CONTACT = "phone";
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";
    public static final String COL_IMAGE = "image";
    public static final String COL_DATE = "date";
    public static final String COL_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TYPE + " TEXT, " +
                COL_TITLE + " TEXT, " +
                COL_CATEGORY + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_LOCATION + " TEXT, " +
                COL_LATITUDE + " REAL, " +
                COL_LONGITUDE + " REAL, " +
                COL_CONTACT + " TEXT, " +
                COL_IMAGE + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_TIMESTAMP + " INTEGER)";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addItem(String type, String title, String category, String description,
                           String address, double latitude, double longitude,
                           String contact, String imageUri) {

        SQLiteDatabase db = this.getWritableDatabase();

        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new Date());
        long timestamp = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put(COL_TYPE, type);
        values.put(COL_TITLE, title);
        values.put(COL_CATEGORY, category);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_LOCATION, address);
        values.put(COL_LATITUDE, latitude);
        values.put(COL_LONGITUDE, longitude);
        values.put(COL_CONTACT, contact);
        values.put(COL_IMAGE, imageUri);
        values.put(COL_DATE, date);
        values.put(COL_TIMESTAMP, timestamp);

        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }

    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_ID + " DESC", null);
    }

    public Cursor getItemsByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();

        if (category.equals("All")) {
            return getAllItems();
        }

        return db.rawQuery("SELECT * FROM " + TABLE_NAME +
                        " WHERE " + COL_CATEGORY + " = ? ORDER BY " + COL_ID + " DESC",
                new String[]{category});
    }

    public Cursor getItemById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery("SELECT * FROM " + TABLE_NAME +
                        " WHERE " + COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public boolean deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(TABLE_NAME, COL_ID + " = ?",
                new String[]{String.valueOf(id)});

        return result > 0;
    }
}
