package com.example.bugtaw;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class PuzzleActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private TextView puzzleText;
    private EditText answerInput;
    private Button submitButton;
    private String puzzleType;
    private int correctAnswer;

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

        // Get puzzle type from intent
        puzzleType = getIntent().getStringExtra("puzzle_type");
        if (puzzleType == null) {
            puzzleType = "Math Problem";
        }

        // Start alarm sound
        startAlarmSound();

        // Generate puzzle based on type
        generatePuzzle();

        // Set submit button click listener
        submitButton.setOnClickListener(v -> checkAnswer());
    }

    private void startAlarmSound() {
        // TODO: Replace with actual alarm sound resource
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
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
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
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
