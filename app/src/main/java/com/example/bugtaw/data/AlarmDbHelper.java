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
    private static final int DATABASE_VERSION = 3;

    // Table and column names
    public static final String TABLE_ALARMS = "alarms";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_MINUTE = "minute";
    public static final String COLUMN_DAYS = "days";
    public static final String COLUMN_PUZZLE_TYPE = "puzzle_type";
    public static final String COLUMN_ENABLED = "enabled";
    public static final String COLUMN_LABEL = "label";
    public static final String COLUMN_SOUND = "sound";
    public static final String COLUMN_VIBRATE = "vibrate";

    // Create table statement
    private static final String SQL_CREATE_ALARMS_TABLE =
            "CREATE TABLE " + TABLE_ALARMS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_HOUR + " INTEGER NOT NULL, " +
                    COLUMN_MINUTE + " INTEGER NOT NULL, " +
                    COLUMN_DAYS + " TEXT NOT NULL, " +
                    COLUMN_PUZZLE_TYPE + " TEXT NOT NULL, " +
                    COLUMN_ENABLED + " INTEGER DEFAULT 1, " +
                    COLUMN_LABEL + " TEXT, " +
                    COLUMN_SOUND + " TEXT DEFAULT 'alarm_sound', " +
                    COLUMN_VIBRATE + " INTEGER DEFAULT 1)"; // Default vibrate to true


    public AlarmDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ALARMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_ALARMS + " ADD COLUMN " + COLUMN_LABEL + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_ALARMS + " ADD COLUMN " + COLUMN_SOUND + " TEXT DEFAULT 'alarm_sound'");
        }
        // Always ensure vibrate column exists if upgrading from any version < 3
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_ALARMS + " ADD COLUMN " + COLUMN_VIBRATE + " INTEGER DEFAULT 1");
        }
    }

    // Insert a new alarm
    public long insertAlarm(int hour, int minute, String days, String puzzleType, String label, String sound, boolean vibrate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HOUR, hour);
        values.put(COLUMN_MINUTE, minute);
        values.put(COLUMN_DAYS, days);
        values.put(COLUMN_PUZZLE_TYPE, puzzleType);
        values.put(COLUMN_ENABLED, 1);
        values.put(COLUMN_LABEL, label);
        values.put(COLUMN_SOUND, sound);
        values.put(COLUMN_VIBRATE, vibrate ? 1 : 0);
        return db.insert(TABLE_ALARMS, null, values);
    }

    // Update an existing alarm
    public int updateAlarm(long id, int hour, int minute, String days, String puzzleType, boolean enabled, String label, String sound, boolean vibrate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HOUR, hour);
        values.put(COLUMN_MINUTE, minute);
        values.put(COLUMN_DAYS, days);
        values.put(COLUMN_PUZZLE_TYPE, puzzleType);
        values.put(COLUMN_ENABLED, enabled ? 1 : 0);
        values.put(COLUMN_LABEL, label);
        values.put(COLUMN_SOUND, sound);
        values.put(COLUMN_VIBRATE, vibrate ? 1 : 0);
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

    // Get a single alarm by ID
    public Alarm getAlarm(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_ALARMS, null, COLUMN_ID + "=?", 
            new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Alarm alarm = new Alarm(
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HOUR)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MINUTE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAYS)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUZZLE_TYPE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENABLED)) == 1,
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LABEL)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOUND)),
                cursor.getColumnIndex(COLUMN_VIBRATE) != -1 ? cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VIBRATE)) == 1 : true
            );
            cursor.close();
            return alarm;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    // Delete all alarms
    public void deleteAllAlarms() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ALARMS, null, null);
        db.close();
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
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ENABLED)) == 1,
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LABEL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOUND)),
                    cursor.getColumnIndex(COLUMN_VIBRATE) != -1 ? cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VIBRATE)) == 1 : true
                );
                alarms.add(alarm);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return alarms;
    }
} 