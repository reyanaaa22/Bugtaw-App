package com.example.bugtaw;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bugtaw.adapter.AlarmAdapter;
import com.example.bugtaw.data.Alarm;
import com.example.bugtaw.data.AlarmDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AlarmAdapter.OnAlarmActionListener {
    private static final String TAG = "MainActivity";
    private AlarmDbHelper dbHelper;
    private AlarmAdapter alarmAdapter;
    private RecyclerView alarmRecyclerView;
    private TextView emptyView;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate called");

        // Initialize database helper and alarm manager
        dbHelper = new AlarmDbHelper(this);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Initialize views
        alarmRecyclerView = findViewById(R.id.alarmRecyclerView);
        emptyView = findViewById(R.id.emptyView);
        FloatingActionButton addAlarmFab = findViewById(R.id.addAlarmFab);

        // Setup RecyclerView
        alarmAdapter = new AlarmAdapter(this);
        alarmRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        alarmRecyclerView.setAdapter(alarmAdapter);

        // Set FAB click listener
        addAlarmFab.setOnClickListener(v -> {
            // Check for exact alarm permission on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent);
                    Toast.makeText(this, "Please enable exact alarms permission", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            Intent intent = new Intent(MainActivity.this, AlarmSetupActivity.class);
            startActivity(intent);
        });

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        loadAlarms();
        // Reschedule all enabled alarms
        List<Alarm> alarms = dbHelper.getAllAlarms();
        for (Alarm alarm : alarms) {
            if (alarm.isEnabled()) {
                Log.d(TAG, "Rescheduling alarm: " + alarm.getId() + " for " + alarm.getTimeString());
                scheduleAlarm(alarm);
            }
        }
    }

    private void loadAlarms() {
        List<Alarm> alarms = dbHelper.getAllAlarms();
        Log.d(TAG, "Loaded " + alarms.size() + " alarms from database");
        alarmAdapter.setAlarms(alarms);
        
        // Show/hide empty view
        if (alarms.isEmpty()) {
            alarmRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            alarmRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAlarmToggled(Alarm alarm, boolean isEnabled) {
        Log.d(TAG, "Alarm " + alarm.getId() + " toggled: " + isEnabled);
        dbHelper.toggleAlarmEnabled(alarm.getId(), isEnabled);
        if (isEnabled) {
            scheduleAlarm(alarm);
        } else {
            cancelAlarm(alarm);
        }
    }

    @Override
    public void onAlarmClicked(Alarm alarm) {
        Intent intent = new Intent(this, AlarmSetupActivity.class);
        intent.putExtra("alarm_id", alarm.getId());
        intent.putExtra("hour", alarm.getHour());
        intent.putExtra("minute", alarm.getMinute());
        intent.putExtra("days", alarm.getDays());
        intent.putExtra("puzzle_type", alarm.getPuzzleType());
        startActivity(intent);
    }

    private void scheduleAlarm(Alarm alarm) {
        Log.d(TAG, "Scheduling alarm " + alarm.getId());
        
        // Create intent for AlarmReceiver
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("alarm_id", alarm.getId());
        intent.putExtra("puzzle_type", alarm.getPuzzleType());

        // Create unique pending intent for this alarm
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this,
            (int) alarm.getId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Get current time
        Calendar now = Calendar.getInstance();
        Log.d(TAG, "Current time: " + now.getTime().toString());
        
        // Calculate next alarm time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Log.d(TAG, "Initial alarm time: " + calendar.getTime().toString());

        // If alarm time has passed for today, move to next occurrence
        if (calendar.getTimeInMillis() <= now.getTimeInMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Log.d(TAG, "Alarm time has passed, moved to next day: " + calendar.getTime().toString());
        }

        // Find the next day that is selected
        String[] selectedDays = alarm.getDays().split(",");
        boolean foundNextDay = false;
        int daysChecked = 0;

        while (!foundNextDay && daysChecked < 7) {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            // Convert Calendar.DAY_OF_WEEK to our day numbering (1=Monday, 7=Sunday)
            int ourDayOfWeek = dayOfWeek == Calendar.SUNDAY ? 7 : dayOfWeek - 1;
            
            Log.d(TAG, "Checking day: " + ourDayOfWeek);
            
            for (String day : selectedDays) {
                if (Integer.parseInt(day) == ourDayOfWeek) {
                    foundNextDay = true;
                    Log.d(TAG, "Found matching day: " + ourDayOfWeek);
                    break;
                }
            }
            
            if (!foundNextDay) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                Log.d(TAG, "No match, moved to next day: " + calendar.getTime().toString());
            }
            daysChecked++;
        }

        if (!foundNextDay) {
            Log.e(TAG, "No valid days selected for alarm");
            Toast.makeText(this, "No valid days selected for alarm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Schedule the alarm
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "Setting alarm using setAlarmClock");
                alarmManager.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent),
                    pendingIntent
                );
            } else {
                Log.d(TAG, "Setting alarm using setExact");
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
                );
            }

            // Show more detailed toast message
            String timeString = alarm.getTimeString();
            String dateString = android.text.format.DateFormat.format("EEE, MMM dd", calendar).toString();
            String message = "Alarm set for " + timeString + " on " + dateString;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            Log.d(TAG, message);

        } catch (Exception e) {
            Log.e(TAG, "Error scheduling alarm", e);
            Toast.makeText(this, "Error scheduling alarm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelAlarm(Alarm alarm) {
        Log.d(TAG, "Cancelling alarm " + alarm.getId());
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this,
            (int) alarm.getId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            // Cancel the alarm
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();

            Toast.makeText(this, 
                "Alarm cancelled", 
                Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Alarm successfully cancelled");
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling alarm", e);
        }
    }
}
