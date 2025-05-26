package com.example.bugtaw;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PuzzleActivity extends AppCompatActivity {
    private EditText editTextAnswer;
    private Button buttonSubmit;
    private TextView textViewQuestion;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        editTextAnswer = findViewById(R.id.editTextAnswer);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        textViewQuestion = findViewById(R.id.textViewQuestion);

        // Generate a simple math question
        int num1 = (int) (Math.random() * 10);
        int num2 = (int) (Math.random() * 10);
        textViewQuestion.setText("What is " + num1 + " + " + num2 + "?");

        buttonSubmit.setOnClickListener(v -> {
            String answer = editTextAnswer.getText().toString();
            if (!answer.isEmpty()) {
                int userAnswer = Integer.parseInt(answer);
                int correctAnswer = num1 + num2;

                if (userAnswer == correctAnswer) {
                    // Stop the alarm and close the activity
                    Toast.makeText(this, "Correct! Alarm turned off.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Wrong answer! Try again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter an answer.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
