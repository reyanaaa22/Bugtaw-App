package com.example.bugtaw;

import android.widget.LinearLayout;
import android.graphics.Color;
import android.os.Handler;

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

    private String memorySequence;
    private int memoryStep;
    private String patternSequence;

    private void generatePuzzle() {
        switch (puzzleType) {
            case "Math Problem":
                generateMathPuzzle();
                break;
            case "Memory Recall":
                generateMemoryRecallPuzzle();
                break;
            case "Pattern Tap":
                generatePatternTapPuzzle();
                break;
            default:
                generateMathPuzzle();
                break;
        }
    }

    private void generateMemoryRecallPuzzle() {
        // Generate a random 5-digit number and ask user to recall
        Random random = new Random();
        int number = 10000 + random.nextInt(90000);
        memorySequence = String.valueOf(number);
        puzzleText.setText("Memorize this number: " + memorySequence);
        answerInput.setVisibility(View.GONE);
        submitButton.setText("Next");
        submitButton.setOnClickListener(v -> {
            puzzleText.setText("Enter the number you just saw:");
            answerInput.setText("");
            answerInput.setVisibility(View.VISIBLE);
            submitButton.setText("Submit");
            submitButton.setOnClickListener(v2 -> {
                String userAnswer = answerInput.getText().toString();
                if (userAnswer.equals(memorySequence)) {
                    stopAlarmAndFinish();
                } else {
                    Toast.makeText(this, "Wrong! Try again.", Toast.LENGTH_SHORT).show();
                    // Generate a new number and repeat
                    generateMemoryRecallPuzzle();
                }
            });
        });
    }

    // Pattern Tap Puzzle variables
    private LinearLayout patternContainer;
    private StringBuilder userPatternInput = new StringBuilder();
    private String[] colorPattern;
    private String[] colorOptions = {"RED", "GREEN", "BLUE", "YELLOW"};

    private void generatePatternTapPuzzle() {
        // Generate a random pattern of 4 colors
        Random random = new Random();
        colorPattern = new String[4];
        for (int i = 0; i < 4; i++) {
            colorPattern[i] = colorOptions[random.nextInt(colorOptions.length)];
        }
        userPatternInput.setLength(0);
        puzzleText.setText("Repeat this pattern:");
        answerInput.setVisibility(View.GONE);
        showPatternToUser();
    }

    private void showPatternToUser() {
        // Show the pattern visually (colored buttons)
        if (patternContainer == null) {
            patternContainer = findViewById(R.id.patternContainer);
        }
        patternContainer.removeAllViews();
        for (String color : colorPattern) {
            View colorView = new View(this);
            int size = (int) getResources().getDimension(R.dimen.pattern_dot_size);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(16, 16, 16, 16);
            colorView.setLayoutParams(params);
            switch (color) {
                case "RED": colorView.setBackgroundColor(Color.RED); break;
                case "GREEN": colorView.setBackgroundColor(Color.GREEN); break;
                case "BLUE": colorView.setBackgroundColor(Color.BLUE); break;
                case "YELLOW": colorView.setBackgroundColor(Color.YELLOW); break;
            }
            patternContainer.addView(colorView);
        }
        // After a delay, hide pattern and show tap buttons
        new Handler().postDelayed(this::showPatternTapButtons, 1500);
    }

    private void showPatternTapButtons() {
        patternContainer.removeAllViews();
        // Create color tap buttons
        for (String color : colorOptions) {
            Button btn = new Button(this);
            btn.setText("");
            int size = (int) getResources().getDimension(R.dimen.pattern_dot_size);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(16, 16, 16, 16);
            btn.setLayoutParams(params);
            switch (color) {
                case "RED": btn.setBackgroundColor(Color.RED); break;
                case "GREEN": btn.setBackgroundColor(Color.GREEN); break;
                case "BLUE": btn.setBackgroundColor(Color.BLUE); break;
                case "YELLOW": btn.setBackgroundColor(Color.YELLOW); break;
            }
            btn.setOnClickListener(v -> {
                userPatternInput.append(color);
                if (userPatternInput.length() / 3 == colorPattern.length) {
                    checkPatternTapAnswer();
                }
            });
            patternContainer.addView(btn);
        }
    }

    private void checkPatternTapAnswer() {
        StringBuilder correctPattern = new StringBuilder();
        for (String color : colorPattern) correctPattern.append(color);
        if (userPatternInput.toString().equals(correctPattern.toString())) {
            stopAlarmAndFinish();
        } else {
            Toast.makeText(this, "Wrong pattern! Try again.", Toast.LENGTH_SHORT).show();
            generatePatternTapPuzzle();
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
