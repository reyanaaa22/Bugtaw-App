package com.example.bugtaw;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private ImageView appLogo;
    private TextView appName, appDescription;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Find views
        appLogo = findViewById(R.id.appLogo);
        appName = findViewById(R.id.appName);
        appDescription = findViewById(R.id.appDescription);
        startButton = findViewById(R.id.startButton);

        // Load animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);

        // Apply animations
        appLogo.startAnimation(fadeIn);
        appName.startAnimation(slideUp);
        appDescription.startAnimation(slideUp);
        startButton.startAnimation(slideUp);

        // Set click listener for start button
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
