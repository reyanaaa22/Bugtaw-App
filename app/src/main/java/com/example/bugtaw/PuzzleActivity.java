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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;


import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bugtaw.data.Alarm;
import com.example.bugtaw.data.AlarmDbHelper;

import java.util.Random;

public class PuzzleActivity extends AppCompatActivity {
    public static MediaPlayer sharedMediaPlayer = null;
    public static String sharedSound = null;
    private MediaPlayer mediaPlayer; // For legacy, but use sharedMediaPlayer
    private TextView puzzleText;
    private EditText answerInput;
    private Button submitButton;
    private String puzzleType;
    private int correctAnswer;
    private long alarmId;
    private AlarmDbHelper dbHelper;

    // --- Puzzle fields for Pattern Tap and Memory Recall ---
    private Button startMemoryButton;
    private String memorySequence;
    private String[] colorPattern;
    private final String[] colorOptions = {"RED", "GREEN", "BLUE", "YELLOW"};
    private final List<String> userPatternInput = new ArrayList<>();
    private LinearLayout patternContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get puzzle type and alarm ID from intent
        puzzleType = getIntent().getStringExtra("puzzle_type");
        alarmId = getIntent().getLongExtra("alarm_id", -1);
        String sound = getIntent().getStringExtra("sound");
        
        if (puzzleType == null) {
            puzzleType = "Math Problem";
        }

        // Only start alarm sound if not already playing the correct sound
        if (sharedMediaPlayer == null || sharedSound == null || !sharedSound.equals(sound)) {
            // Stop previous sound if different
            if (sharedMediaPlayer != null) {
                try {
                    if (sharedMediaPlayer.isPlaying()) {
                        sharedMediaPlayer.stop();
                    }
                    sharedMediaPlayer.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sharedMediaPlayer = null;
                sharedSound = null;
            }
            sharedMediaPlayer = createAndStartMediaPlayer(sound);
            sharedSound = sound;
        }

        // Show only the popup for Pattern Tap on notification click
        if ("Pattern Tap".equals(puzzleType)) {
            // Keep screen on and show above lock screen
            getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
            // Show the popup as the ONLY UI
            View popupView = getLayoutInflater().inflate(R.layout.alarm_pattern_popup, null);
            androidx.appcompat.app.AlertDialog popupDialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setView(popupView)
                    .setCancelable(false)
                    .create();
            popupDialog.show();

            Button startBtn = popupView.findViewById(R.id.btn_start_pattern_puzzle);
            startBtn.setOnClickListener(v -> {
                popupDialog.dismiss();
                // Now show the puzzle UI and start the Pattern Tap Puzzle
                setContentView(R.layout.activity_puzzle);
                // Re-initialize views after setContentView
                puzzleText = findViewById(R.id.puzzleText);
                answerInput = findViewById(R.id.answerInput);
                submitButton = findViewById(R.id.submitButton);
                dbHelper = new AlarmDbHelper(this);
                // Show puzzle UI
                puzzleText.setVisibility(View.VISIBLE);
                if (findViewById(R.id.patternContainer) != null)
                    findViewById(R.id.patternContainer).setVisibility(View.VISIBLE);
                generatePatternTapPuzzle();
            });
        } else {
            // For other puzzle types, show the normal puzzle layout
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
            dbHelper = new AlarmDbHelper(this);
            generatePuzzle();
            submitButton.setOnClickListener(v -> checkAnswer());
        }
    }

