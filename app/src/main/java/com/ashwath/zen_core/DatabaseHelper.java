package com.ashwath.zen_core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ZenCore.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_SESSIONS = "sessions";
    private static final String COL_ID = "id";
    private static final String COL_DATE = "date";
    private static final String COL_DURATION = "duration"; // In minutes

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_SESSIONS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DATE + " TEXT, " +
                COL_DURATION + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
        onCreate(db);
    }

    // Function to Save a Session
    public boolean addSession(int durationMinutes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Get current date/time
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        values.put(COL_DATE, currentDate);
        values.put(COL_DURATION, durationMinutes);

        long result = db.insert(TABLE_SESSIONS, null, values);
        return result != -1; // Returns true if saved successfully
    }

    // Function to Get All Sessions (For the History Screen)
    public List<String> getAllSessions() {
        List<String> sessions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SESSIONS + " ORDER BY " + COL_ID + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(1);
                int duration = cursor.getInt(2);
                sessions.add("📅 " + date + "   ⏳ " + duration + " mins");
            } while (cursor.moveToNext());
        }
        cursor.close();
        return sessions;
    }
}