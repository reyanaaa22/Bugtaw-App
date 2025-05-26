package com.example.bugtaw;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Start the PuzzleActivity when alarm goes off
        Intent puzzleIntent = new Intent(context, PuzzleActivity.class);
        puzzleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(puzzleIntent);
    }
}
