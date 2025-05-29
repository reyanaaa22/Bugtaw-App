package com.example.bugtaw;

import android.app.NotificationManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bugtaw.data.Alarm;
import com.example.bugtaw.data.AlarmDbHelper;

import java.util.Random;

public class PuzzleActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private TextView puzzleText;
    private EditText answerInput;
    private Button submitButton;
    private String puzzleType;
    private int correctAnswer;
    private long alarmId;
    private AlarmDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        // Keep screen on and show above lock screen
        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        // Initialize views
        puzzleText = findViewById(R.id.puzzleText);
        answerInput = findViewById(R.id.answerInput);
        submitButton = findViewById(R.id.submitButton);

        // Initialize database helper
        dbHelper = new AlarmDbHelper(this);

        // Get puzzle type and alarm ID from intent
        puzzleType = getIntent().getStringExtra("puzzle_type");
        alarmId = getIntent().getLongExtra("alarm_id", -1);
        String sound = getIntent().getStringExtra("sound");
        
        if (puzzleType == null) {
            puzzleType = "Math Problem";
        }

        // Start alarm sound
        startAlarmSound(sound);

        // Generate puzzle based on type
        generatePuzzle();

        // Set submit button click listener
        submitButton.setOnClickListener(v -> checkAnswer());
    }

    private void startAlarmSound(String sound) {
        try {
            // Try to create and start the MediaPlayer with selected sound
            int soundResourceId = getResources().getIdentifier(
                sound, "raw", getPackageName()
            );
            
            if (soundResourceId != 0) {
                mediaPlayer = MediaPlayer.create(this, soundResourceId);
            }
            
            // If custom sound fails or not found, try default alarm sound
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
            }
            
            // If default alarm sound fails, use system default
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_RINGTONE_URI);
            }

            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not play alarm sound", Toast.LENGTH_SHORT).show();
        }
    }

    private void generatePuzzle() {
        switch (puzzleType) {
            case "Math Problem":
                generateMathPuzzle();
                break;
            case "Memory Recall":
                // TODO: Implement memory puzzle
                generateMathPuzzle(); // Temporary fallback
                break;
            case "Pattern Tap":
                // TODO: Implement pattern puzzle
                generateMathPuzzle(); // Temporary fallback
                break;
            default:
                generateMathPuzzle();
                break;
        }
    }

    private void generateMathPuzzle() {
        Random random = new Random();
        int num1 = random.nextInt(50) + 1;
        int num2 = random.nextInt(50) + 1;
        int operation = random.nextInt(3);
        String operationSymbol;

        switch (operation) {
            case 0:
                correctAnswer = num1 + num2;
                operationSymbol = "+";
                break;
            case 1:
                correctAnswer = num1 - num2;
                operationSymbol = "-";
                break;
            default:
                correctAnswer = num1 * num2;
                operationSymbol = "×";
                break;
        }

        puzzleText.setText(String.format("%d %s %d = ?", num1, operationSymbol, num2));
    }

    private void checkAnswer() {
        String userAnswer = answerInput.getText().toString();
        if (userAnswer.isEmpty()) {
            Toast.makeText(this, "Please enter an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int answer = Integer.parseInt(userAnswer);
            if (answer == correctAnswer) {
                stopAlarmAndFinish();
            } else {
                Toast.makeText(this, "Wrong answer, try again!", Toast.LENGTH_SHORT).show();
                answerInput.setText("");
                generatePuzzle(); // Generate new puzzle on wrong answer
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAlarmAndFinish() {
        // Stop the alarm sound
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Disable the alarm in the database
        if (alarmId != -1) {
            dbHelper.toggleAlarmEnabled(alarmId, false);
        }

        // Cancel the notification
        NotificationManager notificationManager = 
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1); // Using the same NOTIFICATION_ID as in AlarmReceiver

        // Show success message
        Toast.makeText(this, R.string.correct, Toast.LENGTH_SHORT).show();

        // Close the activity
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button
    }
}