    private MediaPlayer createAndStartMediaPlayer(String sound) {
        try {
            MediaPlayer player = null;
            if (sound != null && (sound.startsWith("content://") || sound.startsWith("file://"))) {
                player = MediaPlayer.create(this, android.net.Uri.parse(sound));
            } else if (sound != null && !sound.isEmpty()) {
                int soundResourceId = getResources().getIdentifier(sound, "raw", getPackageName());
                if (soundResourceId != 0) {
                    player = MediaPlayer.create(this, soundResourceId);
                }
            }
            if (player != null) {
                player.setLooping(true);
                player.start();
            }
            return player;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Could not play alarm sound", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

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
        answerInput.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
        if (startMemoryButton == null) {
            startMemoryButton = new Button(this);
            startMemoryButton.setText("Start Puzzle");
            LinearLayout layout = findViewById(R.id.puzzleLayout);
            layout.addView(startMemoryButton);
        }
        startMemoryButton.setVisibility(View.VISIBLE);
        puzzleText.setText("Press 'Start Puzzle' to memorize the number.");

        startMemoryButton.setOnClickListener(v -> {
            startMemoryButton.setVisibility(View.GONE);

            // Generate a random 6-digit number sequence
            Random random = new Random();
            int number = 100000 + random.nextInt(900000);
            memorySequence = String.valueOf(number);

            // Show the sequence to memorize
            puzzleText.setText("Memorize this number:\n\n" + memorySequence);

            // After 5 seconds, hide the number and show input
            new Handler().postDelayed(() -> {
                puzzleText.setText("Enter the number you just saw:");
                answerInput.setText("");
                answerInput.setVisibility(View.VISIBLE);
                submitButton.setText("Submit");
                submitButton.setVisibility(View.VISIBLE);

                submitButton.setOnClickListener(v2 -> {
                    String userAnswer = answerInput.getText().toString().trim();
                    if (userAnswer.equals(memorySequence)) {
                        Toast.makeText(this, "Correct! Alarm stopped.", Toast.LENGTH_SHORT).show();
                        stopAlarmAndFinish();
                    } else {
                        Toast.makeText(this, "Wrong! Try again.", Toast.LENGTH_SHORT).show();
                        // Restart the memory recall puzzle with a new number
                        generateMemoryRecallPuzzle();
                    }
                });
            }, 5000); // show number for 5 seconds
        });
    }

    private void generatePatternTapPuzzle() {
        Random random = new Random();
        colorPattern = new String[4];
        for (int i = 0; i < 4; i++) {
            colorPattern[i] = colorOptions[random.nextInt(colorOptions.length)];
        }
        userPatternInput.clear();
        puzzleText.setText("Repeat this pattern:");
        answerInput.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);  // Hide submit button since user taps colors
        if (patternContainer == null) {
            patternContainer = findViewById(R.id.patternContainer);
        }
        showPatternToUser();
    }

    private void showPatternToUser() {
        if (patternContainer == null) {
            patternContainer = findViewById(R.id.patternContainer);
        }
        patternContainer.removeAllViews();

        Handler handler = new Handler();
        int colorShowDuration = 2500; // 2.5 seconds
        int colorGapDuration = 1000;  // 1 second gap
        int size = (int) getResources().getDimension(R.dimen.pattern_dot_size);

        long currentTime = 0;
        for (int i = 0; i < colorPattern.length; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                patternContainer.removeAllViews();
                View colorView = new View(this);
                int color = Color.TRANSPARENT;
                switch (colorPattern[index]) {
                    case "RED": color = Color.RED; break;
                    case "GREEN": color = Color.GREEN; break;
                    case "BLUE": color = Color.BLUE; break;
                    case "YELLOW": color = Color.YELLOW; break;
                }
                colorView.setBackgroundColor(color);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                params.setMargins(16, 16, 16, 16);
                colorView.setLayoutParams(params);
                patternContainer.addView(colorView);
            }, currentTime);
            currentTime += colorShowDuration;
            handler.postDelayed(() -> {
                patternContainer.removeAllViews();
            }, currentTime);
            currentTime += colorGapDuration;
        }
        handler.postDelayed(() -> {
            showPatternTapButtons();
        }, currentTime);
    }

    private void showPatternTapButtons() {
        patternContainer.removeAllViews();
        for (String color : colorOptions) {
            Button btn = new Button(this);
            btn.setText("");  // No text, just color
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
                userPatternInput.add(color);
                if (userPatternInput.size() == colorPattern.length) {
                    checkPatternTapAnswer();
                }
            });
            patternContainer.addView(btn);
        }
    }

    private void checkPatternTapAnswer() {
        boolean correct = true;
        for (int i = 0; i < colorPattern.length; i++) {
            if (!colorPattern[i].equals(userPatternInput.get(i))) {
                correct = false;
                break;
            }
        }
        if (correct) {
            stopAlarmAndFinish();
        } else {
            Toast.makeText(this, "Wrong pattern! Try again.", Toast.LENGTH_SHORT).show();
            userPatternInput.clear();
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
                generatePuzzle();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAlarmAndFinish() {
        if (sharedMediaPlayer != null) {
            try {
                if (sharedMediaPlayer.isPlaying()) {
                    sharedMediaPlayer.stop();
                }
                sharedMediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            sharedMediaPlayer = null;
            sharedSound = null;
        }
        if (alarmId != -1) {
            dbHelper.toggleAlarmEnabled(alarmId, false);
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        Toast.makeText(this, R.string.correct, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sharedMediaPlayer != null) {
            try {
                if (sharedMediaPlayer.isPlaying()) {
                    sharedMediaPlayer.stop();
                }
                sharedMediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            sharedMediaPlayer = null;
            sharedSound = null;
        }
    }

    @Override
    public void onBackPressed() {
        // Your custom logic here

        super.onBackPressed();  // make sure to call the superclass method
    }


}
