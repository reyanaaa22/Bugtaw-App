package com.example.bugtaw;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "BugtawAlarmChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Create an intent to open the PuzzleActivity
        Intent puzzleIntent = new Intent(context, PuzzleActivity.class);
        puzzleIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        // Get alarm details from intent
        long alarmId = intent.getLongExtra("alarm_id", -1);
        String puzzleType = intent.getStringExtra("puzzle_type");
        
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

        // Build and show notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Wake Up!")
            .setContentText("Time to solve a puzzle!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);

        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Bugtaw Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for Bugtaw alarm notifications");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 250, 500});
            channel.setBypassDnd(true);

            NotificationManager notificationManager = 
                context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
