package com.example.bugtaw;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.bugtaw.data.Alarm;
import com.example.bugtaw.data.AlarmDbHelper;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "BugtawAlarmChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received at: " + new java.util.Date().toString());

        // Wake up the device
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK |
            PowerManager.ACQUIRE_CAUSES_WAKEUP |
            PowerManager.ON_AFTER_RELEASE,
            "Bugtaw:AlarmWakeLock"
        );
        wakeLock.acquire(60000); // 60 seconds

        try {
            // Get alarm details from intent
            long alarmId = intent.getLongExtra("alarm_id", -1);
            String puzzleType = intent.getStringExtra("puzzle_type");
            Log.d(TAG, "Processing alarm ID: " + alarmId + ", Puzzle Type: " + puzzleType);

            // Get the alarm from database
            AlarmDbHelper dbHelper = new AlarmDbHelper(context);
            Alarm alarm = dbHelper.getAlarm(alarmId);

            if (alarm != null && alarm.isEnabled()) {
                Log.d(TAG, "Found active alarm: " + alarm.getTimeString());
                // Show notification
                showNotification(context, alarm);
                
                // Schedule next alarm
                scheduleNextAlarm(context, alarm);
            } else {
                Log.w(TAG, "Alarm not found or not enabled. ID: " + alarmId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing alarm", e);
        } finally {
            // Release the wake lock
            wakeLock.release();
        }
    }

    private void showNotification(Context context, Alarm alarm) {
        Log.d(TAG, "Showing notification for alarm: " + alarm.getId());
        try {
            // Create notification channel for Android 8.0 and above
            createNotificationChannel(context);

            // Create an intent to open the PuzzleActivity
            Intent puzzleIntent = new Intent(context, PuzzleActivity.class);
            puzzleIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
            
            // Pass alarm details to PuzzleActivity
            puzzleIntent.putExtra("alarm_id", alarm.getId());
            puzzleIntent.putExtra("puzzle_type", alarm.getPuzzleType());

            // Create pending intent for notification
            PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) alarm.getId(),
                puzzleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Get default alarm sound
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) {
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            // Build notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Wake Up!")
                .setContentText("Time to solve a puzzle!")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(false)  // Changed to false to make it persistent
                .setSound(alarmSound)
                .setVibrate(new long[]{0, 500, 250, 500})
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            // Show notification
            NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Notification sent successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification", e);
        }
    }

    private void scheduleNextAlarm(Context context, Alarm alarm) {
        // Create intent for next alarm
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarm_id", alarm.getId());
        intent.putExtra("puzzle_type", alarm.getPuzzleType());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            (int) alarm.getId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Calculate next alarm time
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1); // Start checking from tomorrow
        calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
        calendar.set(Calendar.MINUTE, alarm.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Find the next day that is selected
        String[] selectedDays = alarm.getDays().split(",");
        boolean foundNextDay = false;
        int daysChecked = 0;

        while (!foundNextDay && daysChecked < 7) {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            // Convert Calendar.DAY_OF_WEEK to our day numbering (1=Monday, 7=Sunday)
            int ourDayOfWeek = dayOfWeek == Calendar.SUNDAY ? 7 : dayOfWeek - 1;
            
            for (String day : selectedDays) {
                if (Integer.parseInt(day) == ourDayOfWeek) {
                    foundNextDay = true;
                    break;
                }
            }
            
            if (!foundNextDay) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            daysChecked++;
        }

        if (foundNextDay) {
            // Get AlarmManager and schedule the alarm
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent),
                    pendingIntent
                );
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
                );
            }
            
            String timeString = alarm.getTimeString();
            String dateString = android.text.format.DateFormat.format("EEE, MMM dd", calendar).toString();
            Log.d(TAG, "Next alarm scheduled for " + timeString + " on " + dateString);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Bugtaw Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            );
            
            // Configure channel
            channel.setDescription("Channel for Bugtaw alarm notifications");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 250, 500});
            channel.setBypassDnd(true);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            
            // Set up audio attributes
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build();
            channel.setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                audioAttributes
            );

            NotificationManager notificationManager = 
                context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
