package com.example.bugtaw.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class AlarmDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "alarms.db";
    private static final int DATABASE_VERSION = 1;

    // Table and column names
    public static final String TABLE_ALARMS = "alarms";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_MINUTE = "minute";
    public static final String COLUMN_DAYS = "days";
    public static final String COLUMN_PUZZLE_TYPE = "puzzle_type";
    public static final String COLUMN_ENABLED = "enabled";

    // Create table statement
    private static final String SQL_CREATE_ALARMS_TABLE =
            "CREATE TABLE " + TABLE_ALARMS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_HOUR + " INTEGER NOT NULL, " +
                    COLUMN_MINUTE + " INTEGER NOT NULL, " +
                    COLUMN_DAYS + " TEXT NOT NULL, " +
                    COLUMN_PUZZLE_TYPE + " TEXT NOT NULL, " +
                    COLUMN_ENABLED + " INTEGER DEFAULT 1)";

    public AlarmDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ALARMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For now, simply drop the table and create a new one
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMS);
        onCreate(db);
    }

    // Insert a new alarm
    public long insertAlarm(int hour, int minute, String days, String puzzleType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HOUR, hour);
        values.put(COLUMN_MINUTE, minute);
        values.put(COLUMN_DAYS, days);
        values.put(COLUMN_PUZZLE_TYPE, puzzleType);
        values.put(COLUMN_ENABLED, 1);
        return db.insert(TABLE_ALARMS, null, values);
    }

    // Update an existing alarm
    public int updateAlarm(long id, int hour, int minute, String days, String puzzleType, boolean enabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HOUR, hour);
        values.put(COLUMN_MINUTE, minute);
        values.put(COLUMN_DAYS, days);
        values.put(COLUMN_PUZZLE_TYPE, puzzleType);
        values.put(COLUMN_ENABLED, enabled ? 1 : 0);
        return db.update(TABLE_ALARMS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Delete an alarm
    public void deleteAlarm(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ALARMS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Toggle alarm enabled status
    public void toggleAlarmEnabled(long id, boolean enabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ENABLED, enabled ? 1 : 0);
        db.update(TABLE_ALARMS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Get all alarms
    public List<Alarm> getAllAlarms() {
        List<Alarm> alarms = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_ALARMS, null, null, null, null, null, 
                COLUMN_HOUR + "," + COLUMN_MINUTE + " ASC");

        if (cursor.moveToFirst()) {
            do {
                Alarm alarm = new Alarm(
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HOUR)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MINUTE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAYS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUZZLE_TYPE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENABLED)) == 1
                );
                alarms.add(alarm);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return alarms;
    }
} 