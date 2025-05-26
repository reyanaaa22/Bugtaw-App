package com.example.bugtaw;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TimePicker timePicker;
    private Button setAlarmButton;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timePicker = findViewById(R.id.timePicker);
        setAlarmButton = findViewById(R.id.setAlarmButton);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        setAlarmButton.setOnClickListener(v -> {
            try {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                // Create an intent to start PuzzleActivity
                Intent intent = new Intent(MainActivity.this, PuzzleActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                    MainActivity.this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                );

                // Calculate trigger time
                long triggerTime = System.currentTimeMillis();
                triggerTime += (hour * 60 * 60 * 1000) + (minute * 60 * 1000);

                // Check if we have the exact alarm permission on Android 12+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!alarmManager.canScheduleExactAlarms()) {
                        Toast.makeText(this, "Please enable exact alarms permission in settings", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                // Set the alarm
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                Toast.makeText(this, "Alarm set for " + hour + ":" + minute, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error setting alarm: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
