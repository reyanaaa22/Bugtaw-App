package com.example.bugtaw;

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

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "BugtawAlarmChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received!");

        // Wake up the device
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK |
            PowerManager.ACQUIRE_CAUSES_WAKEUP |
            PowerManager.ON_AFTER_RELEASE,
            "Bugtaw:AlarmWakeLock"
        );
        wakeLock.acquire(60000); // 60 seconds

        // Create an intent to open the PuzzleActivity
        Intent puzzleIntent = new Intent(context, PuzzleActivity.class);
        puzzleIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        // Get alarm details from intent
        long alarmId = intent.getLongExtra("alarm_id", -1);
        String puzzleType = intent.getStringExtra("puzzle_type");
        Log.d(TAG, "Alarm ID: " + alarmId + ", Puzzle Type: " + puzzleType);
        
        // Pass alarm details to PuzzleActivity
        puzzleIntent.putExtra("alarm_id", alarmId);
        puzzleIntent.putExtra("puzzle_type", puzzleType);

        // Create pending intent for notification
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            (int) alarmId,
            puzzleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Create notification channel for Android 8.0 and above
        createNotificationChannel(context);

        // Get default alarm sound
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmSound == null) {
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        // Build and show notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Wake Up!")
            .setContentText("Time to solve a puzzle!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setVibrate(new long[]{0, 500, 250, 500})
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Make notification persistent
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        Log.d(TAG, "Notification sent!");

        // Release the wake lock
        wakeLock.release();
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
